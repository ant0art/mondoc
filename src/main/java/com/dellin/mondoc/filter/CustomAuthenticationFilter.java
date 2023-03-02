package com.dellin.mondoc.filter;

import com.dellin.mondoc.controllers.AuthController;
import java.io.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
	
	private final AuthenticationManager manager;
	
	public CustomAuthenticationFilter(AuthenticationManager manager) {
		this.manager = manager;
	}
	
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request,
			HttpServletResponse response) throws AuthenticationException {
		String email = request.getParameter("username");
		String password = request.getParameter("password");
		log.info("User [email: {}] started authentication", email);
		
		UsernamePasswordAuthenticationToken token =
				new UsernamePasswordAuthenticationToken(email, password);
		return manager.authenticate(token);
	}
	
	@Override
	protected void successfulAuthentication(HttpServletRequest request,
			HttpServletResponse response, FilterChain chain,
			Authentication authentication) throws IOException {
		User user = (User) authentication.getPrincipal();
		AuthController.getTokensJson(request, response, user);
	}
}
