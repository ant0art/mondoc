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

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Getter
@Setter
@Entity
@Table(name = "sessions")
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Session {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(nullable = false)
	private Long id;
	
	@OneToOne(cascade = CascadeType.ALL)
	User user;
	
	@Column
	String appkey;
	
	@Column(name = "login_dl")
	String login;
	
	@Column(name = "password_dl")
	String password;
	
	@Column(name = "session_dl")
	String sessionDl;
	
	@CreationTimestamp
	@Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP",
			updatable = false)
	LocalDateTime createdAt;
	
	@Column(name = "updated_at")
	LocalDateTime updatedAt;
	
	@Enumerated(EnumType.STRING)
	EntityStatus state;
}
