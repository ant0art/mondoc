package com.dellin.mondoc;

import com.dellin.mondoc.model.dto.RoleDTO;
import com.dellin.mondoc.model.dto.UserDTO;
import com.dellin.mondoc.service.RoleService;
import com.dellin.mondoc.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

@SpringBootApplication
public class MondocApplication {

	public static void main(String[] args) {
		SpringApplication.run(MondocApplication.class, args);
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public CommandLineRunner run(UserService userService, RoleService roleService) {
		return args -> {

			roleService.create(new RoleDTO("ROLE_USER"));
			roleService.create(new RoleDTO("ROLE_ADMIN"));

			userService.create(new UserDTO("john@travolta.com", "1234",
					new ArrayList<>()));
			userService.create(new UserDTO("will@Smith.com", "1234",
					new ArrayList<>()));
			userService.create(
					new UserDTO("jim@carry.com", "1234", new ArrayList<>()));
			userService.create(new UserDTO("sarah@conor.com", "1234",
					new ArrayList<>()));

			roleService.addRoleToUser("jim@carry.com", "ROLE_ADMIN");
			roleService.addRoleToUser("sarah@conor.com", "ROLE_ADMIN");
		};
	}
}
