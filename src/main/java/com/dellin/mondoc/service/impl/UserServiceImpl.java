package com.dellin.mondoc.service.impl;

import com.dellin.mondoc.exceptions.CustomException;
import com.dellin.mondoc.model.dto.UserDTO;
import com.dellin.mondoc.model.entity.Role;
import com.dellin.mondoc.model.entity.User;
import com.dellin.mondoc.model.enums.EntityStatus;
import com.dellin.mondoc.model.repository.RoleRepository;
import com.dellin.mondoc.model.repository.UserRepository;
import com.dellin.mondoc.service.UserService;
import com.dellin.mondoc.utils.PaginationUtil;
import com.dellin.mondoc.utils.PropertiesUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;

import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.stream.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService, UserDetailsService {
	
	private final RoleRepository roleRepository;
	
	private final UserRepository userRepository;
	
	private final ObjectMapper mapper =
			JsonMapper.builder().addModule(new JavaTimeModule()).build();
	
	private final PasswordEncoder passwordEncoder;
	private final EmailValidator validator = EmailValidator.getInstance();
	
	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		User user = getUser(email);
		if (user == null) {
			throw new UsernameNotFoundException(
					String.format("User [EMAIL: %s] not found", email));
		}
		log.info(String.format("User [EMAIL: %s] found in db", email));
		
		Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
		user.getRoles()
				.forEach(role -> authorities.add(
						new SimpleGrantedAuthority(role.getRoleName())));
		return new org.springframework.security.core.userdetails.User(user.getUsername(),
				user.getPassword(), authorities);
	}
	
	@Override
	public UserDTO create(UserDTO userDTO) {
		if (!validator.isValid(userDTO.getEmail())) {
			throw new CustomException(
					String.format("Email: [%s] is not valid", userDTO.getEmail()),
					HttpStatus.BAD_REQUEST);
		}
		//userDTO --> user
		User user = mapper.convertValue(userDTO, User.class);
		final String roleName = "ROLE_USER";
		Role role = roleRepository.findByRoleName(roleName).orElseThrow(
				() -> new CustomException(
						String.format("Role [NAME: %s] not found", roleName),
						HttpStatus.NOT_FOUND));
		user.getRoles().add(role);
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		user.setStatus(EntityStatus.CREATED);
		log.info("User [EMAIL: {}] with role: [{}] was created", user.getEmail(),
				role.getRoleName());
		
		//user --> userDTO
		return mapper.convertValue(userRepository.save(user), UserDTO.class);
	}
	
	@Override
	public UserDTO get(String email) {
		return mapper.convertValue(getUser(email), UserDTO.class);
	}
	
	@Override
	public UserDTO update(String email, UserDTO userDTO) {
		
		AtomicReference<UserDTO> dto = new AtomicReference<>(new UserDTO());
		userRepository.findByEmail(email).ifPresentOrElse(u -> {
			if (userDTO.getEmail() != null && !userDTO.getEmail().isEmpty()) {
				userRepository.findByEmail(userDTO.getEmail()).ifPresent(e -> {
					throw new CustomException(
							String.format("User [EMAIL: %s] is already exist",
									u.getEmail()), HttpStatus.BAD_REQUEST);
				});
			}
			PropertiesUtil.copyPropertiesIgnoreNull(
					mapper.convertValue(userDTO, User.class), u);
			updateStatus(u, EntityStatus.UPDATED);
			dto.set(mapper.convertValue(userRepository.save(u), UserDTO.class));
			log.info("User [EMAIL: {}] was updated", email);
		}, () -> {
			throw new CustomException(
					String.format("User [EMAIL: %s] not found. Nothing to update", email),
					HttpStatus.NOT_FOUND);
		});
		return dto.get();
	}
	
	@Override
	public void delete(String email) {
		User user = getUser(email);
		updateStatus(user, EntityStatus.DELETED);
		log.info("Deleting user with email: {}", email);
		userRepository.save(user);
	}
	
	@Override
	public User getUser(String email) {
		return userRepository.findByEmail(email).orElseThrow(() -> new CustomException(
				String.format("User [EMAIL: %s] not found", email),
				HttpStatus.NOT_FOUND));
	}
	
	@Override
	public ModelMap getUsers(Integer page, Integer perPage, String sort,
			Sort.Direction order) {
		if (perPage == 0) {
			throw new CustomException("Page size must not be less than one",
					HttpStatus.BAD_REQUEST);
		}
		
		Pageable pageRequest = PaginationUtil.getPageRequest(page, perPage, sort, order);
		Page<User> pageResult = userRepository.findAll(pageRequest);
		
		List<UserDTO> content = pageResult.getContent()
				.stream()
				.map(u -> mapper.convertValue(u, UserDTO.class))
				.collect(Collectors.toList());
		
		ModelMap map = new ModelMap();
		map.addAttribute("content", content);
		map.addAttribute("pageNumber", page);
		map.addAttribute("PageSize", pageResult.getNumberOfElements());
		map.addAttribute("totalPages", pageResult.getTotalPages());
		
		return map;
	}
	
	@Override
	public void updateStatus(User user, EntityStatus status) {
		user.setStatus(status);
		user.setUpdatedAt(LocalDateTime.now());
	}
}
