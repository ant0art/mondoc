package com.dellin.mondoc.service;

import com.dellin.mondoc.model.dto.CommentDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

public interface CommentService {
	
	@Transactional
	ResponseEntity<CommentDTO> create(CommentDTO commentDTO);
	
	@Transactional
	void addCommentToOrder(String docId, Long id);
	
	ResponseEntity<CommentDTO> update(CommentDTO commentDTO);
}
