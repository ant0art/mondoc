package com.dellin.mondoc.controllers;

import com.dellin.mondoc.model.dto.UserDTO;
import com.dellin.mondoc.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
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

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "The user API. Contains all operations that can be "
		+ "performed on a user")
@SecurityScheme(type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT",
				name = "Authorization")
public class UserController {
	
	private final UserService userService;
	
	@PostMapping("/save")
	@Operation(summary = "Create a user",
			   security = @SecurityRequirement(name = "Authorization"))
	public ResponseEntity<UserDTO> create(@RequestBody UserDTO userDTO) {
		URI uri = URI.create(
				ServletUriComponentsBuilder.fromCurrentContextPath().toUriString());
		return ResponseEntity.created(uri).body(userService.create(userDTO));
	}
	
	@GetMapping("/get")
	@Operation(summary = "Get a user",
			   security = @SecurityRequirement(name = "Authorization"))
	public ResponseEntity<UserDTO> read(@RequestParam String email) {
		return ResponseEntity.ok(userService.get(email));
	}
	
	@PutMapping("/update")
	@Operation(summary = "Update user",
			   security = @SecurityRequirement(name = "Authorization"))
	public ResponseEntity<UserDTO> update(@RequestParam String email,
			@RequestBody UserDTO userDTO) {
		return ResponseEntity.ok(userService.update(email, userDTO));
	}
	
	@DeleteMapping("/delete")
	@Operation(summary = "Remove user",
			   security = @SecurityRequirement(name = "Authorization"))
	public ResponseEntity<HttpStatus> delete(@RequestParam String email) {
		userService.delete(email);
		return ResponseEntity.ok().build();
	}
	
	@GetMapping("/all")
	@Operation(summary = "Get all users",
			   security = @SecurityRequirement(name = "Authorization"))
	public ModelMap getUsers(
			@RequestParam(required = false, defaultValue = "1") Integer page,
			@RequestParam(required = false, defaultValue = "1") Integer perPage,
			@RequestParam(required = false, defaultValue = "name") String sort,
			@RequestParam(required = false, defaultValue = "ASC") Sort.Direction order) {
		return userService.getUsers(page, perPage, sort, order);
	}
}
