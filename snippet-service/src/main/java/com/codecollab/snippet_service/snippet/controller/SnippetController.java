package com.codecollab.snippet_service.snippet.controller;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.codecollab.snippet_service.controller.BaseController;
import com.codecollab.snippet_service.snippet.dto.SnippetCreateDto;
import com.codecollab.snippet_service.snippet.dto.SnippetResponseDto;
import com.codecollab.snippet_service.snippet.dto.SnippetUpdateDto;
import com.codecollab.snippet_service.snippet.service.SnippetService;
import com.codecollab.snippet_service.util.PageResult;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/snippets")
@RequiredArgsConstructor
public class SnippetController extends BaseController {

	private final SnippetService snippetService;

	@GetMapping
	public ResponseEntity<PageResult<SnippetResponseDto>> search(
			@RequestParam UUID userId,
			@RequestParam(required = false) String title,
			@RequestParam(required = false) UUID languageId,
			Pageable pageable) {
		return ResponseEntity.ok(snippetService.search(userId, title, languageId, pageable));
	}

	@GetMapping("/{id}")
	public ResponseEntity<SnippetResponseDto> getById(
			@PathVariable UUID id,
			@RequestHeader("X-User-Id") UUID callerUserId) {
		return ResponseEntity.ok(snippetService.getById(id, callerUserId));
	}

	@PostMapping
	public ResponseEntity<SnippetResponseDto> create(@Valid @RequestBody SnippetCreateDto dto) {
		var response = snippetService.create(dto);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PatchMapping("/{id}")
	public ResponseEntity<SnippetResponseDto> update(@PathVariable UUID id,
			@Valid @RequestBody SnippetUpdateDto dto) {
		return ResponseEntity.ok(snippetService.update(id, dto));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> softDelete(@PathVariable UUID id) {
		snippetService.softDelete(id);
		return ResponseEntity.noContent().build();
	}
}
