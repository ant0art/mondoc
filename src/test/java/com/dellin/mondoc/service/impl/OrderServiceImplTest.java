package com.dellin.mondoc.service.impl;

import com.dellin.mondoc.model.entity.Session;
import com.dellin.mondoc.model.entity.User;
import com.dellin.mondoc.model.pojo.OrderRequest;
import com.dellin.mondoc.model.pojo.OrderRequestBuilder;
import com.dellin.mondoc.model.pojo.OrderResponse;
import com.dellin.mondoc.service.IInterfaceManualLoad;
import com.dellin.mondoc.service.UserService;
import com.dellin.mondoc.utils.EncodingUtil;
import java.io.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.*;
import java.util.concurrent.*;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OrderServiceImplTest {
	
	@InjectMocks
	private OrderServiceImpl orderService;
	//	@Mock
	//	private Retrofit retrofit;
	@Mock
	private UserService userService;
	@Mock
	private IInterfaceManualLoad manualLoad;
	
	//	private OrderRepository orderRepository;
	//	private CompanyRepository companyRepository;
	//	private DocumentRepository documentRepository;
	
	@Test
	public void update() throws InterruptedException, IOException {
		
		CountDownLatch latch = new CountDownLatch(1);
		
		String name = "test@test.com";
		
		SecurityContext securityContext = mock(SecurityContext.class);
		Authentication a = new UsernamePasswordAuthenticationToken(name, null);
		
		when(securityContext.getAuthentication()).thenReturn(a);
		securityContext.setAuthentication(a);
		SecurityContextHolder.setContext(securityContext);
		
		Session session = new Session();
		session.setAppkey(EncodingUtil.getEncrypted("appkey"));
		session.setSessionDl(EncodingUtil.getEncrypted("sessionDl"));
		
		User user = new User();
		user.setEmail(name);
		user.setSession(session);
		
		when(userService.getUser(anyString())).thenReturn(user);
		
		OrderRequestBuilder requestBuilder = OrderRequest.builder()
				.setAppKey(EncodingUtil.getDecrypted(user.getSession().getAppkey()))
				.setSessionID(EncodingUtil.getDecrypted(user.getSession().getSessionDl()))
				.setDateStart("2023-01-01")
				.setDateEnd("2023-01-10");
		
		User someUser = mock(User.class);
		Session someSession = mock(Session.class);
		
		when(someUser.getSession()).thenReturn(session);
		when(someSession.getAppkey()).thenReturn(session.getAppkey());
		
		when(someUser.getSession()).thenReturn(session);
		when(someSession.getSessionDl()).thenReturn(session.getSessionDl());
		
		OrderRequest request = requestBuilder.build();
		
		//		OrderRequest orderRequest = new OrderRequest();
		//		orderRequest.setDocIds(new ArrayList<>());
		//		orderRequest.setAppKey(request.getAppKey());
		//		orderRequest.setSessionID(request.getSessionID());
		//		orderRequest.setDateStart(request.getDateStart());
		//		orderRequest.setDateEnd(request.getDateEnd());
		
		OrderRequest mockRequest = mock(OrderRequest.class);
		
		when(mockRequest.getDocIds()).thenReturn(request.getDocIds());
		
		OrderResponse response = new OrderResponse();
		
		Collection<OrderResponse.Order> orders = new ArrayList<>();
		OrderResponse.Order order = new OrderResponse.Order();
		orders.add(order);
		
		response.setOrders(orders);
		
		Runnable task = new Runnable() {
			@Override
			public void run() {
				
				latch.countDown();
			}
		};
		
		Thread thread = new Thread(task);
		thread.start();
		
		latch.await();
		
		orderService.update(request);
	}
	
	@Test
	public void extracted() throws IOException {
		
		String name = "test@test.com";
		Thread currentThread = Thread.currentThread();
		
		Date programStart = new Date();
		
		Session session = new Session();
		session.setAppkey(EncodingUtil.getEncrypted("appkey"));
		session.setSessionDl(EncodingUtil.getEncrypted("sessionDl"));
		
		User user = new User();
		user.setEmail(name);
		user.setSession(session);
		
		int currentPage = 1;
		int totalPages = 1;
		
		OrderRequestBuilder requestBuilder = OrderRequest.builder()
				.setAppKey(EncodingUtil.getDecrypted(user.getSession().getAppkey()))
				.setSessionID(EncodingUtil.getDecrypted(user.getSession().getSessionDl()))
				.setDateStart("2023-01-01")
				.setDateEnd("2023-01-10");
		
		//create a mock response
		
		OrderRequest orderRequest = mock(OrderRequest.class);
		
		OrderServiceImpl orderService1 = mock(OrderServiceImpl.class);
		
		Retrofit retrofit = new Retrofit.Builder().baseUrl("https://url.com/")
				.addConverterFactory(GsonConverterFactory.create()).build();
		IInterfaceManualLoad load = retrofit.create(IInterfaceManualLoad.class);
		
		when(orderService1.getRemoteData()).thenReturn(manualLoad);
		
		@SuppressWarnings("unchecked")
		Call<OrderResponse> call = (Call<OrderResponse>) mock(Call.class);
		when(manualLoad.update(orderRequest)).thenReturn(call);
		
		//		when(manualLoad.update(orderRequest)).thenReturn(call);
		
		OrderResponse expectedOrderResponse = new OrderResponse();
		//		Collection<OrderResponse.Order> orders = new ArrayList<>();
		//		OrderResponse.Order order = new OrderResponse.Order();
		//		orders.add(order);
		//		expectedOrderResponse.setOrders(orders);
		
		Response<OrderResponse> expectedResponse =
				Response.success(expectedOrderResponse);
		OrderResponse.Metadata metadata = new OrderResponse.Metadata();
		metadata.setStatus(200);
		metadata.setCurrentPage(1);
		metadata.setTotalPages(10);
		
		expectedResponse.body().setMetadata(metadata);
		
		System.out.println(expectedResponse);
		
		when(call.execute()).thenReturn(expectedResponse);
		//		Response<OrderResponse> re = mock(Response.class);
		//		when(re.body()).thenReturn(expectedOrderResponse);
		
		orderService.extracted(currentThread, orderRequest, user, 1, 1, requestBuilder,
				programStart);
	}
	
	@Test
	public void createAndUpdateOrders() {
	}
	
	@Test
	public void getRemoteData() {
		
		//		when(retrofit.create(IInterfaceManualLoad.class)).thenReturn(manualLoad);
		//
		//		IInterfaceManualLoad remoteData = orderService.getRemoteData();
		//		assertNotNull(remoteData);
	}
	
	@Test
	public void getOrder() {
	}
	
	@Test
	public void getOrders() {
	}
	
	@Test
	public void stopUpdate() {
	}
}
