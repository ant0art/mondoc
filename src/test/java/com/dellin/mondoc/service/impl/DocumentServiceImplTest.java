package com.dellin.mondoc.service.impl;

import com.dellin.mondoc.model.entity.Company;
import com.dellin.mondoc.model.entity.Document;
import com.dellin.mondoc.model.entity.Order;
import com.dellin.mondoc.model.entity.Session;
import com.dellin.mondoc.model.entity.User;
import com.dellin.mondoc.model.enums.OrderDocType;
import com.dellin.mondoc.model.pojo.DocumentRequest;
import com.dellin.mondoc.model.pojo.DocumentResponse;
import com.dellin.mondoc.model.repository.DocumentRepository;
import com.dellin.mondoc.service.UserService;
import com.dellin.mondoc.utils.EncodingUtil;
import java.io.*;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
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

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DocumentServiceImplTest {
	
	@InjectMocks
	public DocumentServiceImpl documentService;
	
	@Mock
	public DocumentRepository documentRepository;
	
	@Mock
	private UserService userService;
	
	@Mock
	private SyncService syncService;
	
	@Test
	public void update() throws IOException {
		
		String name = "test@test.com";
		
		SecurityContext securityContext = mock(SecurityContext.class);
		Authentication a = new UsernamePasswordAuthenticationToken(name, null);
		
		when(securityContext.getAuthentication()).thenReturn(a);
		securityContext.setAuthentication(a);
		SecurityContextHolder.setContext(securityContext);
		
		Session session = new Session();
		session.setAppkey(EncodingUtil.getEncrypted("appkey"));
		session.setSessionDl(EncodingUtil.getEncrypted("sessionDl"));
		
		Company company = new Company();
		company.setInn("123456789");
		company.setName("company");
		Set<Company> set = Collections.singleton(company);
		
		User user = new User();
		user.setEmail(name);
		user.setSession(session);
		user.setCompanies(set);
		
		Order orderEnt = new Order();
		orderEnt.setDocId("orderDocId_1");
		
		Document documentEnt = new Document();
		documentEnt.setId(1L);
		documentEnt.setUid("0x1");
		documentEnt.setOrder(orderEnt);
		documentEnt.setType(OrderDocType.BILL);
		
		List<Document> documentsEnt = Collections.singletonList(documentEnt);
		
		when(userService.getUser(anyString())).thenReturn(user);
		
		when(documentRepository.findByBase64NullAndOrder_Company(
				any(Company.class))).thenReturn(documentsEnt);
		
		IInterfaceManualLoad remoteData = mock(IInterfaceManualLoad.class);
		when(syncService.getRemoteData()).thenReturn(remoteData);
		
		@SuppressWarnings("unchecked")
		Call<DocumentResponse> call = (Call<DocumentResponse>) mock(Call.class);
		DocumentResponse expectedDocumentResponse = new DocumentResponse();
		Response<DocumentResponse> expectedResponse =
				Response.success(expectedDocumentResponse);
		DocumentResponse.Metadata metadata = new DocumentResponse.Metadata();
		metadata.setStatus(200);
		DocumentResponse.Data data = new DocumentResponse.Data();
		data.setBase64("base64");
		Collection<DocumentResponse.Data> responseDataCollection =
				Collections.singletonList(data);
		
		expectedDocumentResponse.setMetadata(metadata);
		expectedDocumentResponse.setData(responseDataCollection);
		
		when(remoteData.getPrintableDoc(any(DocumentRequest.class))).thenReturn(call);
		when(call.execute()).thenReturn(expectedResponse);
		
		documentService.update();
	}
	
	@Test
	public void extracted() throws IOException {
		
		String name = "test@test.com";
		Thread currentThread = Thread.currentThread();
		
		SecurityContext securityContext = mock(SecurityContext.class);
		Authentication a = new UsernamePasswordAuthenticationToken(name, null);
		
		lenient().when(securityContext.getAuthentication())
				.thenReturn(a);
		securityContext.setAuthentication(a);
		SecurityContextHolder.setContext(securityContext);
		
		Session session = new Session();
		session.setAppkey(EncodingUtil.getEncrypted("appkey"));
		session.setSessionDl(EncodingUtil.getEncrypted("sessionDl"));
		
		Company company = new Company();
		company.setInn("123456789");
		company.setName("company");
		Set<Company> set = Collections.singleton(company);
		
		User user = new User();
		user.setEmail(name);
		user.setSession(session);
		user.setCompanies(set);
		
		Order orderEnt = new Order();
		orderEnt.setDocId("orderDocId_1");
		
		Document documentEnt = new Document();
		documentEnt.setId(1L);
		documentEnt.setUid("0x1");
		documentEnt.setOrder(orderEnt);
		documentEnt.setType(OrderDocType.BILL);
		
		List<Document> documentsEnt = Collections.singletonList(documentEnt);
		
		lenient().when(userService.getUser(anyString()))
				.thenReturn(user);
		
		lenient().when(
						documentRepository.findByBase64NullAndOrder_Company(any(Company.class)))
				.thenReturn(documentsEnt);
		
		IInterfaceManualLoad remoteData = mock(IInterfaceManualLoad.class);
		when(syncService.getRemoteData()).thenReturn(remoteData);
		
		@SuppressWarnings("unchecked")
		Call<DocumentResponse> call = (Call<DocumentResponse>) mock(Call.class);
		DocumentResponse expectedDocumentResponse = new DocumentResponse();
		Response<DocumentResponse> expectedResponse =
				Response.success(expectedDocumentResponse);
		DocumentResponse.Metadata metadata = new DocumentResponse.Metadata();
		metadata.setStatus(200);
		DocumentResponse.Data data = new DocumentResponse.Data();
		data.setBase64("base64");
		Collection<DocumentResponse.Data> responseDataCollection =
				Collections.singletonList(data);
		
		expectedDocumentResponse.setMetadata(metadata);
		expectedDocumentResponse.setData(responseDataCollection);
		
		when(remoteData.getPrintableDoc(any(DocumentRequest.class))).thenReturn(call);
		when(call.execute()).thenReturn(expectedResponse);
		
		documentService.extracted(currentThread, 0, documentsEnt, user);
		
		assertNotNull(documentEnt.getBase64());
	}
	
	@Test
	public void extracted_interrupted() throws IOException {
		
		String name = "test@test.com";
		Thread currentThread = Thread.currentThread();
		
		SecurityContext securityContext = mock(SecurityContext.class);
		Authentication a = new UsernamePasswordAuthenticationToken(name, null);
		
		lenient().when(securityContext.getAuthentication())
				.thenReturn(a);
		securityContext.setAuthentication(a);
		SecurityContextHolder.setContext(securityContext);
		
		Session session = new Session();
		session.setAppkey(EncodingUtil.getEncrypted("appkey"));
		session.setSessionDl(EncodingUtil.getEncrypted("sessionDl"));
		
		Company company = new Company();
		company.setInn("123456789");
		company.setName("company");
		Set<Company> set = Collections.singleton(company);
		
		User user = new User();
		user.setEmail(name);
		user.setSession(session);
		user.setCompanies(set);
		
		Order orderEnt = new Order();
		orderEnt.setDocId("orderDocId_1");
		
		Document documentEnt = new Document();
		documentEnt.setId(1L);
		documentEnt.setUid("0x1");
		documentEnt.setOrder(orderEnt);
		documentEnt.setType(OrderDocType.BILL);
		
		List<Document> documentsEnt = Collections.singletonList(documentEnt);
		
		lenient().when(userService.getUser(anyString()))
				.thenReturn(user);
		
		lenient().when(
						documentRepository.findByBase64NullAndOrder_Company(any(Company.class)))
				.thenReturn(documentsEnt);
		
		IInterfaceManualLoad remoteData = mock(IInterfaceManualLoad.class);
		when(syncService.getRemoteData()).thenReturn(remoteData);
		
		@SuppressWarnings("unchecked")
		Call<DocumentResponse> call = (Call<DocumentResponse>) mock(Call.class);
		DocumentResponse expectedDocumentResponse = new DocumentResponse();
		Response<DocumentResponse> expectedResponse =
				Response.success(expectedDocumentResponse);
		DocumentResponse.Metadata metadata = new DocumentResponse.Metadata();
		metadata.setStatus(200);
		DocumentResponse.Data data = new DocumentResponse.Data();
		data.setBase64("base64");
		Collection<DocumentResponse.Data> responseDataCollection =
				Collections.singletonList(data);
		
		expectedDocumentResponse.setMetadata(metadata);
		expectedDocumentResponse.setData(responseDataCollection);
		
		when(remoteData.getPrintableDoc(any(DocumentRequest.class))).thenReturn(call);
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
		
		documentService.extracted(currentThread, 0, documentsEnt, user);
		
		assertThat(currentThread.isInterrupted(), is(true));
	}
	
	@Test
	public void extracted_responseBodyNull() throws IOException {
		
		String name = "test@test.com";
		Thread currentThread = Thread.currentThread();
		
		SecurityContext securityContext = mock(SecurityContext.class);
		Authentication a = new UsernamePasswordAuthenticationToken(name, null);
		
		lenient().when(securityContext.getAuthentication())
				.thenReturn(a);
		securityContext.setAuthentication(a);
		SecurityContextHolder.setContext(securityContext);
		
		Session session = new Session();
		session.setAppkey(EncodingUtil.getEncrypted("appkey"));
		session.setSessionDl(EncodingUtil.getEncrypted("sessionDl"));
		
		Company company = new Company();
		company.setInn("123456789");
		company.setName("company");
		Set<Company> set = Collections.singleton(company);
		
		User user = new User();
		user.setEmail(name);
		user.setSession(session);
		user.setCompanies(set);
		
		Order orderEnt = new Order();
		orderEnt.setDocId("orderDocId_1");
		
		Document documentEnt = new Document();
		documentEnt.setId(1L);
		documentEnt.setUid("0x1");
		documentEnt.setOrder(orderEnt);
		documentEnt.setType(OrderDocType.BILL);
		
		List<Document> documentsEnt = Collections.singletonList(documentEnt);
		
		lenient().when(userService.getUser(anyString()))
				.thenReturn(user);
		
		lenient().when(
						documentRepository.findByBase64NullAndOrder_Company(any(Company.class)))
				.thenReturn(documentsEnt);
		
		IInterfaceManualLoad remoteData = mock(IInterfaceManualLoad.class);
		when(syncService.getRemoteData()).thenReturn(remoteData);
		
		@SuppressWarnings("unchecked")
		Call<DocumentResponse> call = (Call<DocumentResponse>) mock(Call.class);
		@SuppressWarnings("unchecked")
		Response<DocumentResponse> expectedResponse = mock(Response.class);
		
		when(remoteData.getPrintableDoc(any(DocumentRequest.class))).thenReturn(call);
		when(call.execute()).thenReturn(expectedResponse);
		
		documentService.extracted(currentThread, 0, documentsEnt, user);
		
		verify(call, times(1)).execute();
		assertNull(call.execute().body());
	}
	
	@Test
	public void extracted_errorBody() throws IOException {
		
		String name = "test@test.com";
		Thread currentThread = Thread.currentThread();
		
		SecurityContext securityContext = mock(SecurityContext.class);
		Authentication a = new UsernamePasswordAuthenticationToken(name, null);
		
		lenient().when(securityContext.getAuthentication())
				.thenReturn(a);
		securityContext.setAuthentication(a);
		SecurityContextHolder.setContext(securityContext);
		
		Session session = new Session();
		session.setAppkey(EncodingUtil.getEncrypted("appkey"));
		session.setSessionDl(EncodingUtil.getEncrypted("sessionDl"));
		
		Company company = new Company();
		company.setInn("123456789");
		company.setName("company");
		Set<Company> set = Collections.singleton(company);
		
		User user = new User();
		user.setEmail(name);
		user.setSession(session);
		user.setCompanies(set);
		
		Order orderEnt = new Order();
		orderEnt.setDocId("orderDocId_1");
		
		Document documentEnt = new Document();
		documentEnt.setId(1L);
		documentEnt.setUid("0x1");
		documentEnt.setOrder(orderEnt);
		documentEnt.setType(OrderDocType.BILL);
		
		List<Document> documentsEnt = Collections.singletonList(documentEnt);
		
		lenient().when(userService.getUser(anyString()))
				.thenReturn(user);
		
		lenient().when(
						documentRepository.findByBase64NullAndOrder_Company(any(Company.class)))
				.thenReturn(documentsEnt);
		
		IInterfaceManualLoad remoteData = mock(IInterfaceManualLoad.class);
		when(syncService.getRemoteData()).thenReturn(remoteData);
		
		@SuppressWarnings("unchecked")
		Call<DocumentResponse> call = (Call<DocumentResponse>) mock(Call.class);
		
		Response<DocumentResponse> expectedResponse = Response.error(403,
				ResponseBody.create("{\"error\":[\"some_api_error_response\"]}",
						MediaType.parse("application/json")));
		
		when(remoteData.getPrintableDoc(any(DocumentRequest.class))).thenReturn(call);
		when(call.execute()).thenReturn(expectedResponse);
		
		documentService.extracted(currentThread, 0, documentsEnt, user);
		
		verify(call, times(1)).execute();
		assertNotNull(call.execute().errorBody());
	}
	
	@Test
	public void stopUpdate() {
		
		Thread thread = mock(Thread.class);
		
		documentService.setTaskThread(thread);
		documentService.setInitializedThread(true);
		
		documentService.stopUpdate();
		
		verify(thread, atLeastOnce()).interrupt();
	}
	
	@Test
	public void updateDocData() {
		
		DocumentResponse.Data data = new DocumentResponse.Data();
		data.setBase64("base64");
		Collection<DocumentResponse.Data> responseDataCollection =
				Collections.singletonList(data);
		
		Order orderEnt = new Order();
		orderEnt.setDocId("orderDocId_1");
		
		Document documentEnt = new Document();
		documentEnt.setId(1L);
		documentEnt.setUid("0x1");
		documentEnt.setOrder(orderEnt);
		documentEnt.setType(OrderDocType.BILL);
		
		documentService.updateDocData(documentEnt, responseDataCollection);
		assertEquals(data.getBase64(), documentEnt.getBase64());
		verify(documentRepository, atLeastOnce()).save(any(Document.class));
	}
	
	@Test
	public void updateDocData_typeGiveout() {
		
		Collection<String> urls = Collections.singletonList("document_url");
		
		DocumentResponse.Data data = new DocumentResponse.Data();
		data.setBase64("base64");
		data.setUrls(urls);
		Collection<DocumentResponse.Data> responseDataCollection =
				Collections.singletonList(data);
		
		Order orderEnt = new Order();
		orderEnt.setDocId("orderDocId_1");
		
		Document documentEnt = new Document();
		documentEnt.setId(1L);
		documentEnt.setUid("0x1");
		documentEnt.setOrder(orderEnt);
		documentEnt.setType(OrderDocType.GIVEOUT);
		
		documentService.updateDocData(documentEnt, responseDataCollection);
		assertEquals(data.getBase64(), documentEnt.getBase64());
		verify(documentRepository, atLeastOnce()).save(any(Document.class));
	}
	
	@Test
	public void getDocsByBase64Null() {
		
		Document document = new Document();
		document.setUid("0x1");
		List<Document> documents = Collections.singletonList(document);
		
		when(documentRepository.findByBase64Null()).thenReturn(documents);
		
		List<Document> result = documentService.getDocsByBase64Null();
		
		assertEquals(document.getUid(), result.get(0).getUid());
	}
	
	@Test
	public void getDocsByBase64NullAndCompanies() {
		
		Company companyOne = new Company();
		Company companyTwo = new Company();
		
		Order orderOne = new Order();
		orderOne.setCompany(companyOne);
		
		Order orderTwo = new Order();
		orderTwo.setCompany(companyTwo);
		
		Order orderThree = new Order();
		orderThree.setCompany(companyOne);
		
		Document documentOne = new Document();
		documentOne.setBase64("base64");
		documentOne.setOrder(orderOne);
		Document documentTwo = new Document();
		documentTwo.setUid("0x1");
		documentTwo.setOrder(orderTwo);
		
		Collection<Document> documents = Collections.singletonList(documentTwo);
		Collection<Company> companies = Collections.singletonList(companyTwo);
		
		when(documentRepository.findByBase64NullAndOrder_Company(companyTwo)).thenReturn(
				documents);
		
		List<Document> result =
				documentService.getDocsByBase64NullAndCompanies(companies);
		
		assertEquals(documentTwo.getUid(), result.get(0).getUid());
	}
}
