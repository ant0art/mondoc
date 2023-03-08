package com.dellin.mondoc.service.impl;

import com.dellin.mondoc.exceptions.CustomException;
import com.dellin.mondoc.model.dto.RoleDTO;
import com.dellin.mondoc.model.entity.Role;
import com.dellin.mondoc.model.entity.User;
import com.dellin.mondoc.model.enums.EntityStatus;
import com.dellin.mondoc.model.repository.RoleRepository;
import com.dellin.mondoc.model.repository.UserRepository;
import com.dellin.mondoc.service.RoleService;
import com.dellin.mondoc.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class to work with Roles
 *
 * @see Role
 * @see RoleRepository
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RoleServiceImpl implements RoleService {
	
	/**
	 * Repository which contains users
	 */
	private final UserRepository userRepository;
	
	/**
	 * Repository which contains roles
	 */
	private final RoleRepository roleRepository;
	
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
	 * Method that creates a new {@link Role} entity and write it to database
	 * <p>
	 * Returns a RoleDTO object if well-created
	 *
	 * @param roleDTO the {@link RoleDTO} object to add
	 *
	 * @return thr {@link RoleDTO} object
	 */
	@Override
	public RoleDTO create(RoleDTO roleDTO) {
		String roleName = roleDTO.getRoleName();
		if (roleName == null || roleName.isEmpty()) {
			throw new CustomException("Role name can`t be null or empty",
					HttpStatus.BAD_REQUEST);
		}
		roleRepository.findByRoleName(roleName).ifPresent(r -> {
			throw new CustomException(
					String.format("Role [NAME: %s] is already exist", r.getRoleName()),
					HttpStatus.BAD_REQUEST);
		});
		
		//roleDTO --> role
		Role role = mapper.convertValue(roleDTO, Role.class);
		role.setStatus(EntityStatus.CREATED);
		log.info("Role: [NAME: {}] was created", role.getRoleName());
		
		//role --> roleDTO
		return mapper.convertValue(roleRepository.save(role), RoleDTO.class);
	}
	
	/**
	 * Method that find a {@link Role} in the database by role name
	 * <p>
	 * Returns the RoleDTO object if found or else the {@link CustomException} with http
	 * <b>404</b> status
	 *
	 * @param roleName the name of role
	 *
	 * @return the {@link RoleDTO} object
	 */
	@Override
	public RoleDTO get(String roleName) {
		return mapper.convertValue(getRole(roleName), RoleDTO.class);
	}
	
	/**
	 * Method provides to add a role to the user
	 * <p>
	 * Method parameters must belong to previously created objects.
	 *
	 * @param email    the value of {@link User} required field
	 * @param roleName the value of {@link Role} required field
	 */
	@Override
	public void addRoleToUser(String email, String roleName) {
		log.info("Adding to user [EMAIL: {}] a role [{}]", email, roleName);
		User user = userService.getUser(email);
		
		Role role = getRole(roleName);
		
		if (user.getRoles()
				.stream()
				.anyMatch(r -> r.equals(role))) {
			throw new CustomException(
					String.format("User [EMAIL: %s] already has a role: [%s]", email,
							roleName), HttpStatus.BAD_REQUEST);
		}
		user.getRoles().add(role);
		userService.updateStatus(user, EntityStatus.UPDATED);
		userRepository.save(user);
		log.info("Role [NAME: {}] was added to user [EMAIL: {}]", role.getRoleName(),
				user.getEmail());
	}
	
	/**
	 * Method that find a {@link Role} in the database by role name
	 * <p>
	 * Returns the Role object if found or else the {@link CustomException} with http
	 * <b>404</b> status
	 *
	 * @param roleName the name of role
	 *
	 * @return the {@link Role} object
	 */
	public Role getRole(String roleName) {
		return roleRepository.findByRoleName(roleName).orElseThrow(
				() -> new CustomException(
						String.format("Role [NAME: %s] not found", roleName),
						HttpStatus.NOT_FOUND));
	}
}
