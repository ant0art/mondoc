package com.dellin.mondoc.controllers;

import com.dellin.mondoc.model.dto.CompanyDTO;
import com.dellin.mondoc.service.CompanyService;
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
public class CompanyController {
	
	private final CompanyService companyService;
	
	@PutMapping("/add")
	public ResponseEntity<CompanyDTO> create(@RequestBody CompanyDTO companyDTO) {
		return companyService.create(companyDTO);
	}
	
	@PostMapping("/addToUser")
	public ResponseEntity<?> addToUser(@RequestBody CompanyToUserForm form) {
		
		companyService.addCompanyToUser(form.getEmail(), form.getInn());
		return ResponseEntity.ok().build();
	}
	
	@PutMapping("/update")
	public ResponseEntity<CompanyDTO> update(@RequestBody CompanyDTO companyDTO) {
		return ResponseEntity.ok(companyService.update(companyDTO));
	}
}

@Data
class CompanyToUserForm {
	
	private String email;
	private String inn;
}
