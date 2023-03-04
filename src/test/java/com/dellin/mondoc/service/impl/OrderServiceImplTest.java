package com.dellin.mondoc.service.impl;

import com.dellin.mondoc.model.dto.SessionDTO;
import com.dellin.mondoc.model.entity.Session;
import com.dellin.mondoc.model.entity.User;
import com.dellin.mondoc.model.pojo.AuthDellin;
import com.dellin.mondoc.model.pojo.DocumentRequest;
import com.dellin.mondoc.model.pojo.DocumentResponse;
import com.dellin.mondoc.model.pojo.OrderRequest;
import com.dellin.mondoc.model.pojo.OrderRequestBuilder;
import com.dellin.mondoc.model.pojo.OrderResponse;
import com.dellin.mondoc.service.IInterfaceManualLoad;
import com.dellin.mondoc.service.UserService;
import com.dellin.mondoc.utils.EncodingUtil;
import java.io.*;
import lombok.SneakyThrows;
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

import java.util.*;
import java.util.concurrent.*;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OrderServiceImplTest {
	
	@InjectMocks
	private OrderServiceImpl orderService;
	@Mock
	private Retrofit retrofit;
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
			@SneakyThrows
			@Override
			public void run() {
				
				Thread currentThread = Thread.currentThread();
				
				//create a mock response
				Call<OrderResponse> call = (Call<OrderResponse>) mock(Call.class);
				
				//				when(manualLoad.update(request)).thenReturn(call);
				//				Retrofit retrofit = mock(Retrofit.class);
				//				when(retrofit.create(IInterfaceManualLoad.class)).thenReturn(
				//						new MockManualLoad(call));
				
				when(call.execute()).thenReturn(Response.success(response));
				
				latch.countDown();
			}
		};
		
		Thread thread = new Thread(task);
		thread.start();
		
		latch.await();
		
		orderService.update(request);
	}
	
	@Test
	public void createAndUpdateOrders() {
	}
	
	@Test
	public void getRemoteData() {
		
		when(retrofit.create(IInterfaceManualLoad.class)).thenReturn(manualLoad);
		
		IInterfaceManualLoad remoteData = orderService.getRemoteData();
		assertNotNull(remoteData);
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
	
	private static class MockManualLoadTest implements IInterfaceManualLoad {
		
		private final Call<OrderResponse> update;
		
		private MockManualLoadTest(Call<OrderResponse> update) {
			this.update = update;
		}
		
		@Override
		public Call<AuthDellin> login(SessionDTO sessionDTO) {
			return null;
		}
		
		@Override
		public Call<AuthDellin> logout(SessionDTO sessionDTO) {
			return null;
		}
		
		@Override
		public Call<OrderResponse> update(OrderRequest orderRequest) {
			return update;
		}
		
		@Override
		public Call<DocumentResponse> getPrintableDoc(DocumentRequest documentRequest) {
			return null;
		}
	}
}
