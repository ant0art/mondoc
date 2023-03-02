package com.dellin.mondoc.service.impl;

import com.dellin.mondoc.exceptions.CustomException;
import com.dellin.mondoc.model.dto.CompanyDTO;
import com.dellin.mondoc.model.entity.Company;
import com.dellin.mondoc.model.entity.User;
import com.dellin.mondoc.model.enums.EntityStatus;
import com.dellin.mondoc.model.repository.CompanyRepository;
import com.dellin.mondoc.service.CompanyService;
import com.dellin.mondoc.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {
	
	private final CompanyRepository companyRepository;
	private final UserService userService;
	private final ObjectMapper mapper =
			JsonMapper.builder().addModule(new JavaTimeModule()).build();
	
	@Override
	public ResponseEntity<CompanyDTO> create(CompanyDTO companyDTO) {
		
		String inn = companyDTO.getInn();
		if (inn == null || inn.isEmpty()) {
			throw new CustomException("Company inn can`t be null or empty",
					HttpStatus.BAD_REQUEST);
		}
		companyRepository.findByInn(companyDTO.getInn()).ifPresent(c -> {
			throw new CustomException(
					String.format("Company [INN: %s] is already exist", c.getInn()),
					HttpStatus.BAD_REQUEST);
		});
		
		//companyDTO --> company
		Company company = mapper.convertValue(companyDTO, Company.class);
		company.setStatus(EntityStatus.CREATED);
		
		//company --> companyDTO
		CompanyDTO dto =
				mapper.convertValue(companyRepository.save(company), CompanyDTO.class);
		log.info("Company [INN: {}] was created", company.getInn());
		
		return ResponseEntity.status(HttpStatus.OK).body(dto);
	}
	
	@Override
	public void addCompanyToUser(String email, String inn) {
		
		Company company = getCompany(inn);
		User user = userService.getUser(email);
		
		if (user.getCompanies().contains(company)) {
			throw new CustomException(String.format(
					"User [%s] already has the rights to view a company [%s]", email,
					company.getName()), HttpStatus.BAD_REQUEST);
		}
		
		user.setStatus(EntityStatus.UPDATED);
		user.setUpdatedAt(LocalDateTime.now());
		
		company.getUsers().add(user);
		company.setStatus(EntityStatus.UPDATED);
		company.setUpdatedAt(LocalDateTime.now());
		
		companyRepository.save(company);
		log.info("Company [NAME: {}, INN: {}] was added to user [EMAIL: {}]",
				company.getName(), company.getInn(), user.getEmail());
	}
	
	@Override
	public ResponseEntity<CompanyDTO> update(CompanyDTO companyDTO) {
		
		Company company = getCompany(companyDTO.getInn());
		String name = companyDTO.getName();
		
		if (name == null || name.isEmpty()) {
			throw new CustomException("Company name can`t be empty",
					HttpStatus.BAD_REQUEST);
		}
		company.setName(name);
		company.setStatus(EntityStatus.UPDATED);
		company.setUpdatedAt(LocalDateTime.now());
		
		CompanyDTO dto =
				mapper.convertValue(companyRepository.save(company), CompanyDTO.class);
		
		return ResponseEntity.status(HttpStatus.OK).body(dto);
	}
	
	public Company getCompany(String inn) {
		return companyRepository.findByInn(inn).orElseThrow(() -> new CustomException(
				String.format("Company [ID: %s] not found", inn), HttpStatus.NOT_FOUND));
	}
}
