package com.dellin.mondoc.exceptions;

import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.*;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class GlobalException {

	@Bean
	public ErrorAttributes errorAttributes() {
		return new DefaultErrorAttributes() {
			@Override
			public Map<String, Object> getErrorAttributes(WebRequest webRequest,
					ErrorAttributeOptions options) {
				return super.getErrorAttributes(webRequest,
						ErrorAttributeOptions.defaults()
								.including(ErrorAttributeOptions.Include.MESSAGE));
			}
		};
	}

	@ExceptionHandler(CustomException.class)
	public void handleCustomException(HttpServletResponse response, CustomException ex)
			throws IOException {
		response.sendError(ex.getStatus().value(), ex.getMessage());
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<ErrorMessage> handleMissingParams(
			MissingServletRequestParameterException ex) {
		String parameter = ex.getParameterName();

		log.error("{} parameter is missing", parameter);
		return ResponseEntity.status(404).body(new ErrorMessage(
				String.format("parameter is missing: %s", parameter)));
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ErrorMessage> handleMismatchParams(
			MethodArgumentTypeMismatchException ex) {
		String parameter = ex.getParameter().getParameterName();

		log.error("wrong data for parameter: {}", parameter);
		return ResponseEntity.status(404).body(new ErrorMessage(
				String.format("wrong data for parameter: %s", parameter)));
	}
}
