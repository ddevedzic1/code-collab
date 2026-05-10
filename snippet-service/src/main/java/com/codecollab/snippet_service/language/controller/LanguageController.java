package com.codecollab.snippet_service.language.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codecollab.snippet_service.controller.BaseController;
import com.codecollab.snippet_service.language.dto.LanguageCreateDto;
import com.codecollab.snippet_service.language.dto.LanguageResponseDto;
import com.codecollab.snippet_service.language.dto.LanguageUpdateDto;
import com.codecollab.snippet_service.language.service.LanguageService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/languages")
@RequiredArgsConstructor
public class LanguageController extends BaseController {

	private final LanguageService languageService;

	@GetMapping
	public ResponseEntity<List<LanguageResponseDto>> getAll() {
		return ResponseEntity.ok(languageService.getAll());
	}

	@GetMapping("/{id}")
	public ResponseEntity<LanguageResponseDto> getById(@PathVariable UUID id) {
		return ResponseEntity.ok(languageService.getById(id));
	}

	@PostMapping
	public ResponseEntity<LanguageResponseDto> create(@Valid @RequestBody LanguageCreateDto dto) {
		var response = languageService.create(dto);
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	@PatchMapping("/{id}")
	public ResponseEntity<LanguageResponseDto> update(@PathVariable UUID id,
			@Valid @RequestBody LanguageUpdateDto dto) {
		return ResponseEntity.ok(languageService.update(id, dto));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> softDelete(@PathVariable UUID id) {
		languageService.softDelete(id);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}
