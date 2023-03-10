package com.dellin.mondoc.jobs;

import com.dellin.mondoc.model.dto.SessionDTO;
import com.dellin.mondoc.model.entity.Document;
import com.dellin.mondoc.model.pojo.AuthDellin;
import com.dellin.mondoc.model.pojo.DocumentRequest;
import com.dellin.mondoc.model.pojo.DocumentRequestBuilder;
import com.dellin.mondoc.model.pojo.DocumentResponse;
import com.dellin.mondoc.model.pojo.OrderRequest;
import com.dellin.mondoc.model.pojo.OrderRequestBuilder;
import com.dellin.mondoc.model.pojo.OrderResponse;
import com.dellin.mondoc.service.DocumentService;
import com.dellin.mondoc.service.OrderService;
import com.dellin.mondoc.service.impl.SyncService;
import com.dellin.mondoc.utils.OrderUtil;
import java.io.*;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Response;

import java.util.*;
import java.util.concurrent.*;

@Slf4j
@Service
@RequiredArgsConstructor
@EnableScheduling
public class OrderJob {
	
	private static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
	private final DocumentService documentService;
	private final OrderService orderService;
	private final SyncService syncService;
	
	@Value("${api.appkey}")
	String APPKEY = "";
	@Value("${api.login}")
	String LOGIN = "";
	@Value("${api.password}")
	String PASS = "";
	
	@Scheduled(cron = "0 0 21 ? * *")
	//	@Scheduled(fixedDelay = 1000000L, initialDelay = 0)
	public void getOrders() throws IOException {
		
		log.info("Scheduled method [getOrders()] started to work");
		
		Date programStart = new Date();
		
		/* STEP ONE
		Get sessionID */
		
		String sessionID = getSessionID();
		
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
				
				if (currentPage <= totalPages) {
					log.info("Starting cycle of updating orders at [{}] page",
							currentPage);
					requestBuilder.setPage(currentPage);
					
					Date start = new Date();
					log.info("Sending request to API");
					
					Call<OrderResponse> orders =
							syncService.getRemoteData().update(requestBuilder.build());
					try {
						Response<OrderResponse> orderResponse = orders.execute();
						log.info("Got the response in [{}] sec",
								(new Date().getTime() - start.getTime()) / 1000.);
			
					/* STEP THREE
					Get orders and put every new to DB or else update them */
						
						assert orderResponse.body() != null;
						Collection<OrderResponse.Order> ord =
								orderResponse.body().getOrders();
						
						orderService.createAndUpdateOrders(ord);
						totalPages = orderResponse.body().getMetadata().getTotalPages();
					} catch (IOException e) {
						log.error(e.getMessage());
					}
					log.info("End of page: [{}]. Total pages: [{}]", currentPage,
							totalPages);
					currentPage++;
				} else {
					Date programEnd = new Date();
					long ms = programEnd.getTime() - programStart.getTime();
					log.info("Method [getOrders()] finished after [{}] sec of working",
							(ms / 1000L));
					executorService.shutdown();
				}
			}
		};
		executorService.scheduleAtFixedRate(task, 0, 10, TimeUnit.SECONDS);
	}
	
	@Scheduled(cron = "0 30 21 ? * *")
	//	@Scheduled(fixedDelay = 1000000L, initialDelay = 0)
	public void getAvailableDocs() throws IOException {
		
		log.info("Method [getAvailableDocs()] started to work");
		Date programStart = new Date();
		
		/* STEP ONE
		Get sessionID */
		
		String sessionID = getSessionID();
		
		/* STEP TWO
		
		Build DocumentRequest
		All we need are: appkey, sessionID, mode, docUID*/
		
		ScheduledExecutorService executorService =
				Executors.newSingleThreadScheduledExecutor();
		
		//GET ALL DOCS IN DB WITH FIELD BASE64 MARKED BY NULL
		List<Document> byBase64Null = documentService.getDocsByBase64Null();
		
		Runnable task = new Runnable() {
			int count = 0;
			
			@Override
			public void run() {
				Document document = byBase64Null.get(count);
				
				if (count <= byBase64Null.size()) {
					try {
						log.info("Starting cycle of updating documents at [{}] of [{}]",
								count + 1, byBase64Null.size());
						
						log.info("Current document to update: [ID: {}, TYPE:{}, UID: "
										+ "{}, OrdID: {}]", document.getId(),
								document.getType().name(), document.getUid(),
								document.getOrder().getDocId());
						
						DocumentRequestBuilder requestBuilder = DocumentRequest.builder()
								.setAppkey(APPKEY)
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
								syncService.getRemoteData().getPrintableDoc(build);
						
						try {
							Response<DocumentResponse> docResponse =
									availableDoc.execute();
							log.info("Got the response in [{}] sec",
									(new Date().getTime() - start.getTime()) / 1000.);
							
							if (!docResponse.isSuccessful()) {
								
								log.error(docResponse.errorBody() != null
										? docResponse.errorBody().string()
										: "Unknown error");
							} else {
								assert docResponse.body() != null;
								Collection<DocumentResponse.Data> data =
										docResponse.body().getData();
								documentService.updateDocData(document, data);
							}
						} catch (NullPointerException e) {
							log.error(e.getMessage());
						}
						
						count++;
						log.info("Scheduled method [getAvailableDocs()] ended process on "
								+ "doc {} of {}", count, byBase64Null.size());
					} catch (IOException e) {
						log.error(e.getMessage());
					}
				} else {
					
					Date programEnd = new Date();
					long ms = programEnd.getTime() - programStart.getTime();
					log.info("Method [getAvailableDocs()] finished after [{}] sec of "
							+ "working", (ms / 1000L));
					executorService.shutdown();
				}
			}
		};
		executorService.scheduleAtFixedRate(task, 0, 10, TimeUnit.SECONDS);
	}
	
	private String getSessionID() throws IOException {
		SessionDTO sessionDTO = new SessionDTO();
		sessionDTO.setAppkey(APPKEY);
		sessionDTO.setLogin(LOGIN);
		sessionDTO.setPassword(PASS);
		Call<AuthDellin> login = syncService.getRemoteData().login(sessionDTO);
		
		Response<AuthDellin> response = login.execute();
		
		if (!response.isSuccessful()) {
			throw new IOException(
					response.errorBody() != null ? response.errorBody().string()
							: "Unknown error");
		}
		
		assert response.body() != null;
		return response.body().getData().getSessionID();
	}
}
