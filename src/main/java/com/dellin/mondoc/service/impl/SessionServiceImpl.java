package com.dellin.mondoc.service.impl;

import com.dellin.mondoc.exceptions.CustomException;
import com.dellin.mondoc.model.dto.SessionDTO;
import com.dellin.mondoc.model.entity.Session;
import com.dellin.mondoc.model.entity.User;
import com.dellin.mondoc.model.enums.EntityStatus;
import com.dellin.mondoc.model.pojo.AuthDellin;
import com.dellin.mondoc.model.repository.UserRepository;
import com.dellin.mondoc.service.SessionService;
import com.dellin.mondoc.utils.EncodingUtil;
import com.dellin.mondoc.utils.PropertiesUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.*;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import retrofit2.Call;
import retrofit2.Response;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SessionServiceImpl implements SessionService {
	
	private final UserRepository userRepository;
	
	private final String APPKEY = System.getenv("appkey");
	
	private final ObjectMapper mapper =
			JsonMapper.builder().addModule(new JavaTimeModule()).build();
	
	private final SyncService syncService;
	
	@Override
	public AuthDellin getLoginResponse(SessionDTO sessionDTO) throws IOException {
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		
		User user = userRepository.findByEmail(email).orElseThrow(
				() -> new CustomException(
						String.format("User with email: %s not found", email),
						HttpStatus.NOT_FOUND));
		
		sessionDTO.setAppkey(APPKEY);
		
		Call<AuthDellin> login = syncService.getRemoteData().login(sessionDTO);
		Response<AuthDellin> response = login.execute();
		
		if (!response.isSuccessful()) {
			throw new IOException(
					response.errorBody() != null ? response.errorBody().string()
							: "Unknown error");
		}
		
		sessionDTO.setPassword(EncodingUtil.getEncrypted(sessionDTO.getPassword()));
		sessionDTO.setLogin(EncodingUtil.getEncrypted(sessionDTO.getLogin()));
		sessionDTO.setAppkey(EncodingUtil.getEncrypted(sessionDTO.getAppkey()));
		sessionDTO.setSessionDl(
				EncodingUtil.getEncrypted(response.body().getData().getSessionID()));
		Session session = mapper.convertValue(sessionDTO, Session.class);
		Session userSession = user.getSession();
		
		if (userSession == null) {
			session.setStatus(EntityStatus.CREATED);
			log.info("Session: {}  created", session.getSessionDl());
			user.setSession(session);
			session.setUser(user);
		} else {
			PropertiesUtil.copyPropertiesIgnoreNull(session, userSession);
			updateStatus(userSession, EntityStatus.UPDATED);
			log.info("Session: {}  updated", userSession.getSessionDl());
			user.setSession(userSession);
			userSession.setUser(user);
		}
		
		userRepository.save(user);
		
		return response.body();
	}
	
	@Override
	public AuthDellin getLogoutResponse() throws IOException {
		
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		log.info("User with email: {} starts logout", email);
		
		User user = userRepository.findByEmail(email).orElseThrow(
				() -> new CustomException(
						String.format("User with email: %s not found", email),
						HttpStatus.NOT_FOUND));
		
		Session session = user.getSession();
		SessionDTO sessionDTO = mapper.convertValue(session, SessionDTO.class);
		
		sessionDTO.setAppkey(EncodingUtil.getDecrypted(sessionDTO.getAppkey()));
		sessionDTO.setSessionDl(EncodingUtil.getDecrypted(sessionDTO.getSessionDl()));
		
		Call<AuthDellin> logout = syncService.getRemoteData().logout(sessionDTO);
		Response<AuthDellin> response = logout.execute();
		
		if (!response.isSuccessful()) {
			throw new IOException(
					response.errorBody() != null ? response.errorBody().string()
							: "Unknown error");
		}
		
		updateStatus(session, EntityStatus.DELETED);
		session.setUser(user);
		user.setSession(session);
		userRepository.save(user);
		
		log.info("User with email: {} successfully logout", email);
		
		return response.body();
	}
	
	private void updateStatus(Session session, EntityStatus status) {
		session.setStatus(status);
		session.setUpdatedAt(LocalDateTime.now());
	}
}
