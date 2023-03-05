package com.dellin.mondoc.service.impl;

import com.dellin.mondoc.exceptions.CustomException;
import com.dellin.mondoc.model.entity.Comment;
import com.dellin.mondoc.model.entity.Company;
import com.dellin.mondoc.model.entity.Document;
import com.dellin.mondoc.model.entity.Order;
import com.dellin.mondoc.model.entity.Role;
import com.dellin.mondoc.model.entity.Session;
import com.dellin.mondoc.model.entity.User;
import com.dellin.mondoc.model.enums.OrderDocType;
import com.dellin.mondoc.model.pojo.OrderModel;
import com.dellin.mondoc.model.pojo.OrderRequest;
import com.dellin.mondoc.model.pojo.OrderRequestBuilder;
import com.dellin.mondoc.model.pojo.OrderResponse;
import com.dellin.mondoc.model.repository.CompanyRepository;
import com.dellin.mondoc.model.repository.DocumentRepository;
import com.dellin.mondoc.model.repository.OrderRepository;
import com.dellin.mondoc.service.UserService;
import com.dellin.mondoc.utils.EncodingUtil;
import java.io.*;
import java.time.LocalDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import retrofit2.Call;
import retrofit2.Response;

import java.util.*;
import java.util.concurrent.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
		
		Runnable test = () -> {
			Thread testThread = Thread.currentThread();
			try {
				testThread.sleep(1000L);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			currentThread.interrupt();
		};
		Thread thread = new Thread(test);
		thread.start();
		
		orderService.extracted(currentThread, orderRequest, user, 1, 1, requestBuilder,
				programStart);
		assertThat(currentThread.isInterrupted(), is(true));
	}
	
	@Test
	public void createAndUpdateOrders() {
		
		OrderResponse.Order.Member payer = new OrderResponse.Order.Member();
		payer.setName("Mondoc");
		payer.setInn("123456789");
		
		Collection<String> avDocs = new ArrayList<>();
		avDocs.add("Bill");
		
		OrderResponse.Order.Document document = new OrderResponse.Order.Document();
		document.setType("shipping");
		document.setUid("0x1");
		document.setId("id1");
		document.setPayer(payer);
		document.setAvailableDocs(avDocs);
		
		Collection<OrderResponse.Order.Document> documents = new ArrayList<>();
		documents.add(document);
		
		OrderResponse.Order order = new OrderResponse.Order();
		order.setDocuments(documents);
		order.setState("finished");
		
		Company companyEnt = new Company();
		companyEnt.setInn("123456789");
		companyEnt.setName("company");
		
		Order orderEnt = new Order();
		orderEnt.setUid("0x1");
		orderEnt.setState("arrived");
		
		Document documentEnt = new Document();
		documentEnt.setUid("0x1");
		documentEnt.setType(OrderDocType.BILL);
		
		Collection<OrderResponse.Order> orders = new ArrayList<>();
		orders.add(order);
		
		when(companyService.findByInn(anyString())).thenReturn(Optional.of(companyEnt));
		when(orderRepository.findByDocId(anyString())).thenReturn(Optional.of(orderEnt));
		
		when(documentRepository.findByUidAndType(anyString(),
				any(OrderDocType.class))).thenReturn(Optional.empty());
		
		@SuppressWarnings("unchecked")
		Collection<String> strings = mock(Collection.class);
		lenient().when(strings.stream())
				.thenReturn(avDocs.stream());
		
		orderService.createAndUpdateOrders(orders);
		verify(companyService, times(1)).save(companyEnt);
	}
	
	@Test
	public void createAndUpdateOrders_emptyCompany() {
		
		OrderResponse.Order.Member payer = new OrderResponse.Order.Member();
		payer.setName("Mondoc");
		payer.setInn("123456789");
		
		Collection<String> avDocs = new ArrayList<>();
		avDocs.add("Bill");
		
		OrderResponse.Order.Document document = new OrderResponse.Order.Document();
		document.setType("shipping");
		document.setUid("0x1");
		document.setId("id1");
		document.setPayer(payer);
		document.setAvailableDocs(avDocs);
		
		Collection<OrderResponse.Order.Document> documents = new ArrayList<>();
		documents.add(document);
		
		OrderResponse.Order order = new OrderResponse.Order();
		order.setDocuments(documents);
		order.setState("finished");
		
		Collection<OrderResponse.Order> orders = new ArrayList<>();
		orders.add(order);
		
		when(companyService.findByInn(anyString())).thenReturn(Optional.empty());
		
		when(orderRepository.findByDocId(anyString())).thenReturn(Optional.empty());
		
		when(documentRepository.findByUidAndType(anyString(),
				any(OrderDocType.class))).thenReturn(Optional.empty());
		
		Mockito.lenient()
				.when(companyService.save(any(Company.class)))
				.thenAnswer(i -> i.getArguments()[0]);
		orderService.createAndUpdateOrders(orders);
		verify(companyService).save(any(Company.class));
	}
	
	@Test
	public void getOrder() {
		
		Order order = new Order();
		order.setUid("0x1");
		when(orderRepository.findByDocId(anyString())).thenReturn(Optional.of(order));
		Order result = orderService.getOrder("1");
		assertEquals(order.getUid(), result.getUid());
	}
	
	@Test(expected = CustomException.class)
	public void getOrder_notFound() {
		
		orderService.getOrder("1");
	}
	
	@Test
	public void getOrders() {
		Integer page = 2;
		Integer perPage = 10;
		String sort = "state";
		Sort.Direction order = Sort.Direction.DESC;
		
		Order ord = new Order();
		ord.setState("finished");
		ord.setDocId("11-22");
		ord.setUid("0x1");
		
		Company company = new Company();
		company.setInn("123456789");
		company.setName("company");
		ord.setCompany(company);
		
		List<Order> orders = Collections.singletonList(ord);
		
		@SuppressWarnings("unchecked")
		Page<Order> pageResult = mock(Page.class);
		
		String name = "test@test.com";
		
		SecurityContext securityContext = mock(SecurityContext.class);
		Authentication a = new UsernamePasswordAuthenticationToken(name, null);
		
		when(securityContext.getAuthentication()).thenReturn(a);
		securityContext.setAuthentication(a);
		SecurityContextHolder.setContext(securityContext);
		
		User user = new User();
		user.setEmail(name);
		Role role = new Role();
		role.setRoleName("ROLE_ADMIN");
		List<Role> roles = Collections.singletonList(role);
		user.setRoles(roles);
		
		Comment comment = new Comment();
		comment.setText("some text");
		comment.setUser(user);
		comment.setUpdatedAt(LocalDateTime.now());
		
		Collection<Comment> comments = Collections.singletonList(comment);
		ord.setComments(comments);
		
		when(userService.getUser(anyString())).thenReturn(user);
		
		when(orderRepository.findByCompanyIn(any(Collection.class),
				any(Pageable.class))).thenReturn(pageResult);
		when(pageResult.getContent()).thenReturn(orders);
		
		@SuppressWarnings("unchecked")
		Collection<Order> mockOrders = mock(Collection.class);
		lenient().when(mockOrders.stream())
				.thenReturn(orders.stream());
		
		ModelMap resultMap = orderService.getOrders(page, perPage, sort, order);
		
		@SuppressWarnings("unchecked")
		List<OrderModel> orderModels = (List<OrderModel>) resultMap.get("content");
		
		assertEquals(ord.getDocId(), orderModels.get(0).getDocId());
	}
	
	@Test(expected = CustomException.class)
	public void getOrders_perPageZero() {
		
		Integer page = 1;
		Integer perPage = 0;
		String sort = "state";
		Sort.Direction order = Sort.Direction.DESC;
		
		orderService.getOrders(page, perPage, sort, order);
	}
	
	@Test
	public void getOrders_notRoleAdmin() {
		
		Integer page = 2;
		Integer perPage = 10;
		String sort = "state";
		Sort.Direction order = Sort.Direction.DESC;
		
		Order ord = new Order();
		ord.setState("finished");
		ord.setDocId("11-22");
		ord.setUid("0x1");
		
		Company company = new Company();
		company.setInn("123456789");
		company.setName("company");
		ord.setCompany(company);
		
		List<Order> orders = Collections.singletonList(ord);
		
		@SuppressWarnings("unchecked")
		Page<Order> pageResult = mock(Page.class);
		
		String name = "test@test.com";
		
		SecurityContext securityContext = mock(SecurityContext.class);
		Authentication a = new UsernamePasswordAuthenticationToken(name, null);
		
		when(securityContext.getAuthentication()).thenReturn(a);
		securityContext.setAuthentication(a);
		SecurityContextHolder.setContext(securityContext);
		
		User user = new User();
		user.setEmail(name);
		Role role = new Role();
		role.setRoleName("ROLE_USER");
		List<Role> roles = Collections.singletonList(role);
		user.setRoles(roles);
		
		Comment comment = new Comment();
		comment.setText("some text");
		comment.setUser(user);
		comment.setUpdatedAt(LocalDateTime.now());
		
		Collection<Comment> comments = Collections.singletonList(comment);
		ord.setComments(comments);
		
		when(userService.getUser(anyString())).thenReturn(user);
		
		when(orderRepository.findByCompanyIn(any(Collection.class),
				any(Pageable.class))).thenReturn(pageResult);
		when(pageResult.getContent()).thenReturn(orders);
		
		@SuppressWarnings("unchecked")
		Collection<Order> mockOrders = mock(Collection.class);
		lenient().when(mockOrders.stream())
				.thenReturn(orders.stream());
		
		ModelMap resultMap = orderService.getOrders(page, perPage, sort, order);
		
		@SuppressWarnings("unchecked")
		List<OrderModel> orderModels = (List<OrderModel>) resultMap.get("content");
		
		assertEquals(ord.getDocId(), orderModels.get(0).getDocId());
	}
	
	@Test
	@Transactional
	public void stopUpdate() {
		
		Thread thread = mock(Thread.class);
		
		orderService.setTaskThread(thread);
		orderService.setInitializedThread(true);
		
		orderService.stopUpdate();
		
		verify(thread, atLeastOnce()).interrupt();
	}
}
