package com.dellin.mondoc.controllers;

import com.dellin.mondoc.service.DocumentService;
import java.io.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
public class DocumentController {
	
	private final DocumentService documentService;
	
	@PostMapping(value = "/update", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> update(@RequestParam(required = false) String uid,
			@RequestParam(required = false) String type) throws IOException {
		
		documentService.update();
		return ResponseEntity.ok().build();
	}
	
	@GetMapping("/stopUpdate")
	public ResponseEntity<?> stopUpdate() {
		documentService.stopUpdate();
		return ResponseEntity.ok().build();
	}
}
