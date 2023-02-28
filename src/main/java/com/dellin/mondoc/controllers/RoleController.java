package com.dellin.mondoc.controllers;

import com.dellin.mondoc.model.dto.RoleDTO;
import com.dellin.mondoc.service.RoleService;
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
public class RoleController {
	
	private final RoleService roleService;
	
	@PostMapping("/save")
	public ResponseEntity<RoleDTO> create(@RequestBody RoleDTO roleDTO) {
		URI uri = URI.create(
				ServletUriComponentsBuilder.fromCurrentContextPath().toUriString());
		return ResponseEntity.created(uri).body(roleService.create(roleDTO));
	}
	
	@PostMapping("/addToUser")
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
