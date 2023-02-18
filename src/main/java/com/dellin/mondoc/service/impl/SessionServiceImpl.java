package com.dellin.mondoc.service.impl;

import com.dellin.mondoc.exceptions.CustomException;
import com.dellin.mondoc.model.dto.SessionDTO;
import com.dellin.mondoc.model.entity.Session;
import com.dellin.mondoc.model.entity.User;
import com.dellin.mondoc.model.enums.EntityStatus;
import com.dellin.mondoc.model.pojo.AuthDellin;
import com.dellin.mondoc.model.repository.SessionRepository;
import com.dellin.mondoc.model.repository.UserRepository;
import com.dellin.mondoc.service.IInterfaceManualLoad;
import com.dellin.mondoc.service.SessionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.*;
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
public class SessionServiceImpl implements SessionService {
	
	private final UserRepository userRepository;
	
	private final SessionRepository sessionRepository;
	
	private final ObjectMapper mapper = JsonMapper.builder().addModule(
			new JavaTimeModule()).build();
	
	@Value("${api.address}")
	private String baseUrlFid;
	
	private IInterfaceManualLoad iInterfaceManualLoad;
	
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
	
	@Override
	public AuthDellin getLoginResponse(String email, SessionDTO sessionDTO) throws
			IOException {
		
		User user = userRepository.findByEmail(email).orElseThrow(
				() -> new CustomException(
						String.format("User with email: %s not found", email),
						HttpStatus.NOT_FOUND));
		
		Call<AuthDellin> login = getRemoteData().login(email, sessionDTO);
		Response<AuthDellin> response = login.execute();
		
		if (!response.isSuccessful()) {
			throw new IOException(
					response.errorBody() != null ? response.errorBody().string()
							: "Unknown error");
		}
		
		sessionDTO.setSessionDl(response.body().getData().getSessionID());
		
		Session session = mapper.convertValue(sessionDTO, Session.class);
		session.setUser(user);
		user.setSession(session);
		session.setState(EntityStatus.CREATED);
		log.info("Session: {}  created", session.getSessionDl());
		
		userRepository.save(user);
		
		return response.body();
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
			
			builder.readTimeout(240, TimeUnit.SECONDS);
			builder.connectTimeout(240, TimeUnit.SECONDS);
			builder.writeTimeout(240, TimeUnit.SECONDS);
			
			//   builder.sslSocketFactory(sslSocketFactory);
			builder.hostnameVerifier(new HostnameVerifier() {
				public boolean verify(String s, SSLSession sslSession) {
					return true;
				}
			});
			
			OkHttpClient okHttpClient = builder.build();
			return okHttpClient;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	//	@Override
	//	public RoleDTO get(String roleName) {
	//		Role role = getRole(roleName);
	//		return mapper.convertValue(role, RoleDTO.class);
	//	}
	//
	//	@Override
	//	public void addRoleToUser(String email, String roleName) {
	//		log.info("Adding to user with email: {} a role {}", email, roleName);
	//		User user = userService.getUser(email);
	//
	//		Role role = getRole(roleName);
	//
	//		if (user.getRoles().stream().anyMatch(r -> r.equals(role))) {
	//			throw new CustomException(
	//					String.format("User with email: %s already has a role: %s", email,
	//							roleName), HttpStatus.BAD_REQUEST);
	//		}
	//		user.getRoles().add(role);
	//		userService.updateStatus(user, EntityStatus.UPDATED);
	//		role.getUsers().add(user);
	//		updateStatus(role, EntityStatus.UPDATED);
	//		userRepository.save(user);
	//	}
	//
	//	public Role getRole(String roleName) {
	//		return roleRepository.findByRoleName(roleName).orElseThrow(
	//				() -> new CustomException(
	//						String.format("Role with name: %s not found", roleName),
	//						HttpStatus.NOT_FOUND));
	//	}
	
	private void updateStatus(Session session, EntityStatus status) {
		session.setState(status);
		session.setUpdatedAt(LocalDateTime.now());
	}
}
