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

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderModel {
	
	String docId;
	String companyName;
	Collection<Document> documents;
	String state;
	String uid;
	Collection<CommentHistory> comments = new ArrayList<>();
}

