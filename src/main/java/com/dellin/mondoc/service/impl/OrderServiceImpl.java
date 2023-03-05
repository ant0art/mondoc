package com.dellin.mondoc.service.impl;

import com.dellin.mondoc.exceptions.CustomException;
import com.dellin.mondoc.model.entity.Company;
import com.dellin.mondoc.model.entity.Document;
import com.dellin.mondoc.model.entity.Order;
import com.dellin.mondoc.model.entity.Role;
import com.dellin.mondoc.model.entity.User;
import com.dellin.mondoc.model.enums.EntityStatus;
import com.dellin.mondoc.model.enums.OrderDocType;
import com.dellin.mondoc.model.pojo.CommentHistory;
import com.dellin.mondoc.model.pojo.OrderModel;
import com.dellin.mondoc.model.pojo.OrderRequest;
import com.dellin.mondoc.model.pojo.OrderRequestBuilder;
import com.dellin.mondoc.model.pojo.OrderResponse;
import com.dellin.mondoc.model.repository.CompanyRepository;
import com.dellin.mondoc.model.repository.DocumentRepository;
import com.dellin.mondoc.model.repository.OrderRepository;
import com.dellin.mondoc.service.OrderService;
import com.dellin.mondoc.service.UserService;
import com.dellin.mondoc.utils.EncodingUtil;
import com.dellin.mondoc.utils.OrderUtil;
import com.dellin.mondoc.utils.PaginationUtil;
import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import retrofit2.Call;
import retrofit2.Response;

import java.util.*;
import java.util.stream.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
	
	private static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
	private final UserService userService;
	private final OrderRepository orderRepository;
	private final CompanyRepository companyRepository;
	private final DocumentRepository documentRepository;
	private final SyncService syncService;
	private Thread taskThread;
	
	@Override
	@Transactional
	public void update(OrderRequest orderRequest) throws IOException {
		
		log.info("Method [update() orders] started to work");
		Date programStart = new Date();
		
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = userService.getUser(email);
		
		OrderRequestBuilder requestBuilder = OrderRequest.builder()
				.setAppKey(EncodingUtil.getDecrypted(user.getSession().getAppkey()))
				.setSessionID(
						EncodingUtil.getDecrypted(user.getSession().getSessionDl()));
		
		if (orderRequest.getDocIds() != null && !orderRequest.getDocIds().isEmpty()) {
			requestBuilder.setDocIds(orderRequest.getDocIds());
			log.info("User: [name: {}] set docIds to orderRequest", user.getUsername());
		} else {
			
			Date dateStart = OrderUtil.getParsedDate(orderRequest.getDateStart());
			Date dateEnd = OrderUtil.getParsedDate(orderRequest.getDateEnd());
			
			SimpleDateFormat formatter = new SimpleDateFormat(DATE_PATTERN);
			requestBuilder.setDateStart(formatter.format(dateStart))
					.setDateEnd(formatter.format(dateEnd));
			log.info("User: [name: {}] set startDate: [{}], endDate: [{}]",
					user.getUsername(), dateStart, dateEnd);
		}
		
		Runnable task = new Runnable() {
			
			int currentPage = 1;
			int totalPages = Integer.MAX_VALUE;
			
			@Override
			public void run() {
				Thread thread = Thread.currentThread();
				
				extracted(thread, orderRequest, user, currentPage, totalPages,
						requestBuilder, programStart);
			}
		};
		taskThread = new Thread(task);
		taskThread.start();
	}
	
	@Override
	public void extracted(Thread thread, OrderRequest orderRequest, User user,
			int currentPage, int totalPages, OrderRequestBuilder requestBuilder,
			Date programStart) {
		Integer page = orderRequest.getPage();
		if (page != null) {
			log.info("User [EMAIL: {}] chose page [{}] for update", user.getUsername(),
					page);
			currentPage = page;
			totalPages = page;
		}
		
		log.info("Starting cycle of updating orders at [{}] page", currentPage);
		
		while (currentPage <= totalPages && !thread.isInterrupted()) {
			try {
				requestBuilder.setPage(currentPage);
					
					/*
					User can update database by two different ways:
					1. Including the list of interested in doc uid to the request
					2. Including dates from-to, setting the range by it
					These ways exclude each other from the request
					* */
				
				OrderRequest build = requestBuilder.build();
				
				Call<OrderResponse> orders = syncService.getRemoteData().update(build);
				
				Date start = new Date();
				log.info("Sending request to API");
				Response<OrderResponse> response = orders.execute();
				long responseTime = new Date().getTime() - start.getTime();
				log.info("Got the response in {} sec", responseTime / 1000.);
				
				//				assert response.body() != null;
				if (response.body() == null) {
					throw new CustomException("Response body is empty",
							HttpStatus.BAD_REQUEST);
				}
				Collection<OrderResponse.Order> ord = response.body().getOrders();
				createAndUpdateOrders(ord);
				if (page == null) {
					totalPages = response.body().getMetadata().getTotalPages();
				}
				log.info("End of page: [{}]. Total pages: [{}]", currentPage,
						response.body().getMetadata().getTotalPages());
				currentPage++;
				long timeout = 10000L - responseTime;
				log.info("Timeout before next request {} sec", timeout / 1000.);
				Thread.sleep(timeout);
			} catch (InterruptedException | IOException e) {
				log.error(e.getMessage());
				thread.interrupt();
			}
		}
		
		Date programEnd = new Date();
		long ms = programEnd.getTime() - programStart.getTime();
		log.info("Method [update() orders] finished after {} seconds of working",
				(ms / 1000L));
	}
	
	@Override
	public void createAndUpdateOrders(Collection<OrderResponse.Order> orders) {
		orders.forEach(o -> {
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
							if (company.getName().length() == 0 || !company.getName()
																		   .equals(d.getPayer()
																					.getName())) {
								company.setName(d.getPayer().getName());
								company.setStatus(EntityStatus.UPDATED);
								company.setUpdatedAt(LocalDateTime.now());
							}
							log.info("Loaded company from DB: [NAME: {}, " + "INN: {}]",
									company.getName(), company.getInn());
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
							Collection<Order> companyOrders = company.getOrders();
							companyOrders.add(order);
							company.setOrders(companyOrders);
							company.setStatus(EntityStatus.UPDATED);
							company.setUpdatedAt(LocalDateTime.now());
							log.info("Orders database updated. New "
											+ "order: [UID: {}, DOC_ID: {}] " + "added",
									order.getUid(), order.getDocId());
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
							
							log.info("Loaded order from DB: [UID: {}, " + "DOC_ID: {}]",
									order.getUid(), order.getDocId());
							//NOW WE HAVE ORDER
						}
						
						//WORKING WITH ORDER DOCS
						Collection<String> avDocs = d.getAvailableDocs();
						
						//COLLECTION OF UPDATES
						avDocs.stream()
								.filter(doc -> documentRepository.findByUidAndType(
																		 orderUID, OrderDocType.valueOf(doc.toUpperCase()))
																 .isEmpty())
								.forEach(doc -> {
									Document document = new Document();
									document.setType(
											OrderDocType.valueOf(doc.toUpperCase()));
									document.setOrder(order);
									document.setUid(orderUID);
									document.setStatus(EntityStatus.CREATED);
									log.info("Document's database updated. "
													+ "New document: [TYPE:"
													+ " {}, UID: {}] added",
											document.getType().name(), document.getUid());
									
									order.getDocuments().add(document);
								});
						
						order.setStatus(EntityStatus.UPDATED);
						order.setUpdatedAt(LocalDateTime.now());
						companyRepository.save(company);
					});
		});
	}
	
	@Override
	public Order getOrder(String docId) {
		return orderRepository.findByDocId(docId).orElseThrow(() -> new CustomException(
				String.format("Order with ID: %s not found", docId),
				HttpStatus.NOT_FOUND));
	}
	
	@Override
	public ModelMap getOrders(Integer page, Integer perPage, String sort,
			Sort.Direction order) {
		if (perPage == 0) {
			throw new CustomException("Page size must not be less than one",
					HttpStatus.BAD_REQUEST);
		}
		
		Pageable pageRequest = PaginationUtil.getPageRequest(page, perPage, sort, order);
		
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = userService.getUser(email);
		Collection<Company> companies = user.getCompanies();
		boolean role_admin = user.getRoles()
				.stream()
				.map(Role::getRoleName)
				.collect(Collectors.toList()).contains("ROLE_ADMIN");
		
		Page<Order> pageResult = orderRepository.findByCompanyIn(companies, pageRequest);
		
		List<Order> collect;
		if (role_admin) {
			collect = pageResult.getContent();
		} else {
			collect = pageResult.getContent()
					.stream()
					.filter(o -> !o.getComments().isEmpty())
					.collect(Collectors.toList());
		}
		
		List<OrderModel> content = collect.stream()
				.map(o -> {
					OrderModel orderModel = new OrderModel();
					
					List<CommentHistory> history = o.getComments()
							.stream()
							.map(c -> {
								
								CommentHistory commentHistory = new CommentHistory();
								commentHistory.setUserName(c.getUser().getUsername());
								commentHistory.setUpdatedAt(c.getUpdatedAt());
								commentHistory.setText(c.getText());
								return commentHistory;
							})
							.collect(Collectors.toList());
					orderModel.setComments(history);
					
					orderModel.setCompanyName(o.getCompany().getName());
					orderModel.setState(o.getState());
					orderModel.setDocId(o.getDocId());
					orderModel.setUid(o.getUid());
					return orderModel;
				})
				.collect(Collectors.toList());
		
		ModelMap map = new ModelMap();
		map.addAttribute("content", content);
		map.addAttribute("pageNumber", page);
		map.addAttribute("PageSize", pageResult.getNumberOfElements());
		map.addAttribute("totalPages", pageResult.getTotalPages());
		
		return map;
	}
	
	@Override
	public void stopUpdate() {
		
		taskThread.interrupt();
		log.warn("Update was manually stopped");
	}
}
