package com.dellin.mondoc.jobs;

import com.dellin.mondoc.model.dto.SessionDTO;
import com.dellin.mondoc.model.entity.Company;
import com.dellin.mondoc.model.entity.Document;
import com.dellin.mondoc.model.entity.Order;
import com.dellin.mondoc.model.enums.EntityStatus;
import com.dellin.mondoc.model.enums.OrderDocType;
import com.dellin.mondoc.model.pojo.AuthDellin;
import com.dellin.mondoc.model.pojo.DocumentRequest;
import com.dellin.mondoc.model.pojo.DocumentRequestBuilder;
import com.dellin.mondoc.model.pojo.DocumentResponse;
import com.dellin.mondoc.model.pojo.OrderRequest;
import com.dellin.mondoc.model.pojo.OrderRequestBuilder;
import com.dellin.mondoc.model.pojo.OrderResponse;
import com.dellin.mondoc.model.repository.CompanyRepository;
import com.dellin.mondoc.model.repository.DocumentRepository;
import com.dellin.mondoc.model.repository.OrderRepository;
import com.dellin.mondoc.service.OrderService;
import com.dellin.mondoc.service.SessionService;
import com.dellin.mondoc.utils.OrderUtil;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import retrofit2.Call;
import retrofit2.Response;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

@Slf4j
@Service
@RequiredArgsConstructor
@EnableScheduling
public class OrderJob {
	
	private static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
	private final OrderRepository orderRepository;
	private final SessionService sessionService;
	
	private final DocumentRepository documentRepository;
	private final CompanyRepository companyRepository;
	private final OrderService orderService;
	
	private final String APPKEY = System.getenv("appkey");
	private final String LOGIN = System.getenv("loginDl");
	private final String PASS = System.getenv("passDl");
	
	@Scheduled(cron = "0 0 21 ? * *")
	//	@Scheduled(fixedDelay = 1000000L, initialDelay = 0)
	@Transactional
	public void getOrders() throws IOException, InterruptedException {
		
		log.info("Scheduled method [getOrders] started to work");
		
		Date programStart = new Date();
		
		/* STEP ONE
		Get sessionID */
		
		SessionDTO sessionDTO = new SessionDTO();
		sessionDTO.setAppkey(APPKEY);
		sessionDTO.setLogin(LOGIN);
		sessionDTO.setPassword(PASS);
		Call<AuthDellin> login = sessionService.getRemoteData().login(sessionDTO);
		
		Response<AuthDellin> response = login.execute();
		
		if (!response.isSuccessful()) {
			throw new IOException(
					response.errorBody() != null ? response.errorBody().string()
							: "Unknown error");
		}
		
		assert response.body() != null;
		String sessionID = response.body().getData().getSessionID();
		
		/* STEP TWO
		
		Build OrderRequest to get totalPages
		All we need are: appkey, sessionID, dates, page*/
		
		LocalDate dateEnd = LocalDate.now();
		LocalDate dateStart = dateEnd.minusMonths(2);
		
		ScheduledExecutorService executorService =
				Executors.newSingleThreadScheduledExecutor();
		
		Runnable task = new Runnable() {
			
			int currentPage = 1;
			final OrderRequestBuilder requestBuilder = OrderRequest.builder()
					.setAppKey(APPKEY)
					.setSessionID(sessionID)
					.setPage(currentPage)
					.setDateEnd(OrderUtil.getFormattedDate(dateEnd, DATE_PATTERN))
					.setDateStart(OrderUtil.getFormattedDate(dateStart, DATE_PATTERN));
			int totalPages = Integer.MAX_VALUE;
			
			@Override
			public void run() {
				
				log.info("Starting cycle of updating orders at {} page", currentPage);
				
				if (currentPage <= totalPages) {
					requestBuilder.setPage(currentPage);
					
					Date start = new Date();
					log.info("Sending request to API");
					
					Call<OrderResponse> orders = orderService.getRemoteData().getOrders(
							requestBuilder.build());
					Response<OrderResponse> orderResponse = null;
					try {
						orderResponse = orders.execute();
						log.info("Got the response in {} ms",
								(new Date().getTime() - start.getTime()) / 1000.);
					} catch (IOException e) {
						log.error(e.getMessage());
					}
					
			
			/* STEP THREE
			Get orders and put every new to DB or else update them */
					
					Collection<OrderResponse.Order> ord =
							orderResponse.body().getOrders();
					
					ord.forEach(o -> {
						Collection<OrderResponse.Order.Document> docs = o.getDocuments();
						docs.stream()
								.filter(d -> d.getType().equalsIgnoreCase("shipping"))
								.forEach(d -> {
									String orderId = d.getId();
									String orderUID = d.getUid();
									String innPayer = d.getPayer().getInn();
									
									Optional<Company> optionalCompany =
											companyRepository.findByInn(innPayer);
									Optional<Order> optionalOrder =
											orderRepository.findByDocId(orderId);
									
									//WORKING WITH COMPANY
									Company company;
									if (optionalCompany.isEmpty()) {
										//CREATE
										company = new Company();
										company.setStatus(EntityStatus.CREATED);
										company.setInn(innPayer);
										company.setName(d.getPayer().getName());
										log.info("Company's database updated. New "
														+ "company: [NAME: {}, INN: {}] added",
												company.getName(), company.getInn());
										//NOW WE HAVE COMPANY
									} else {
										//USE EX FROM DB
										company = optionalCompany.get();
										if (company.getName().length() == 0) {
											company.setName(d.getPayer().getName());
											company.setStatus(EntityStatus.UPDATED);
											company.setUpdatedAt(LocalDateTime.now());
										}
										log.info("Loaded company from DB: [NAME: {}, "
														+ "INN: {}]", company.getName(),
												company.getInn());
										//NOW WE HAVE COMPANY
									}
									
									//WORKING WITH ORDER
									Order order;
									if (optionalOrder.isEmpty()) {
										order = new Order();
										order.setUid(orderUID);
										order.setStatus(EntityStatus.CREATED);
										order.setState(o.getState());
										order.setDocId(orderId);
										order.setCompany(company);
										Collection<Order> companyOrders =
												company.getOrders();
										companyOrders.add(order);
										company.setOrders(companyOrders);
										company.setStatus(EntityStatus.UPDATED);
										company.setUpdatedAt(LocalDateTime.now());
										log.info("Orders database updated. New "
														+ "order: [UID: {}, DOC_ID: {}] "
														+ "added", order.getUid(),
												order.getDocId());
										//NOW WE HAVE ORDER
									} else {
										//USE EX FROM DB
										order = optionalOrder.get();
										//order checks:
										if (!o.getState().equals(order.getState())) {
											order.setState(o.getState());
											order.setStatus(EntityStatus.UPDATED);
											order.setUpdatedAt(LocalDateTime.now());
										}
										
										log.info("Loaded order from DB: [UID: {}, "
														+ "DOC_ID: {}]", order.getUid(),
												order.getDocId());
										//NOW WE HAVE ORDER
									}
									
									//WORKING WITH ORDER DOCS
									Collection<String> avDocs = d.getAvailableDocs();
									
									//COLLECTION OF UPDATES
									List<Document> collect = avDocs.stream()
											.filter(doc -> documentRepository.findByUidAndType(
													orderUID, OrderDocType.valueOf(
															doc.toUpperCase())).isEmpty())
											.map(doc -> {
												Document document = new Document();
												document.setType(OrderDocType.valueOf(
														doc.toUpperCase()));
												document.setOrder(order);
												document.setUid(orderUID);
												document.setStatus(EntityStatus.CREATED);
												log.info("Document's database updated. "
																+ "New document: [TYPE:"
																+ " {}, UID: {}] added",
														document.getType().name(),
														document.getUid());
												return document;
											})
											.collect(Collectors.toList());
									
									order.setDocuments(collect);
									order.setStatus(EntityStatus.UPDATED);
									order.setUpdatedAt(LocalDateTime.now());
									companyRepository.save(company);
								});
					});
					totalPages = orderResponse.body().getMetadata().getTotalPages();
					log.info("End of page: [{}]. Total pages: [{}]", currentPage,
							totalPages);
					currentPage++;
				} else {
					Date programEnd = new Date();
					long ms = programEnd.getTime() - programStart.getTime();
					log.info("Method finished after {} seconds of working", (ms / 1000L));
					executorService.shutdown();
				}
			}
		};
		executorService.scheduleAtFixedRate(task, 0, 10, TimeUnit.SECONDS);
	}
	
	@Scheduled(cron = "0 30 21 ? * *")
	//	@Scheduled(fixedDelay = 1000000L, initialDelay = 0)
	@Transactional
	@DependsOn("getOrders")
	public void getAvailableDocs() throws IOException {
		
		Date programStart = new Date();
		
		/* STEP ONE
		Get sessionID */
		
		SessionDTO sessionDTO = new SessionDTO();
		sessionDTO.setAppkey(APPKEY);
		sessionDTO.setLogin(LOGIN);
		sessionDTO.setPassword(PASS);
		Call<AuthDellin> login = sessionService.getRemoteData().login(sessionDTO);
		
		Response<AuthDellin> response = login.execute();
		
		if (!response.isSuccessful()) {
			throw new IOException(
					response.errorBody() != null ? response.errorBody().string()
							: "Unknown error");
		}
		
		assert response.body() != null;
		String sessionID = response.body().getData().getSessionID();
		
		/* STEP TWO
		
		Build DocumentRequest
		All we need are: appkey, sessionID, mode, docUID*/
		
		ScheduledExecutorService executorService =
				Executors.newSingleThreadScheduledExecutor();
		
		//GET ALL DOCS IN DB WITH FIELD BASE64 MARKED BY NULL
		List<Document> byBase64Null = documentRepository.findByBase64Null();
		
		Runnable task = new Runnable() {
			int count = 0;
			
			@Override
			public void run() {
				log.info("Starting cycle of updating documents at {} element of {}",
						count + 1, byBase64Null.size());
				
				Document document = byBase64Null.get(count);
				
				log.info("Current document to update: [ID: {}, UID: {}]",
						document.getId(), document.getUid());
				
				if (count <= byBase64Null.size()) {
					DocumentRequestBuilder requestBuilder =
							DocumentRequest.builder().setAppkey(APPKEY)
									.setSessionID(sessionID)
									.setMode(document.getType().name().toLowerCase())
									.setDocUID(document.getUid());
					
					/*STEP THREE
					 * Get doc from API and update one from DB
					 * */
					DocumentRequest build = requestBuilder.build();
					Date start = new Date();
					log.info("Sending request to API");
					Call<DocumentResponse> availableDoc =
							orderService.getRemoteData().getPrintableDoc(build);
					
					Response<DocumentResponse> docResponse = null;
					try {
						docResponse = availableDoc.execute();
						log.info("Got the response in {} ms",
								(new Date().getTime() - start.getTime()) / 1000.);
					} catch (IOException e) {
						log.error(e.getMessage());
					}
					
					DocumentResponse body = docResponse.body();
					
					Collection<DocumentResponse.Data> data = body.getData();
					data.forEach(d -> {
						
						if (d.getBase64() != null && !d.getBase64().isEmpty()) {
							document.setBase64(d.getBase64());
						}
						if (document.getType() == OrderDocType.GIVEOUT) {
							if (!d.getUrls().isEmpty()) {
								d.getUrls()
										.stream()
										.filter(Objects::nonNull).findFirst().ifPresent(
										 document::setUrl);
							}
						}
						
						document.setStatus(EntityStatus.UPDATED);
						document.setUpdatedAt(LocalDateTime.now());
						documentRepository.save(document);
						log.info("Document: [TYPE: {}, UID: {}] updated",
								document.getType().name(), document.getUid());
					});
					count++;
					log.info("Scheduled method \"getAvailableDocs()\" ended process on "
							+ "doc {} of {}", count, byBase64Null.size());
				} else {
					
					Date programEnd = new Date();
					long ms = programEnd.getTime() - programStart.getTime();
					log.info("Method finished after {} seconds of working", (ms / 1000L));
					executorService.shutdown();
				}
			}
		};
		executorService.scheduleAtFixedRate(task, 0, 10, TimeUnit.SECONDS);
	}
}
