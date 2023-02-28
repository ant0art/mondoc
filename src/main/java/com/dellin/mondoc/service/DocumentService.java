package com.dellin.mondoc.service;

import com.dellin.mondoc.model.entity.Document;
import com.dellin.mondoc.model.pojo.DocumentResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

public interface DocumentService {
	
	@Transactional
	void update();
	
	void stopUpdate();
	
	void updateDocData(Document document, Collection<DocumentResponse.Data> data);
	
	List<Document> getDocsByBase64Null();
}
