package com.dellin.mondoc.model.dto;

import com.dellin.mondoc.model.enums.OrderDocType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentDTO {
	
	String uid;
	
	OrderDocType type;
	
	String base64;
	
	String url;
}
