package com.dellin.mondoc.model.dto;

import com.dellin.mondoc.model.entity.Role;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import java.util.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDTO {
	
	@NotEmpty(message = "Email should not be empty") @Email String email;
	
	@NotEmpty(message = "Password should not be empty") String password;
	
	@ManyToMany(fetch = FetchType.EAGER)
	Collection<Role> roles = new ArrayList<>();
}
