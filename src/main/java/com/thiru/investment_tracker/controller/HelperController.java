package com.thiru.investment_tracker.controller;

import java.io.ByteArrayInputStream;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thiru.investment_tracker.service.PortfolioService;
import com.thiru.investment_tracker.util.transaction.TransactionHeaders;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@RequestMapping("/helper")
@RestController
public class HelperController {
	private final PortfolioService portfolioService;

	@GetMapping("/template")
	public ResponseEntity<InputStreamResource> getTemplate() {

		String fileName = "template.xlsx";
		ByteArrayInputStream inputStream = portfolioService.downloadTemplate();
		InputStreamResource resource = new InputStreamResource(inputStream);
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
				.contentType(MediaType.parseMediaType("application/vnd.ms-excel")).body(resource);
	}

	@GetMapping("/template/fields")
	public ResponseEntity<String[]> getTemplateFields() {

		String[] templateFields = TransactionHeaders.getHeaders();
		return ResponseEntity.ok(templateFields);
	}
}
