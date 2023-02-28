package com.dellin.mondoc.service;

import com.dellin.mondoc.model.dto.CompanyDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

public interface CompanyService {
	
	ResponseEntity<CompanyDTO> create(CompanyDTO companyDTO);
	
	@Transactional
	void addCompanyToUser(String email, String inn);
	
	ResponseEntity<CompanyDTO> update(CompanyDTO companyDTO);
}
