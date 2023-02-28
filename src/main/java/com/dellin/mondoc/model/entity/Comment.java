package com.dellin.mondoc.model.entity;

import com.dellin.mondoc.model.enums.EntityStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table(name = "comments")
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Comment {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(nullable = false)
	private Long id;
	
	@ManyToOne(cascade = CascadeType.ALL)
	@JsonIgnore
	@JsonManagedReference(value = "order_comments")
	Order order;
	
	@ManyToOne(cascade = CascadeType.ALL)
	@JsonIgnore
	@JsonManagedReference(value = "user_comments")
	User user;
	
	@CreationTimestamp
	@Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP",
			updatable = false)
	LocalDateTime createdAt;
	
	@Column(name = "updated_at")
	LocalDateTime updatedAt;
	
	@Enumerated(EnumType.STRING)
	EntityStatus status;
	
	String text;
}
