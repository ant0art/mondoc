package com.dellin.mondoc.service.impl;

import com.dellin.mondoc.exceptions.CustomException;
import com.dellin.mondoc.model.entity.Company;
import com.dellin.mondoc.model.entity.Document;
import com.dellin.mondoc.model.entity.Order;
import com.dellin.mondoc.model.entity.User;
import com.dellin.mondoc.model.enums.EntityStatus;
import com.dellin.mondoc.model.enums.OrderDocType;
import com.dellin.mondoc.model.pojo.OrderRequest;
import com.dellin.mondoc.model.pojo.OrderRequestBuilder;
import com.dellin.mondoc.model.pojo.OrderResponse;
import com.dellin.mondoc.model.repository.CompanyRepository;
import com.dellin.mondoc.model.repository.DocumentRepository;
import com.dellin.mondoc.model.repository.OrderRepository;
import com.dellin.mondoc.model.repository.UserRepository;
import com.dellin.mondoc.service.IInterfaceManualLoad;
import com.dellin.mondoc.service.OrderService;
import com.dellin.mondoc.utils.EncodingUtil;
import com.dellin.mondoc.utils.OrderUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {
	
	private static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
	private final UserRepository userRepository;
	private final OrderRepository orderRepository;
	private final CompanyRepository companyRepository;
	private final DocumentRepository documentRepository;
	private final ObjectMapper mapper =
			JsonMapper.builder().addModule(new JavaTimeModule()).build();
	
	@Value("${api.address}")
	private String baseUrlFid;
	
	private IInterfaceManualLoad iInterfaceManualLoad;
	
	@Override
	public OrderResponse getOrderResponse(OrderRequest orderRequest) throws IOException {
		
		//find User
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		
		User user = userRepository.findByEmail(email).orElseThrow(
				() -> new CustomException(
						String.format("User with email: %s not found", email),
						HttpStatus.NOT_FOUND));
		
		OrderRequestBuilder requestBuilder = OrderRequest.builder()
				.setAppKey(EncodingUtil.getDecrypted(user.getSession().getAppkey()))
				.setSessionID(EncodingUtil.getDecrypted(user.getSession().getSessionDl()))
				.setPage(orderRequest.getPage());
		
		if (orderRequest.getDocIds() != null && !orderRequest.getDocIds().isEmpty()) {
			requestBuilder.setDocIds(orderRequest.getDocIds());
			log.info("Set docIds to orderRequest");
		} else {
			
			Date dateStart = OrderUtil.getParsedDate(orderRequest.getDateStart());
			Date dateEnd = OrderUtil.getParsedDate(orderRequest.getDateEnd());
			
			SimpleDateFormat formatter = new SimpleDateFormat(DATE_PATTERN);
			requestBuilder.setDateStart(formatter.format(dateStart))
					.setDateEnd(formatter.format(dateEnd));
			log.info("Set startDate: [{}], endDate: [{}]", dateStart, dateEnd);
		}
		
		OrderRequest build = requestBuilder.build();
		
		//		log.info("Api key: [{}]", build.getAppKey());
		log.info("Start date: [{}]", build.getDateStart());
		log.info("End date: [{}]", build.getDateEnd());
		log.info("SessionID: [{}]", build.getSessionID());
		
		Call<OrderResponse> orders = getRemoteData().getOrders(build);
		Response<OrderResponse> response = orders.execute();
		//Response received
		
		if (!response.isSuccessful()) {
			throw new IOException(
					response.errorBody() != null ? response.errorBody().string()
							: "Unknown error");
		}
		
		//Getting orders
		response.body().getOrders()
				.forEach(o -> {
					
					//getting documents marked as "shipping"
					o.getDocuments()
							.stream()
							.filter(d -> d.getType().equalsIgnoreCase("shipping"))
							.forEach(d -> {
								
								String orderAndDocUid = d.getUid();
								String orderId = d.getId();
								
								String inn = d.getPayer().getInn();
								
								//find doc in database
								Optional<Order> optionalOrder =
										orderRepository.findByDocId(orderId);
								Optional<Company> optionalCompany =
										companyRepository.findByInn(inn);
								
								//doc not found
								if (optionalOrder.isEmpty()) {
									
									Order order = new Order();
									order.setDocId(d.getId());
									order.setUid(orderAndDocUid);
									
									Company company;
									if (optionalCompany.isEmpty()) {
										company = new Company();
										company.setName(d.getPayer().getName());
										company.setInn(inn);
									} else {
										company = optionalCompany.get();
									}
									List<Document> documents;
									documents = d.getAvailableDocs()
											.stream()
											.filter(avDoc -> documentRepository.findByUidAndType(
																					   orderAndDocUid, OrderDocType.valueOf(
																							   avDoc.toUpperCase()))
																			   .isEmpty())
											.map(avDoc -> {
												Document document = new Document();
												document.setUid(orderAndDocUid);
												document.setType(OrderDocType.valueOf(
														avDoc.toUpperCase()));
												return document;
											})
											.collect(Collectors.toList());
									
									company.getOrders().add(order);
									
									order.setDocuments(documents);
									order.setStatus(EntityStatus.CREATED);
									order.setCompany(company);
									documents.forEach(doc -> doc.setOrder(order));
									orderRepository.save(order);
								}
							});
					log.info("orderID: [{}], payerName: [{}] have been created",
							o.getOrderId(), o.getPayer().getName());
				});
		
		log.info("Total pages: {}", response.body().getMetadata().getTotalPages());
		log.info("CurrentPage: {}", response.body().getMetadata().getCurrentPage());
		
		return response.body();
	}
	
	@Override
	public IInterfaceManualLoad getRemoteData() {
		
		Gson gson = new GsonBuilder().setLenient().create();
		
		if (iInterfaceManualLoad == null) {
			Retrofit retrofit = new Retrofit.Builder().baseUrl(baseUrlFid)
					.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
					.addConverterFactory(GsonConverterFactory.create(gson))
					.client(getUnsafeOkHttpClient()).build();
			iInterfaceManualLoad = retrofit.create(IInterfaceManualLoad.class);
		}
		return iInterfaceManualLoad;
	}
	
	private OkHttpClient getUnsafeOkHttpClient() {
		try {
			// Create a trust manager that does not validate certificate chains
			final TrustManager[] trustAllCerts =
					new TrustManager[]{new X509TrustManager() {
						
						public void checkClientTrusted(X509Certificate[] x509Certificates,
								String s) throws CertificateException {
						}
						
						public void checkServerTrusted(X509Certificate[] x509Certificates,
								String s) throws CertificateException {
						}
						
						public X509Certificate[] getAcceptedIssuers() {
							return new X509Certificate[]{};
						}
					}};
			
			// Install the all-trusting trust manager
			final SSLContext sslContext = SSLContext.getInstance("SSL");
			sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
			// Create an ssl socket factory with our all-trusting manager
			final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
			
			OkHttpClient.Builder builder = new OkHttpClient.Builder();
			
			builder.readTimeout(240, TimeUnit.SECONDS)
				   .connectTimeout(240, TimeUnit.SECONDS)
				   .writeTimeout(240, TimeUnit.SECONDS)
				   .hostnameVerifier(new HostnameVerifier() {
					   public boolean verify(String s, SSLSession sslSession) {
						   return true;
					   }
				   });
			
			return builder.build();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
