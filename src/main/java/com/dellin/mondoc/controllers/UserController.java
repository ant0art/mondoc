package com.dellin.mondoc.controllers;

import com.auth0.jwt.JWT;
import com.dellin.mondoc.model.dto.UserDTO;
import com.dellin.mondoc.model.entity.Role;
import com.dellin.mondoc.model.entity.User;
import com.dellin.mondoc.service.UserService;
import com.dellin.mondoc.utils.EncodingUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.*;
import java.util.stream.*;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.FORBIDDEN;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@PostMapping("/user/save")
	public ResponseEntity<UserDTO> create(@RequestBody UserDTO userDTO) {
		URI uri = URI.create(
				ServletUriComponentsBuilder.fromCurrentContextPath().toUriString());
		return ResponseEntity.created(uri).body(userService.create(userDTO));
	}

	@GetMapping("/user/get")
	public ResponseEntity<UserDTO> read(@RequestParam String email) {
		return ResponseEntity.ok(userService.get(email));
	}

	@PutMapping("/user/update")
	public ResponseEntity<UserDTO> update(@RequestParam String email,
			@RequestBody UserDTO userDTO) {
		return ResponseEntity.ok(userService.update(email, userDTO));
	}

	@DeleteMapping("/user/delete")
	public ResponseEntity<HttpStatus> delete(@RequestParam String email) {
		userService.delete(email);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/users")
	public ModelMap getUsers(
			@RequestParam(required = false, defaultValue = "1") Integer page,
			@RequestParam(required = false, defaultValue = "1") Integer perPage,
			@RequestParam(required = false, defaultValue = "name") String sort,
			@RequestParam(required = false, defaultValue = "ASC") Sort.Direction order) {
		return userService.getUsers(page, perPage, sort, order);
	}

	@GetMapping("/token/refresh")
	public void refreshToken(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		String authorizationHeader = request.getHeader(AUTHORIZATION);
		if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
			try {
				String secret = System.getenv("secret");

				String refreshToken = EncodingUtil.getRefreshToken(authorizationHeader);
				User user = userService.getUser(
						EncodingUtil.getDecodedUsername(secret, authorizationHeader));
				String accessToken = JWT.create().withSubject(user.getUsername())
						.withExpiresAt(
								new Date(System.currentTimeMillis() + 60 * 60 * 1000))
						.withIssuer(request.getRequestURL().toString()).withClaim("roles",
								user.getRoles().stream().map(Role::getRoleName)
										.collect(Collectors.toList()))
						.sign(EncodingUtil.getAlgorithm(secret));

				Map<String, String> tokens = new HashMap<>();
				tokens.put("access_token", accessToken);
				tokens.put("refresh_token", refreshToken);
				response.setContentType(MediaType.APPLICATION_JSON_VALUE);
				new ObjectMapper().writeValue(response.getOutputStream(), tokens);
			} catch (Exception e) {
				response.setHeader("error", e.getMessage());
				response.setStatus(FORBIDDEN.value());
				Map<String, String> error = new HashMap<>();
				error.put("error_message", e.getMessage());
				response.setContentType(MediaType.APPLICATION_JSON_VALUE);
				new ObjectMapper().writeValue(response.getOutputStream(), error);
			}
		} else {
			throw new RuntimeException("Refresh token is missing");
		}
	}
}
