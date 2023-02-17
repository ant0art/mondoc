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
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RoleServiceImpl implements RoleService {
	
	private final UserRepository userRepository;
	
	private final RoleRepository roleRepository;
	
	private final UserService userService;
	
	private final ObjectMapper mapper = JsonMapper.builder().addModule(
			new JavaTimeModule()).build();
	
	@Override
	public RoleDTO create(RoleDTO roleDTO) {
		if (roleDTO.getRoleName() == null || roleDTO.getRoleName().isEmpty()) {
			throw new CustomException("Role name can`t be null or empty",
					HttpStatus.BAD_REQUEST);
		}
		//roleDTO --> role
		Role role = mapper.convertValue(roleDTO, Role.class);
		updateStatus(role, EntityStatus.CREATED);
		log.info("Role: {}  created", role.getRoleName());
		
		//role --> roleDTOq
		return mapper.convertValue(roleRepository.save(role), RoleDTO.class);
	}
	
	@Override
	public RoleDTO get(String roleName) {
		Role role = getRole(roleName);
		return mapper.convertValue(role, RoleDTO.class);
	}
	
	@Override
	public void addRoleToUser(String email, String roleName) {
		log.info("Adding to user with email: {} a role {}", email, roleName);
		User user = userService.getUser(email);
		
		Role role = getRole(roleName);
		
		if (user.getRoles().stream().anyMatch(r -> r.equals(role))) {
			throw new CustomException(
					String.format("User with email: %s already has a role: %s", email,
							roleName), HttpStatus.BAD_REQUEST);
		}
		user.getRoles().add(role);
		userService.updateStatus(user, EntityStatus.UPDATED);
		role.getUsers().add(user);
		updateStatus(role, EntityStatus.UPDATED);
		userRepository.save(user);
	}
	
	public Role getRole(String roleName) {
		return roleRepository.findByRoleName(roleName).orElseThrow(
				() -> new CustomException(
						String.format("Role with name: %s not found", roleName),
						HttpStatus.NOT_FOUND));
	}
	
	private void updateStatus(Role role, EntityStatus status) {
		role.setState(status);
		role.setUpdatedAt(LocalDateTime.now());
	}
}
