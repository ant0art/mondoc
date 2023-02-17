package com.dellin.mondoc.model.entity;

import com.dellin.mondoc.model.enums.EntityStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.GrantedAuthority;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.*;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "roles")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Role implements GrantedAuthority {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	Long id;
	
	@Column(nullable = false, unique = true)
	String roleName;
	
	@ManyToMany
	@JsonIgnore
	@JoinTable(name = "users_roles",
			   joinColumns = {@JoinColumn(name = "USER_ID", referencedColumnName = "id")},
			   inverseJoinColumns = {@JoinColumn(name = "ROLE_ID",
												 referencedColumnName = "id")})
	List<User> users;
	
	@Column(name = "updated_at")
	LocalDateTime updatedAt;
	
	@Enumerated(EnumType.STRING)
	EntityStatus state;
	
	@JsonIgnore
	@Override
	public String getAuthority() {
		return getRoleName();
	}
}
