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

import java.util.*;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

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
			   joinColumns = {@JoinColumn(name = "ROLE_ID", referencedColumnName = "id")},
			   inverseJoinColumns = {@JoinColumn(name = "USER_ID",
												 referencedColumnName = "id")})
	List<User> users;
	
	@Column(name = "updated_at")
	LocalDateTime updatedAt;
	
	@Enumerated(EnumType.STRING)
	EntityStatus status;
	
	@JsonIgnore
	@Override
	public String getAuthority() {
		return getRoleName();
	}
}
