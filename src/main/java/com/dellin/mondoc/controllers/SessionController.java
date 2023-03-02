package com.dellin.mondoc.controllers;

import com.dellin.mondoc.model.dto.SessionDTO;
import com.dellin.mondoc.model.pojo.AuthDellin;
import com.dellin.mondoc.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.*;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/sessions")
@RequiredArgsConstructor
@Tag(name = "Sessions", description = "The session API. Contains operations to work "
		+ "with sessions of third-side API like login or logout")
@SecurityScheme(type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT",
				name = "Authorization")
public class SessionController {
	
	private final SessionService sessionService;
	
	@PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Login to API",
			   security = @SecurityRequirement(name = "Authorization"))
	public ResponseEntity<AuthDellin> login(
			@RequestBody(required = false) SessionDTO sessionDTO) throws IOException {
		
		URI uri = URI.create(
				ServletUriComponentsBuilder.fromCurrentContextPath().toUriString());
		AuthDellin loginResponse = sessionService.getLoginResponse(sessionDTO);
		
		return ResponseEntity.created(uri).body(loginResponse);
	}
	
	@PostMapping(value = "/logout")
	@Operation(summary = "Logout process",
			   security = @SecurityRequirement(name = "Authorization"))
	public ResponseEntity<AuthDellin> logout() throws IOException {
		URI uri = URI.create(
				ServletUriComponentsBuilder.fromCurrentContextPath().toUriString());
		AuthDellin logoutResponse = sessionService.getLogoutResponse();
		
		return ResponseEntity.created(uri).body(logoutResponse);
	}
}
