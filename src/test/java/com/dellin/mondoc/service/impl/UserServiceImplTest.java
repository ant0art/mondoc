package com.dellin.mondoc.service.impl;

import com.dellin.mondoc.exceptions.CustomException;
import com.dellin.mondoc.model.dto.UserDTO;
import com.dellin.mondoc.model.entity.Role;
import com.dellin.mondoc.model.entity.User;
import com.dellin.mondoc.model.enums.EntityStatus;
import com.dellin.mondoc.model.repository.RoleRepository;
import com.dellin.mondoc.model.repository.UserRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ModelMap;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceImplTest {
	
	@InjectMocks
	private UserServiceImpl userService;
	@Mock
	private RoleRepository roleRepository;
	@Mock
	private UserRepository userRepository;
	@Spy
	private PasswordEncoder passwordEncoder;
	
	@Test
	public void loadUserByUsername() {
		
		User user = new User();
		user.setEmail("test@test.com");
		user.setPassword("pass");
		
		Role role = new Role();
		role.setRoleName("ROLE_TEST");
		Collection<Role> roles = Collections.singleton(role);
		user.setRoles(roles);
		
		when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
		
		UserDetails userDetails = userService.loadUserByUsername("test@test.com");
		
		assertNotNull(userDetails);
		assertEquals(user.getUsername(),
				ReflectionTestUtils.getField(userDetails, "username"));
		
		assertEquals(user.getPassword(),
				ReflectionTestUtils.getField(userDetails, "password"));
		
		assertEquals(user.getRoles()
						.stream().findFirst().orElseThrow().getRoleName(),
				userDetails.getAuthorities()
						.stream().findFirst().orElseThrow().getAuthority());
	}
	
	@Test
	public void create() {
		
		UserDTO userDTO = new UserDTO();
		userDTO.setEmail("test@test.com");
		userDTO.setPassword("pass");
		
		Role role = new Role();
		role.setRoleName("ROLE_USER");
		
		when(roleRepository.findByRoleName(anyString())).thenReturn(Optional.of(role));
		
		when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);
		
		UserDTO resultDTO = userService.create(userDTO);
		
		assertEquals(userDTO.getEmail(), resultDTO.getEmail());
	}
	
	@Test(expected = CustomException.class)
	public void create_emailNotValid() {
		
		UserDTO userDTO = new UserDTO();
		userDTO.setEmail("test_test.com");
		userService.create(userDTO);
	}
	
	@Test(expected = CustomException.class)
	public void create_roleNotFound() {
		
		UserDTO userDTO = new UserDTO();
		userDTO.setEmail("test@test.com");
		userDTO.setPassword("pass");
		
		userService.create(userDTO);
	}
	
	@Test
	public void get() {
		
		User user = new User();
		user.setEmail("test@test.com");
		
		when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
		
		UserDTO resultDTO = userService.get("test@test.com");
		
		assertEquals(user.getEmail(), resultDTO.getEmail());
	}
	
	@Test
	public void update() {
		
		Role role = new Role();
		role.setRoleName("ROLE_TEST");
		Collection<Role> roles = Collections.singleton(role);
		
		UserDTO userDTO = new UserDTO();
		userDTO.setRoles(roles);
		
		User user = new User();
		user.setEmail("user@test.com");
		
		when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
		
		when(userRepository.save(user)).thenAnswer(i -> i.getArguments()[0]);
		
		UserDTO resultDTO = userService.update(user.getEmail(), userDTO);
		assertEquals(userDTO.getEmail(), resultDTO.getEmail());
		assertEquals(userDTO.getRoles()
				.stream().findFirst().orElseThrow().getRoleName(), resultDTO.getRoles()
				.stream().findFirst().orElseThrow().getRoleName());
	}
	
	@Test(expected = CustomException.class)
	public void update_emailNotFound() {
		
		UserDTO userDTO = new UserDTO();
		userDTO.setEmail("dto@test.com");
		
		User user = new User();
		user.setEmail("user@test.com");
		
		when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
		
		UserDTO resultDTO = userService.update("user@test.com", userDTO);
		assertEquals(userDTO.getEmail(), resultDTO.getEmail());
	}
	
	@Test(expected = CustomException.class)
	public void update_userExist() {
		
		UserDTO userDTO = new UserDTO();
		userDTO.setEmail("dto@test.com");
		
		User user = new User();
		user.setEmail("user@test.com");
		
		when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
		
		UserDTO resultDTO = userService.update("user@test.com", userDTO);
		assertEquals(userDTO.getEmail(), resultDTO.getEmail());
	}
	
	@Test
	public void delete() {
		
		User user = new User();
		
		when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
		when(userRepository.save(user)).thenAnswer(i -> i.getArguments()[0]);
		userService.delete("user@email.com");
		
		assertEquals(EntityStatus.DELETED, user.getStatus());
	}
	
	@Test
	public void getUser() {
		User user = new User();
		user.setEmail("test@test.com");
		when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
		User result = userService.getUser("test@test.com");
		assertEquals(user.getUsername(), result.getUsername());
	}
	
	@Test(expected = CustomException.class)
	public void getUser_userNotFound() {
		userService.getUser("test@test.com");
	}
	
	@Test
	public void getUsers() {
		
		Integer page = 2;
		Integer perPage = 10;
		String sort = "id";
		Sort.Direction order = Sort.Direction.DESC;
		User user = new User();
		user.setEmail("user@test.com");
		List<User> users = Collections.singletonList(user);
		
		@SuppressWarnings("unchecked")
		Page<User> pageResult = mock(Page.class);
		
		when(userRepository.findAll(any(Pageable.class))).thenReturn(pageResult);
		when(pageResult.getContent()).thenReturn(users);
		ModelMap result = userService.getUsers(page, perPage, sort, order);
		
		@SuppressWarnings("unchecked")
		List<UserDTO> userDTOList = (List<UserDTO>) (result.get("content"));
		assertEquals(user.getEmail(), userDTOList.get(0).getEmail());
	}
	
	@Test(expected = CustomException.class)
	public void getUsers_perPageZero() {
		Integer page = 2;
		Integer perPage = 0;
		String sort = "id";
		Sort.Direction order = Sort.Direction.DESC;
		userService.getUsers(page, perPage, sort, order);
	}
	
	@Test
	public void updateStatus() {
		
		User user = new User();
		userService.updateStatus(user, EntityStatus.UPDATED);
		assertEquals(EntityStatus.UPDATED, user.getStatus());
	}
}
