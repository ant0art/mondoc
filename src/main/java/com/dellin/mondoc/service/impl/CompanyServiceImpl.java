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

/**
 * Service class to work with Companies
 *
 * @see Company
 * @see CompanyRepository
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {
	
	/**
	 * Repository which contains companies
	 */
	private final CompanyRepository companyRepository;
	/**
	 * User service class
	 */
	private final UserService userService;
	/**
	 * ObjectMapper for reading and writing JSON
	 */
	private final ObjectMapper mapper =
			JsonMapper.builder().addModule(new JavaTimeModule()).build();
	
	/**
	 * Method that create a new {@link Company} entity and write it to database
	 * <p>
	 * Returns the ResponseEntity object with http <b>200</b> status if well-created. The
	 * object passed to the method should contain the required inn field.
	 *
	 * @param companyDTO the {@link CompanyDTO} object to add
	 *
	 * @return the {@link ResponseEntity} object
	 */
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
	
	/**
	 * Method provides to add a company to the user
	 * <p>
	 * Method parameters must belong to previously created objects
	 *
	 * @param email the value of required field email of {@link User}
	 * @param inn   the value of required field inn of {@link Company}
	 */
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
	
	/**
	 * Method that update current {@link Company} entity found in database
	 * <p>
	 * Returns the ResponseEntity object with http <b>200</b> status if well-updated. The
	 * object passed to the method should contain the required <u>inn</u> field.
	 *
	 * @param companyDTO the {@link CompanyDTO} object to update
	 *
	 * @return the {@link ResponseEntity} object
	 */
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
	
	/**
	 * Method that find a {@link Company} in the database by inn
	 * <p>
	 * Returns the Company object if found or else a {@link CustomException} with http
	 * <b>404</b> status
	 *
	 * @param inn the company inn
	 *
	 * @return the {{@link Company} object
	 */
	public Company getCompany(String inn) {
		return companyRepository.findByInn(inn).orElseThrow(() -> new CustomException(
				String.format("Company [INN: %s] not found", inn), HttpStatus.NOT_FOUND));
	}
}
