package com.dellin.mondoc.service;

import com.dellin.mondoc.model.dto.CompanyDTO;
import org.springframework.http.ResponseEntity;

public interface CompanyService {
	
	ResponseEntity<CompanyDTO> create(CompanyDTO companyDTO);
	
	void addCompanyToUser(String email, String inn);
	
	CompanyDTO update(CompanyDTO companyDTO);
}
