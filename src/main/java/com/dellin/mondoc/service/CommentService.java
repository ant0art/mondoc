package com.dellin.mondoc.service;

import com.dellin.mondoc.model.dto.CommentDTO;
import org.springframework.http.ResponseEntity;

public interface CommentService {
	
	ResponseEntity<CommentDTO> create(CommentDTO commentDTO);
	
	void addCommentToOrder(String docId, Long id);
	
	ResponseEntity<CommentDTO> update(CommentDTO commentDTO);
}
