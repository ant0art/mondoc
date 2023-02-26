package com.dellin.mondoc.model.entity;

import com.dellin.mondoc.model.enums.EntityStatus;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User implements UserDetails {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(nullable = false)
	Long id;
	
	@Column(nullable = false, unique = true)
	String email;
	
	@Column(nullable = false)
	String password;
	
	@CreationTimestamp
	@Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP",
			updatable = false)
	LocalDateTime createdAt;
	
	@Column(name = "updated_at")
	LocalDateTime updatedAt;
	
	@Enumerated(EnumType.STRING)
	EntityStatus state;
	
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "users_roles",
			   joinColumns = {@JoinColumn(name = "USER_ID", referencedColumnName = "id")},
			   inverseJoinColumns = {@JoinColumn(name = "ROLE_ID",
												 referencedColumnName = "id")})
	Collection<Role> roles = new ArrayList<>();
	
	@OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	Session session;
	
	@ManyToMany
	@JoinTable(name = "users_companies",
			   joinColumns = {@JoinColumn(name = "USER_ID", referencedColumnName = "id")},
			   inverseJoinColumns = {@JoinColumn(name = "COMPANY_ID",
												 referencedColumnName = "id")})
	Collection<Company> companies = new ArrayList<>();
	
	@ManyToMany
	@JoinTable(name = "users_comments",
			   joinColumns = {@JoinColumn(name = "USER_ID", referencedColumnName = "id")},
			   inverseJoinColumns = {@JoinColumn(name = "COMMENT_ID",
												 referencedColumnName = "id")})
	Collection<Comment> comments = new ArrayList<>();
	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return getRoles();
	}
	
	@Override
	public String getUsername() {
		return email;
	}
	
	@Override
	public boolean isAccountNonExpired() {
		return true;
	}
	
	@Override
	public boolean isAccountNonLocked() {
		return true;
	}
	
	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}
	
	@Override
	public boolean isEnabled() {
		return true;
	}
}
