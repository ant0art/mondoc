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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Service class to work with Sessions
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SessionServiceImpl implements SessionService {
	
	/**
	 * Repository which contains users
	 */
	private final UserRepository userRepository;
	/**
	 * ObjectMapper for reading and writing JSON
	 */
	private final ObjectMapper mapper =
			JsonMapper.builder().addModule(new JavaTimeModule()).build();
	/**
	 * Injection of Retrofit service requests
	 */
	private final SyncService syncService;
	/**
	 * The field APPKEY set up by environment var
	 */
	@Value("${api.appkey}")
	String APPKEY = "";
	
	/**
	 * Method that login User in API Dellin
	 * <p>
	 * Method connects to the Dellin API by a request that contains the following required
	 * parameters of Dellin account: appkey, login, password.
	 * <p>
	 * Received response contains information about new Dellin Api session for current
	 * account. The session and other passed parameters are encrypted and written to the
	 * database
	 * <p>
	 * Login process is possible only for authorized users, since any change is recorded
	 * in the history. Successful response changes the status of current Session to
	 * {@link EntityStatus#CREATED} or {@link EntityStatus#UPDATED}, that depends on
	 * previous state
	 *
	 * @param sessionDTO the {@link SessionDTO} object for creating API request
	 *
	 * @return the {@link AuthDellin} response
	 *
	 * @throws IOException by Retrofit method with synchronized {@link Call#execute()} if
	 *                     a problem occurred talking to the server
	 * @see IInterfaceManualLoad#login(SessionDTO)
	 */
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
	
	/**
	 * Method that logout User from API Dellin
	 * <p>
	 * Method connects to the Dellin API by a request that contains the following required
	 * parameters: appkey, current Dellin session.
	 * <p>
	 * Logout process is possible only for authorized users, since any change is recorded
	 * in the history. Successful response changes the status of current Session to
	 * {@link EntityStatus#DELETED}
	 *
	 * @return the {@link AuthDellin} with logout API status
	 *
	 * @throws IOException by Retrofit method with synchronized {@link Call#execute()} if
	 *                     a problem occurred talking to the server
	 */
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
	
	/**
	 * Change the state of {@link Session}-entity by chosen and set up the entity field
	 * updatedAt new local date time
	 *
	 * @param session the {@link Session} object
	 * @param status  the {@link EntityStatus} enum
	 */
	private void updateStatus(Session session, EntityStatus status) {
		session.setStatus(status);
		session.setUpdatedAt(LocalDateTime.now());
	}
}
