package com.dellin.mondoc.service.impl;

import com.dellin.mondoc.exceptions.CustomException;
import com.dellin.mondoc.model.dto.RoleDTO;
import com.dellin.mondoc.model.entity.Role;
import com.dellin.mondoc.model.entity.User;
import com.dellin.mondoc.model.repository.RoleRepository;
import com.dellin.mondoc.model.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RoleServiceImplTest {
	
	@InjectMocks
	private RoleServiceImpl roleService;
	
	@Mock
	private RoleRepository roleRepository;
	
	@Mock
	private UserRepository userRepository;
	
	@Mock
	private UserServiceImpl userService;
	
	@Spy
	private ObjectMapper mapper;
	
	@Test
	public void create() {
		
		RoleDTO roleDTO = new RoleDTO();
		roleDTO.setRoleName("ROLE_TEST");
		
		when(roleRepository.save(any(Role.class))).thenAnswer(i -> i.getArguments()[0]);
		
		RoleDTO result = roleService.create(roleDTO);
		assertEquals(roleDTO.getRoleName(), result.getRoleName());
	}
	
	@Test(expected = CustomException.class)
	public void create_emptyRoleName() {
		
		RoleDTO roleDTO = new RoleDTO();
		roleDTO.setRoleName("");
		
		roleService.create(roleDTO);
	}
	
	@Test(expected = CustomException.class)
	public void create_roleExist() {
		
		RoleDTO roleDTO = new RoleDTO();
		roleDTO.setRoleName("mondoc");
		
		Role role = mapper.convertValue(roleDTO, Role.class);
		
		when(roleRepository.findByRoleName(anyString())).thenReturn(Optional.of(role));
		
		roleService.create(roleDTO);
	}
	
	@Test
	public void get() {
		
		Role role = new Role();
		role.setRoleName("ROLE_TEST");
		
		when(roleRepository.findByRoleName(anyString())).thenReturn(Optional.of(role));
		
		RoleDTO result = roleService.get("role");
		assertEquals(role.getRoleName(), result.getRoleName());
	}
	
	@Test(expected = CustomException.class)
	public void get_notFound() {
		
		roleService.get("role");
	}
	
	@Test
	public void addRoleToUser() {
		
		User user = new User();
		user.setEmail("test@test.com");
		user.setRoles(new ArrayList<>());
		
		Role role = new Role();
		role.setRoleName("ROLE_TEST");
		
		when(userService.getUser(anyString())).thenReturn(user);
		when(roleRepository.findByRoleName(anyString())).thenReturn(Optional.of(role));
		when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);
		
		roleService.addRoleToUser("test@test.com", "role");
		
		verify(userRepository, times(1)).save(user);
	}
	
	@Test(expected = CustomException.class)
	public void addRoleToUser_userRoleExist() {
		
		User user = new User();
		user.setEmail("test@test.com");
		
		Role role = new Role();
		role.setRoleName("ROLE_TEST");
		user.setRoles(Collections.singletonList(role));
		
		when(userService.getUser(anyString())).thenReturn(user);
		when(roleRepository.findByRoleName(anyString())).thenReturn(Optional.of(role));
		
		roleService.addRoleToUser("test@test.com", "role");
	}
	
	@Test
	public void getRole() {
		Role role = new Role();
		role.setRoleName("ROLE_TEST");
		
		when(roleRepository.findByRoleName(anyString())).thenReturn(Optional.of(role));
		
		Role result = roleService.getRole("role");
		
		assertEquals(role.getRoleName(), result.getRoleName());
	}
	
	@Test(expected = CustomException.class)
	public void getRole_notFound() {
		
		roleService.getRole("role");
	}
}
