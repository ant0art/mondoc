package com.dellin.mondoc.service.impl;

import com.dellin.mondoc.exceptions.CustomException;
import com.dellin.mondoc.model.dto.CompanyDTO;
import com.dellin.mondoc.model.entity.Company;
import com.dellin.mondoc.model.entity.User;
import com.dellin.mondoc.model.repository.CompanyRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CompanyServiceImplTest {
	
	@InjectMocks
	private CompanyServiceImpl companyService;
	@Mock
	private UserServiceImpl userService;
	
	@Mock
	private CompanyRepository companyRepository;
	
	@Spy
	private ObjectMapper mapper;
	
	@Test
	public void create() {
		
		CompanyDTO companyDTO = new CompanyDTO();
		companyDTO.setInn("123");
		companyDTO.setName("mondoc");
		
		when(companyRepository.save(any(Company.class))).thenAnswer(
				i -> i.getArguments()[0]);
		
		ResponseEntity<CompanyDTO> resultDTO = companyService.create(companyDTO);
		
		assertEquals(companyDTO.getName(), resultDTO.getBody().getName());
	}
	
	@Test(expected = CustomException.class)
	public void create_existedCompany() {
		
		CompanyDTO companyDTO = new CompanyDTO();
		companyDTO.setInn("123");
		companyDTO.setName("mondoc");
		
		Company company = mapper.convertValue(companyDTO, Company.class);
		
		when(companyRepository.findByInn(anyString())).thenReturn(Optional.of(company));
		
		companyService.create(companyDTO);
	}
	
	@Test(expected = CustomException.class)
	public void create_companyInnNull() {
		
		companyService.create(new CompanyDTO());
	}
	
	@Test
	public void addCompanyToUser() {
		
		Company company = new Company();
		company.setName("mondoc");
		company.setInn("123");
		company.setUsers(new HashSet<>());
		
		User user = new User();
		user.setEmail("test@test.com");
		user.setCompanies(new HashSet<>());
		
		when(companyRepository.findByInn(anyString())).thenReturn(Optional.of(company));
		when(userService.getUser(anyString())).thenReturn(user);
		when(companyRepository.save(any(Company.class))).thenAnswer(
				i -> i.getArguments()[0]);
		
		companyService.addCompanyToUser("test@test.com", "345");
		
		verify(companyRepository, times(1)).save(any(Company.class));
	}
	
	@Test(expected = CustomException.class)
	public void addCompanyToUser_userCompanyExist() {
		
		Company company = new Company();
		company.setUsers(new HashSet<>());
		Set<Company> companies = Collections.singleton(company);
		
		User user = new User();
		user.setCompanies(companies);
		
		when(companyRepository.findByInn(anyString())).thenReturn(Optional.of(company));
		when(userService.getUser(anyString())).thenReturn(user);
		
		companyService.addCompanyToUser("test@test.com", "123");
	}
	
	@Test(expected = CustomException.class)
	public void addCompanyToUser_innNotFound() {
		
		companyService.addCompanyToUser("test@test.com", "123");
	}
	
	@Test
	public void update() {
		
		CompanyDTO companyDTO = new CompanyDTO();
		companyDTO.setName("mondoc");
		companyDTO.setInn("123");
		
		Company company = mapper.convertValue(companyDTO, Company.class);
		
		when(companyRepository.findByInn(anyString())).thenReturn(Optional.of(company));
		
		when(companyRepository.save(any(Company.class))).thenAnswer(
				i -> i.getArguments()[0]);
		
		ResponseEntity<CompanyDTO> result = companyService.update(companyDTO);
		
		assertEquals(companyDTO.getName(), result.getBody().getName());
	}
	
	@Test(expected = CustomException.class)
	public void update_emptyName() {
		
		CompanyDTO companyDTO = new CompanyDTO();
		companyDTO.setName("");
		companyDTO.setInn("123");
		
		Company company = mapper.convertValue(companyDTO, Company.class);
		
		when(companyRepository.findByInn(anyString())).thenReturn(Optional.of(company));
		
		companyService.update(companyDTO);
	}
	
	@Test(expected = CustomException.class)
	public void update_innNotFound() {
		
		companyService.update(new CompanyDTO());
	}
	
	@Test
	public void getCompany() {
		
		Company company = new Company();
		company.setName("mondoc");
		
		when(companyRepository.findByInn(anyString())).thenReturn(Optional.of(company));
		
		Company result = companyService.getCompany("123");
		assertEquals(company.getName(), result.getName());
	}
	
	@Test(expected = CustomException.class)
	public void getCompany_innNotFound() {
		
		companyService.getCompany("123");
	}
}
