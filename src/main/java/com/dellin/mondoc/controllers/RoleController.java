package com.dellin.mondoc.controllers;

import com.dellin.mondoc.model.dto.RoleDTO;
import com.dellin.mondoc.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
@Tag(name = "Roles", description = "The role API. Contains operations to work "
		+ "with roles like add new one or add it to definite user")
@SecurityScheme(type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT",
				name = "Authorization")
public class RoleController {
	
	private final RoleService roleService;
	
	@PostMapping("/save")
	@Operation(summary = "Create a role",
			   security = @SecurityRequirement(name = "Authorization"))
	public ResponseEntity<RoleDTO> create(@RequestBody RoleDTO roleDTO) {
		URI uri = URI.create(
				ServletUriComponentsBuilder.fromCurrentContextPath().toUriString());
		return ResponseEntity.created(uri).body(roleService.create(roleDTO));
	}
	
	@PostMapping("/addToUser")
	@Operation(summary = "Add a role to user",
			   security = @SecurityRequirement(name = "Authorization"))
	public ResponseEntity<?> addToUser(@RequestBody RoleToUserForm form) {
		roleService.addRoleToUser(form.getEmail(), form.getRoleName());
		return ResponseEntity.ok().build();
	}
}

@Data
class RoleToUserForm {
	
	private String email;
	private String roleName;
}
