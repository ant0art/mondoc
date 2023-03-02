package com.dellin.mondoc.controllers;

import com.dellin.mondoc.model.dto.CompanyDTO;
import com.dellin.mondoc.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/companies")
@RequiredArgsConstructor
@Tag(name = "Companies", description = "The company API. Contains operations to work "
		+ "with companies like add new one or add it to definite user")
@SecurityScheme(type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT",
				name = "Authorization")
public class CompanyController {
	
	private final CompanyService companyService;
	
	@PutMapping("/add")
	@Operation(summary = "Add a new company",
			   security = @SecurityRequirement(name = "Authorization"))
	public ResponseEntity<CompanyDTO> create(@RequestBody CompanyDTO companyDTO) {
		return companyService.create(companyDTO);
	}
	
	@PostMapping("/addToUser")
	@Operation(summary = "Add a company to a user",
			   security = @SecurityRequirement(name = "Authorization"))
	public ResponseEntity<?> addToUser(@RequestBody CompanyToUserForm form) {
		
		companyService.addCompanyToUser(form.getEmail(), form.getInn());
		return ResponseEntity.ok().build();
	}
	
	@PutMapping("/update")
	@Operation(summary = "Update company",
			   security = @SecurityRequirement(name = "Authorization"))
	public ResponseEntity<CompanyDTO> update(@RequestBody CompanyDTO companyDTO) {
		return companyService.update(companyDTO);
	}
}

@Data
class CompanyToUserForm {
	
	private String email;
	private String inn;
}
