package com.dellin.mondoc.controllers;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.dellin.mondoc.model.entity.Role;
import com.dellin.mondoc.model.entity.User;
import com.dellin.mondoc.service.UserService;
import com.dellin.mondoc.utils.EncodingUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.FORBIDDEN;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Contains operations of users authentication")
@SecurityScheme(type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT",
				name = "Authorization")
public class AuthController {
	
	private final UserService userService;
	
	private final AuthenticationManager manager;
	
	public static void sendAuthError(HttpServletResponse response, Exception e) throws
			IOException {
		response.setHeader("error", e.getMessage());
		response.setStatus(FORBIDDEN.value());
		Map<String, String> error = new HashMap<>();
		error.put("error_message", e.getMessage());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		new ObjectMapper().writeValue(response.getOutputStream(), error);
	}
	
	public static void getTokensJson(HttpServletRequest request,
			HttpServletResponse response,
			org.springframework.security.core.userdetails.User user) throws IOException {
		String secret = System.getenv("secret");
		Algorithm algorithm = EncodingUtil.getAlgorithm(secret);
		
		String accessToken = JWT.create()
				.withSubject(user.getUsername())
				.withExpiresAt(new Date(System.currentTimeMillis() + 120 * 60 * 1000))
				.withIssuer(request.getRequestURL().toString())
				.withClaim("roles", user.getAuthorities()
						.stream()
						.map(GrantedAuthority::getAuthority)
						.collect(Collectors.toList())).sign(algorithm);
		
		String refreshToken = JWT.create()
				.withSubject(user.getUsername())
				.withExpiresAt(new Date(System.currentTimeMillis() + 180 * 60 * 1000))
				.withIssuer(request.getRequestURL().toString()).sign(algorithm);
		
		Map<String, String> tokens = new HashMap<>();
		tokens.put("access_token", accessToken);
		tokens.put("refresh_token", refreshToken);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		new ObjectMapper().writeValue(response.getOutputStream(), tokens);
	}
	
	@GetMapping("/token/refresh")
	@Operation(summary = "Refresh token",
			   security = @SecurityRequirement(name = AUTHORIZATION))
	public void refreshToken(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		String authorizationHeader = request.getHeader(AUTHORIZATION);
		if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
			try {
				String secret = System.getenv("secret");
				
				String refreshToken = EncodingUtil.getRefreshToken(authorizationHeader);
				User user = userService.getUser(
						EncodingUtil.getDecodedUsername(secret, authorizationHeader));
				String accessToken = JWT.create()
						.withSubject(user.getUsername())
						.withExpiresAt(
								new Date(System.currentTimeMillis() + 120 * 60 * 1000))
						.withIssuer(request.getRequestURL().toString())
						.withClaim("roles", user.getRoles()
								.stream()
								.map(Role::getRoleName)
								.collect(Collectors.toList()))
						.sign(EncodingUtil.getAlgorithm(secret));
				
				Map<String, String> tokens = new HashMap<>();
				tokens.put("access_token", accessToken);
				tokens.put("refresh_token", refreshToken);
				response.setContentType(MediaType.APPLICATION_JSON_VALUE);
				new ObjectMapper().writeValue(response.getOutputStream(), tokens);
			} catch (Exception e) {
				sendAuthError(response, e);
			}
		} else {
			throw new RuntimeException("Refresh token is missing");
		}
	}
	
	@PostMapping(value = "/login", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	@Operation(summary = "User authentication")
	public void login(@RequestBody AuthenticationForm form, HttpServletRequest request,
			HttpServletResponse response, Authentication authentication) throws
			ServletException, IOException {
		
		UsernamePasswordAuthenticationToken token =
				new UsernamePasswordAuthenticationToken(form.getUsername(),
						form.getPassword());
		
		manager.authenticate(token);
		
		org.springframework.security.core.userdetails.User user =
				(org.springframework.security.core.userdetails.User) authentication.getPrincipal();
		getTokensJson(request, response, user);
	}
}

@Data
class AuthenticationForm {
	
	@NotEmpty(message = "Email should not be empty")
	@Email
	private String username;
	@NotEmpty(message = "Password should not be empty")
	private String password;
}
