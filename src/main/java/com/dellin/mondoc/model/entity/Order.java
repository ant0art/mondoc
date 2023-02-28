package com.dellin.mondoc.model.entity;

import com.dellin.mondoc.model.enums.EntityStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.util.*;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table(name = "orders")
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Order {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(nullable = false)
	private Long id;
	
	@Column(name = "doc_id", unique = true, nullable = false)
	String docId;
	
	@ManyToOne(cascade = CascadeType.ALL)
	@JsonBackReference(value = "company_orders")
	Company company;
	
	@OneToMany(cascade = CascadeType.ALL)
	@JsonBackReference(value = "order_documents")
	Collection<Document> documents = new ArrayList<>();
	
	@CreationTimestamp
	@Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP",
			updatable = false)
	LocalDateTime createdAt;
	
	@Column(name = "updated_at")
	LocalDateTime updatedAt;
	
	@Enumerated(EnumType.STRING)
	EntityStatus status;
	
	String state;
	
	String uid;
	
	@OneToMany(cascade = CascadeType.ALL)
	@JsonBackReference(value = "order_comments")
	Collection<Comment> comments;
}
