package com.dellin.mondoc.controllers;

import com.dellin.mondoc.model.pojo.AuthDellin;
import com.dellin.mondoc.model.pojo.UserLoginRequest;
import com.dellin.mondoc.service.AuthDellinService;
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
@RequestMapping("/dellin")
@RequiredArgsConstructor
public class AuthDellinController {
	
	private final AuthDellinService authDellinService;
	
	@PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<AuthDellin> login(
			@RequestBody UserLoginRequest loginRequest) throws IOException {
		
		URI uri = URI.create(
				ServletUriComponentsBuilder.fromCurrentContextPath().toUriString());
		AuthDellin loginResponse = authDellinService.getLoginResponse();
		
		return ResponseEntity.created(uri).body(loginResponse);
	}
	
}
