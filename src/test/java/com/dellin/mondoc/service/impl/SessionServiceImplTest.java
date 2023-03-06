package com.dellin.mondoc.service.impl;

import com.dellin.mondoc.exceptions.CustomException;
import com.dellin.mondoc.model.dto.SessionDTO;
import com.dellin.mondoc.model.entity.Session;
import com.dellin.mondoc.model.entity.User;
import com.dellin.mondoc.model.pojo.AuthDellin;
import com.dellin.mondoc.model.repository.UserRepository;
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

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SessionServiceImplTest {
	
	@InjectMocks
	private SessionServiceImpl sessionService;
	@Mock
	private UserRepository userRepository;
	@Mock
	private SyncService syncService;
	
	@Test
	public void getLoginResponse() throws IOException {
		
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
		when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
		
		SessionDTO sessionDTO = new SessionDTO();
		sessionDTO.setAppkey("appkey");
		sessionDTO.setLogin("login");
		sessionDTO.setPassword("pass");
		
		IInterfaceManualLoad remoteData = mock(IInterfaceManualLoad.class);
		when(syncService.getRemoteData()).thenReturn(remoteData);
		@SuppressWarnings("unchecked")
		Call<AuthDellin> call = (Call<AuthDellin>) mock(Call.class);
		when(remoteData.login(any(SessionDTO.class))).thenReturn(call);
		
		AuthDellin expectedAuthResponse = new AuthDellin();
		AuthDellin.Data data = new AuthDellin.Data();
		data.setSessionID("sessionId_1");
		expectedAuthResponse.setData(data);
		
		Response<AuthDellin> expectedResponse = Response.success(expectedAuthResponse);
		
		when(call.execute()).thenReturn(expectedResponse);
		
		when(userRepository.save(user)).thenAnswer(i -> i.getArguments()[0]);
		
		AuthDellin resultAuth = sessionService.getLoginResponse(sessionDTO);
		
		verify(userRepository, atLeastOnce()).save(user);
		assertEquals(EncodingUtil.getDecrypted(user.getSession().getSessionDl()),
				resultAuth.getData().getSessionID());
	}
	
	@Test(expected = CustomException.class)
	public void getLoginResponse_userNotFound() throws IOException {
		
		String name = "test@test.com";
		
		SecurityContext securityContext = mock(SecurityContext.class);
		Authentication a = new UsernamePasswordAuthenticationToken(name, null);
		
		when(securityContext.getAuthentication()).thenReturn(a);
		securityContext.setAuthentication(a);
		SecurityContextHolder.setContext(securityContext);
		
		sessionService.getLoginResponse(new SessionDTO());
	}
	
	@Test(expected = IOException.class)
	public void getLoginResponse_responseBodyNull() throws IOException {
		
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
		when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
		
		SessionDTO sessionDTO = new SessionDTO();
		sessionDTO.setAppkey("appkey");
		sessionDTO.setLogin("login");
		sessionDTO.setPassword("pass");
		
		IInterfaceManualLoad remoteData = mock(IInterfaceManualLoad.class);
		when(syncService.getRemoteData()).thenReturn(remoteData);
		@SuppressWarnings("unchecked")
		Call<AuthDellin> call = (Call<AuthDellin>) mock(Call.class);
		when(remoteData.login(any(SessionDTO.class))).thenReturn(call);
		
		AuthDellin expectedAuthResponse = new AuthDellin();
		AuthDellin.Data data = new AuthDellin.Data();
		data.setSessionID("sessionId_1");
		expectedAuthResponse.setData(data);
		
		@SuppressWarnings("unchecked")
		Response<AuthDellin> expectedResponse = mock(Response.class);
		
		when(call.execute()).thenReturn(expectedResponse);
		
		sessionService.getLoginResponse(sessionDTO);
		
		verify(call, times(1)).execute();
		assertNull(call.execute().body());
	}
	
	@Test
	public void getLoginResponse_userSessionNull() throws IOException {
		
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
		when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
		
		SessionDTO sessionDTO = new SessionDTO();
		sessionDTO.setAppkey("appkey");
		sessionDTO.setLogin("login");
		sessionDTO.setPassword("pass");
		
		IInterfaceManualLoad remoteData = mock(IInterfaceManualLoad.class);
		when(syncService.getRemoteData()).thenReturn(remoteData);
		@SuppressWarnings("unchecked")
		Call<AuthDellin> call = (Call<AuthDellin>) mock(Call.class);
		when(remoteData.login(any(SessionDTO.class))).thenReturn(call);
		
		AuthDellin expectedAuthResponse = new AuthDellin();
		AuthDellin.Data data = new AuthDellin.Data();
		data.setSessionID("sessionId_1");
		expectedAuthResponse.setData(data);
		
		Response<AuthDellin> expectedResponse = Response.success(expectedAuthResponse);
		
		when(call.execute()).thenReturn(expectedResponse);
		
		when(userRepository.save(user)).thenAnswer(i -> i.getArguments()[0]);
		
		AuthDellin resultAuth = sessionService.getLoginResponse(sessionDTO);
		
		verify(userRepository, atLeastOnce()).save(user);
		assertEquals(EncodingUtil.getDecrypted(user.getSession().getSessionDl()),
				resultAuth.getData().getSessionID());
	}
	
	@Test
	public void getLogoutResponse() throws IOException {
		
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
		when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
		
		IInterfaceManualLoad remoteData = mock(IInterfaceManualLoad.class);
		when(syncService.getRemoteData()).thenReturn(remoteData);
		@SuppressWarnings("unchecked")
		Call<AuthDellin> call = (Call<AuthDellin>) mock(Call.class);
		when(remoteData.logout(any(SessionDTO.class))).thenReturn(call);
		
		AuthDellin expectedAuthResponse = new AuthDellin();
		AuthDellin.Data data = new AuthDellin.Data();
		data.setState("success");
		expectedAuthResponse.setData(data);
		
		Response<AuthDellin> expectedResponse = Response.success(expectedAuthResponse);
		
		when(call.execute()).thenReturn(expectedResponse);
		
		when(userRepository.save(user)).thenAnswer(i -> i.getArguments()[0]);
		
		AuthDellin resultAuth = sessionService.getLogoutResponse();
		
		verify(userRepository, atLeastOnce()).save(user);
		assertEquals("success", resultAuth.getData().getState());
	}
	
	@Test(expected = CustomException.class)
	public void getLogoutResponse_userNotFound() throws IOException {
		
		String name = "test@test.com";
		
		SecurityContext securityContext = mock(SecurityContext.class);
		Authentication a = new UsernamePasswordAuthenticationToken(name, null);
		
		when(securityContext.getAuthentication()).thenReturn(a);
		securityContext.setAuthentication(a);
		SecurityContextHolder.setContext(securityContext);
		
		sessionService.getLogoutResponse();
	}
	
	@Test(expected = IOException.class)
	public void getLogoutResponse_responseBodyNull() throws IOException {
		
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
		when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
		
		IInterfaceManualLoad remoteData = mock(IInterfaceManualLoad.class);
		when(syncService.getRemoteData()).thenReturn(remoteData);
		@SuppressWarnings("unchecked")
		Call<AuthDellin> call = (Call<AuthDellin>) mock(Call.class);
		when(remoteData.logout(any(SessionDTO.class))).thenReturn(call);
		
		@SuppressWarnings("unchecked")
		Response<AuthDellin> expectedResponse = mock(Response.class);
		
		when(call.execute()).thenReturn(expectedResponse);
		
		sessionService.getLogoutResponse();
		
		verify(call, times(1)).execute();
		assertNull(call.execute().body());
	}
}
