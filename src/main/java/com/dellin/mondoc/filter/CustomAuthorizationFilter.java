package com.dellin.mondoc.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.dellin.mondoc.controllers.AuthController;
import com.dellin.mondoc.utils.EncodingUtil;
import java.io.*;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.ArrayList;
import java.util.*;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static java.util.Arrays.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Slf4j
public class CustomAuthorizationFilter extends OncePerRequestFilter {
	
	@Override
	protected void doFilterInternal(HttpServletRequest request,
			@NotNull HttpServletResponse response,
			@NotNull FilterChain filterChain) throws ServletException, IOException {
		if (request.getServletPath().equals("/api/login") || request.getServletPath()
																	.equals("/api/token/refresh")) {
			filterChain.doFilter(request, response);
		} else {
			String authorizationHeader = request.getHeader(AUTHORIZATION);
			if (authorizationHeader != null && authorizationHeader.startsWith(
					"Bearer ")) {
				try {
					String secret = System.getenv("secret");
					String token = EncodingUtil.getRefreshToken(authorizationHeader);
					JWTVerifier jwtVerifier =
							JWT.require(EncodingUtil.getAlgorithm(secret)).build();
					DecodedJWT decodedJWT = jwtVerifier.verify(token);
					String email = decodedJWT.getSubject();
					String[] roles = decodedJWT.getClaim("roles").asArray(String.class);
					Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
					stream(roles).forEach(
							role -> authorities.add(new SimpleGrantedAuthority(role)));
					
					UsernamePasswordAuthenticationToken authenticationToken =
							new UsernamePasswordAuthenticationToken(email, null,
									authorities);
					SecurityContextHolder.getContext()
										 .setAuthentication(authenticationToken);
					filterChain.doFilter(request, response);
				} catch (Exception e) {
					log.error("Some error: " + e.getMessage());
					AuthController.sendAuthError(response, e);
				}
			} else {
				filterChain.doFilter(request, response);
			}
		}
	}
}
