package com.dellin.mondoc.model.dto;

import com.dellin.mondoc.model.entity.Role;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDTO {
	
	@NotEmpty(message = "Email should not be empty") @Email String email;
	
	@NotEmpty(message = "Password should not be empty") String password;
	
	Collection<Role> roles = new ArrayList<>();
}
