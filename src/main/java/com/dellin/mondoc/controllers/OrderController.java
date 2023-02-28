package com.dellin.mondoc.controllers;

import com.dellin.mondoc.model.pojo.OrderRequest;
import com.dellin.mondoc.service.OrderService;
import java.io.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
	
	private final OrderService orderService;
	
	@PostMapping(value = "/update", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> update(@RequestBody OrderRequest orderRequest) throws
			IOException {
		
		orderService.update(orderRequest);
		return ResponseEntity.ok().build();
	}
	
	@GetMapping("/stopUpdate")
	public ResponseEntity<?> stopUpdate() {
		
		orderService.stopUpdate();
		return ResponseEntity.ok().build();
	}
	
	@GetMapping("/all")
	public ModelMap getOrders(
			@RequestParam(required = false, defaultValue = "1") Integer page,
			@RequestParam(required = false, defaultValue = "1") Integer perPage,
			@RequestParam(required = false, defaultValue = "name") String sort,
			@RequestParam(required = false, defaultValue = "ASC") Sort.Direction order) {
		return orderService.getOrders(page, perPage, sort, order);
	}
}
