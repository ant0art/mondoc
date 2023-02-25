package com.dellin.mondoc.controllers;

import com.dellin.mondoc.model.dto.SessionDTO;
import com.dellin.mondoc.model.pojo.AuthDellin;
import com.dellin.mondoc.service.SessionService;
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
@RequestMapping("/session")
@RequiredArgsConstructor
public class SessionController {
	
	private final SessionService sessionService;
	
	@PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<AuthDellin> login(
			@RequestBody(required = false) SessionDTO sessionDTO) throws IOException {
		
		URI uri = URI.create(
				ServletUriComponentsBuilder.fromCurrentContextPath().toUriString());
		AuthDellin loginResponse = sessionService.getLoginResponse(sessionDTO);
		
		return ResponseEntity.created(uri).body(loginResponse);
	}
	
	@PostMapping(value = "/logout")
	public ResponseEntity<AuthDellin> logout() throws IOException {
		URI uri = URI.create(
				ServletUriComponentsBuilder.fromCurrentContextPath().toUriString());
		AuthDellin logoutResponse = sessionService.getLogoutResponse();
		
		return ResponseEntity.created(uri).body(logoutResponse);
	}
}
