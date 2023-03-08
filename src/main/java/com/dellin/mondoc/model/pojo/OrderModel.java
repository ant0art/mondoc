package com.dellin.mondoc.model.pojo;

import com.dellin.mondoc.model.entity.Document;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.*;

/**
 * Model view class of database Order
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderModel {
	
	/**
	 * The field of Order docId
	 */
	String docId;
	/**
	 * The field of payer-company name
	 */
	String companyName;
	/**
	 * The field collection of available documents
	 */
	Collection<Document> documents;
	/**
	 * The field of state of order
	 */
	String state;
	/**
	 * The field of Order and Document UID
	 */
	String uid;
	/**
	 * The field collection of available order comments
	 */
	Collection<CommentHistory> comments = new ArrayList<>();
}

