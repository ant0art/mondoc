package com.dellin.mondoc.service.impl;

import com.dellin.mondoc.exceptions.CustomException;
import com.dellin.mondoc.model.entity.Session;
import com.dellin.mondoc.model.entity.User;
import com.dellin.mondoc.model.pojo.OrderRequest;
import com.dellin.mondoc.model.pojo.OrderRequestBuilder;
import com.dellin.mondoc.model.pojo.OrderResponse;
import com.dellin.mondoc.model.repository.CompanyRepository;
import com.dellin.mondoc.model.repository.DocumentRepository;
import com.dellin.mondoc.model.repository.OrderRepository;
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
import org.springframework.transaction.annotation.Transactional;
import retrofit2.Call;
import retrofit2.Response;

import java.util.*;
import java.util.concurrent.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OrderServiceImplTest {
	
	@InjectMocks
	private OrderServiceImpl orderService;
	@Mock
	private UserService userService;
	@Mock
	private CompanyRepository companyService;
	@Mock
	private OrderRepository orderRepository;
	@Mock
	private DocumentRepository documentRepository;
	@Mock
	private SyncService syncService;
	
	@Test
	@Transactional
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
				.setDateEnd("2023-01-10")
				.setPage(2);
		
		User someUser = mock(User.class);
		Session someSession = mock(Session.class);
		
		when(someUser.getSession()).thenReturn(session);
		lenient().when(someSession.getAppkey())
				.thenReturn(session.getAppkey());
		
		lenient().when(someUser.getSession())
				.thenReturn(session);
		lenient().when(someSession.getSessionDl())
				.thenReturn(session.getSessionDl());
		
		OrderRequest request = requestBuilder.build();
		
		OrderRequest mockRequest = mock(OrderRequest.class);
		
		lenient().when(mockRequest.getDocIds())
				.thenReturn(request.getDocIds());
		
		OrderResponse response = new OrderResponse();
		
		Collection<OrderResponse.Order> orders = new ArrayList<>();
		OrderResponse.Order order = new OrderResponse.Order();
		orders.add(order);
		
		response.setOrders(orders);
		
		IInterfaceManualLoad remoteData = mock(IInterfaceManualLoad.class);
		when(syncService.getRemoteData()).thenReturn(remoteData);
		@SuppressWarnings("unchecked")
		Call<OrderResponse> call = (Call<OrderResponse>) mock(Call.class);
		when(remoteData.update(any(OrderRequest.class))).thenReturn(call);
		OrderResponse expectedOrderResponse = new OrderResponse();
		
		Response<OrderResponse> expectedResponse =
				Response.success(expectedOrderResponse);
		expectedOrderResponse.setOrders(Collections.emptyList());
		expectedOrderResponse.setDeleted(Collections.emptyList());
		
		OrderResponse.Metadata metadata = new OrderResponse.Metadata();
		metadata.setStatus(200);
		metadata.setCurrentPage(1);
		metadata.setTotalPages(2);
		
		expectedOrderResponse.setMetadata(metadata);
		when(call.execute()).thenReturn(expectedResponse);
		
		Runnable task = latch::countDown;
		
		Thread thread = new Thread(task);
		thread.start();
		
		latch.await();
		
		orderService.update(request);
	}
	
	@Test
	@Transactional
	public void update_docIds() throws InterruptedException, IOException {
		
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
		
		Collection<String> docIds = new ArrayList<>();
		docIds.add("123");
		docIds.add("456");
		
		OrderRequestBuilder requestBuilder = OrderRequest.builder()
				.setAppKey(EncodingUtil.getDecrypted(user.getSession().getAppkey()))
				.setSessionID(EncodingUtil.getDecrypted(user.getSession().getSessionDl()))
				.setDocIds(docIds)
				.setPage(2);
		
		User someUser = mock(User.class);
		Session someSession = mock(Session.class);
		
		when(someUser.getSession()).thenReturn(session);
		lenient().when(someSession.getAppkey())
				.thenReturn(session.getAppkey());
		
		lenient().when(someUser.getSession())
				.thenReturn(session);
		lenient().when(someSession.getSessionDl())
				.thenReturn(session.getSessionDl());
		
		OrderRequest request = requestBuilder.build();
		
		OrderRequest mockRequest = mock(OrderRequest.class);
		
		lenient().when(mockRequest.getDocIds())
				.thenReturn(request.getDocIds());
		
		OrderResponse response = new OrderResponse();
		
		Collection<OrderResponse.Order> orders = new ArrayList<>();
		OrderResponse.Order order = new OrderResponse.Order();
		orders.add(order);
		
		response.setOrders(orders);
		
		IInterfaceManualLoad remoteData = mock(IInterfaceManualLoad.class);
		when(syncService.getRemoteData()).thenReturn(remoteData);
		@SuppressWarnings("unchecked")
		Call<OrderResponse> call = (Call<OrderResponse>) mock(Call.class);
		when(remoteData.update(any(OrderRequest.class))).thenReturn(call);
		OrderResponse expectedOrderResponse = new OrderResponse();
		
		Response<OrderResponse> expectedResponse =
				Response.success(expectedOrderResponse);
		expectedOrderResponse.setOrders(Collections.emptyList());
		expectedOrderResponse.setDeleted(Collections.emptyList());
		
		OrderResponse.Metadata metadata = new OrderResponse.Metadata();
		metadata.setStatus(200);
		metadata.setCurrentPage(1);
		metadata.setTotalPages(2);
		
		expectedOrderResponse.setMetadata(metadata);
		when(call.execute()).thenReturn(expectedResponse);
		
		Runnable task = latch::countDown;
		
		Thread thread = new Thread(task);
		thread.start();
		
		latch.await();
		
		orderService.update(request);
	}
	
	@Test
	@Transactional
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
		
		OrderRequestBuilder requestBuilder = OrderRequest.builder()
				.setAppKey(EncodingUtil.getDecrypted(user.getSession().getAppkey()))
				.setSessionID(EncodingUtil.getDecrypted(user.getSession().getSessionDl()))
				.setDateStart("2023-01-01")
				.setDateEnd("2023-01-10");
		
		//create a mock response
		
		OrderRequest orderRequest = requestBuilder.build();
		
		IInterfaceManualLoad remoteData = mock(IInterfaceManualLoad.class);
		when(syncService.getRemoteData()).thenReturn(remoteData);
		@SuppressWarnings("unchecked")
		Call<OrderResponse> call = (Call<OrderResponse>) mock(Call.class);
		when(remoteData.update(any(OrderRequest.class))).thenReturn(call);
		
		OrderResponse expectedOrderResponse = new OrderResponse();
		expectedOrderResponse.setOrders(Collections.emptyList());
		expectedOrderResponse.setDeleted(Collections.emptyList());
		
		OrderResponse.Metadata metadata = new OrderResponse.Metadata();
		metadata.setStatus(200);
		metadata.setCurrentPage(1);
		metadata.setTotalPages(2);
		
		expectedOrderResponse.setMetadata(metadata);
		
		Response<OrderResponse> expectedResponse =
				Response.success(expectedOrderResponse);
		
		when(call.execute()).thenReturn(expectedResponse);
		
		orderService.extracted(currentThread, orderRequest, user, 1, 1, requestBuilder,
				programStart);
	}
	
	@Test(expected = CustomException.class)
	@Transactional
	public void extracted_responseBodyNull() throws IOException {
		
		String name = "test@test.com";
		Thread currentThread = Thread.currentThread();
		
		Date programStart = new Date();
		
		Session session = new Session();
		session.setAppkey(EncodingUtil.getEncrypted("appkey"));
		session.setSessionDl(EncodingUtil.getEncrypted("sessionDl"));
		
		User user = new User();
		user.setEmail(name);
		user.setSession(session);
		
		OrderRequestBuilder requestBuilder = OrderRequest.builder()
				.setAppKey(EncodingUtil.getDecrypted(user.getSession().getAppkey()))
				.setSessionID(EncodingUtil.getDecrypted(user.getSession().getSessionDl()))
				.setDateStart("2023-01-01")
				.setDateEnd("2023-01-10")
				.setPage(1);
		
		//create a mock response
		
		OrderRequest orderRequest = requestBuilder.build();
		
		IInterfaceManualLoad remoteData = mock(IInterfaceManualLoad.class);
		when(syncService.getRemoteData()).thenReturn(remoteData);
		@SuppressWarnings("unchecked")
		Call<OrderResponse> call = (Call<OrderResponse>) mock(Call.class);
		when(remoteData.update(any(OrderRequest.class))).thenReturn(call);
		
		Response<OrderResponse> expectedResponse = mock(Response.class);
		when(call.execute()).thenReturn(expectedResponse);
		
		orderService.extracted(currentThread, orderRequest, user, 1, 1, requestBuilder,
				programStart);
	}
	
	@Test
	@Transactional
	public void extracted_interrupted() throws IOException {
		
		String name = "test@test.com";
		Thread currentThread = Thread.currentThread();
		
		Date programStart = new Date();
		
		Session session = new Session();
		session.setAppkey(EncodingUtil.getEncrypted("appkey"));
		session.setSessionDl(EncodingUtil.getEncrypted("sessionDl"));
		
		User user = new User();
		user.setEmail(name);
		user.setSession(session);
		
		OrderRequestBuilder requestBuilder = OrderRequest.builder()
				.setAppKey(EncodingUtil.getDecrypted(user.getSession().getAppkey()))
				.setSessionID(EncodingUtil.getDecrypted(user.getSession().getSessionDl()))
				.setDateStart("2023-01-01")
				.setDateEnd("2023-01-10");
		
		//create a mock response
		
		OrderRequest orderRequest = requestBuilder.build();
		
		IInterfaceManualLoad remoteData = mock(IInterfaceManualLoad.class);
		when(syncService.getRemoteData()).thenReturn(remoteData);
		@SuppressWarnings("unchecked")
		Call<OrderResponse> call = (Call<OrderResponse>) mock(Call.class);
		when(remoteData.update(any(OrderRequest.class))).thenReturn(call);
		
		OrderResponse expectedOrderResponse = new OrderResponse();
		expectedOrderResponse.setOrders(Collections.emptyList());
		expectedOrderResponse.setDeleted(Collections.emptyList());
		
		OrderResponse.Metadata metadata = new OrderResponse.Metadata();
		metadata.setStatus(200);
		metadata.setCurrentPage(1);
		metadata.setTotalPages(2);
		
		expectedOrderResponse.setMetadata(metadata);
		
		Response<OrderResponse> expectedResponse =
				Response.success(expectedOrderResponse);
		
		when(call.execute()).thenReturn(expectedResponse);
		
		Runnable test = new Runnable() {
			@Override
			public void run() {
				Thread testThread = Thread.currentThread();
				try {
					testThread.sleep(1000L);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
				currentThread.interrupt();
			}
		};
		Thread thread = new Thread(test);
		thread.start();
		
		orderService.extracted(currentThread, orderRequest, user, 1, 1, requestBuilder,
				programStart);
		assertThat(currentThread.isInterrupted(), is(true));
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
