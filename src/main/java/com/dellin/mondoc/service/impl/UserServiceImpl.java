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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;

import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.stream.*;

/**
 * Service class to work with Users
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService, UserDetailsService {
	
	/**
	 * Repository which contains roles
	 */
	private final RoleRepository roleRepository;
	
	/**
	 * Repository which contains users
	 */
	private final UserRepository userRepository;
	
	/**
	 * ObjectMapper for reading and writing JSON
	 */
	private final ObjectMapper mapper =
			JsonMapper.builder().addModule(new JavaTimeModule()).build();
	
	/**
	 * Injected service interface for encoding passwords
	 */
	private final PasswordEncoder passwordEncoder;
	/**
	 * The field of injected email validator
	 */
	private final EmailValidator validator = EmailValidator.getInstance();
	
	/**
	 * Locates the user based on the email
	 *
	 * @param email the username identifying the user whose data is required.
	 *
	 * @return a fully populated user record (never null)
	 */
	@Override
	public UserDetails loadUserByUsername(String email) {
		User user = getUser(email);
		log.info(String.format("User [EMAIL: %s] found in db", email));
		
		Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
		user.getRoles()
				.forEach(role -> authorities.add(
						new SimpleGrantedAuthority(role.getRoleName())));
		return new org.springframework.security.core.userdetails.User(user.getUsername(),
				user.getPassword(), authorities);
	}
	
	/**
	 * Method that creates a new {@link User} entity and write it to database
	 * <p>
	 * Returns a UserDTO object if well-created
	 *
	 * @param userDTO the {@link UserDTO} object to add
	 *
	 * @return the {@link UserDTO} object
	 */
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
	
	/**
	 * Method that find a {@link Role} in the database by role name
	 * <p>
	 * Returns the RoleDTO object if found or else the {@link CustomException} with http
	 * <b>404</b> status
	 *
	 * @param email the email of user
	 *
	 * @return the {@link UserDTO} object
	 */
	@Override
	public UserDTO get(String email) {
		return mapper.convertValue(getUser(email), UserDTO.class);
	}
	
	/**
	 * Method that update current {@link User} entity found in database
	 * <p>
	 * Returns a {@link UserDTO} object after updating fields of its entity
	 *
	 * @param email   the email of User
	 * @param userDTO the {@link UserDTO} object to update
	 *
	 * @return the updated {@link UserDTO} object
	 */
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
			
			u.setEmail(userDTO.getEmail());
			u.setRoles(userDTO.getRoles());
			u.setPassword(passwordEncoder.encode(userDTO.getPassword()));
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
	
	/**
	 * Method that delete User by its email
	 * <p>
	 * Method changes status of entity to {@link EntityStatus#DELETED} instead of totally
	 * removing it from database
	 *
	 * @param email the email of User
	 */
	@Override
	public void delete(String email) {
		User user = getUser(email);
		updateStatus(user, EntityStatus.DELETED);
		log.info("Deleting user with email: {}", email);
		userRepository.save(user);
	}
	
	/**
	 * Method that find a {@link User} in the database by email
	 * <p>
	 * Returns the Company object if found or else a {@link CustomException} with http
	 * <b>404</b> status
	 *
	 * @param email the email of User
	 *
	 * @return the {@link User} object
	 */
	@Override
	public User getUser(String email) {
		return userRepository.findByEmail(email).orElseThrow(() -> new CustomException(
				String.format("User [EMAIL: %s] not found", email),
				HttpStatus.NOT_FOUND));
	}
	
	/**
	 * Method that gets all available Users
	 * <p>
	 * Returns a model map of all objects users in a limited size list sorted by chosen
	 * parameter.
	 *
	 * @param page    the serial number of page
	 * @param perPage the number of elements on page
	 * @param sort    the main parameter of sorting
	 * @param order   ASC or DESC
	 *
	 * @return the ModelMap of sorted {@link User}
	 *
	 * @see ModelMap
	 * @see Pageable
	 * @see Page
	 */
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
	
	/**
	 * Change the state of {@link User}-entity by chosen and set up the entity field
	 * updatedAt new local date time
	 *
	 * @param user   the {@link User} object
	 * @param status the {@link EntityStatus} enum
	 */
	@Override
	public void updateStatus(User user, EntityStatus status) {
		user.setStatus(status);
		user.setUpdatedAt(LocalDateTime.now());
	}
}
