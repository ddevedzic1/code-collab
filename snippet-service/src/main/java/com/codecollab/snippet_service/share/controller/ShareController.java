package com.codecollab.snippet_service.share.controller;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.codecollab.snippet_service.controller.BaseController;
import com.codecollab.snippet_service.share.dto.ShareCreateDto;
import com.codecollab.snippet_service.share.dto.ShareResponseDto;
import com.codecollab.snippet_service.share.dto.ShareUpdateDto;
import com.codecollab.snippet_service.share.dto.ShareUserCreateDto;
import com.codecollab.snippet_service.share.dto.ShareUserResponseDto;
import com.codecollab.snippet_service.share.dto.SharedSnippetResponseDto;
import com.codecollab.snippet_service.share.service.ShareService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ShareController extends BaseController {

	private final ShareService shareService;

	@PostMapping("/snippets/{snippetId}/share")
	public ResponseEntity<ShareResponseDto> create(@PathVariable UUID snippetId,
			@Valid @RequestBody ShareCreateDto dto) {
		var response = shareService.create(snippetId, dto);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/snippets/{snippetId}/share")
	public ResponseEntity<ShareResponseDto> getBySnippetId(@PathVariable UUID snippetId) {
		return ResponseEntity.ok(shareService.getBySnippetId(snippetId));
	}

	@GetMapping("/shares/{shareId}")
	public ResponseEntity<ShareResponseDto> getById(@PathVariable UUID shareId) {
		return ResponseEntity.ok(shareService.getById(shareId));
	}

	@PatchMapping("/shares/{shareId}")
	public ResponseEntity<ShareResponseDto> update(@PathVariable UUID shareId,
			@Valid @RequestBody ShareUpdateDto dto) {
		return ResponseEntity.ok(shareService.update(shareId, dto));
	}

	@DeleteMapping("/shares/{shareId}")
	public ResponseEntity<Void> softDelete(@PathVariable UUID shareId) {
		shareService.softDelete(shareId);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/shares/{shareId}/users")
	public ResponseEntity<List<ShareUserResponseDto>> getUsers(@PathVariable UUID shareId) {
		return ResponseEntity.ok(shareService.getUsers(shareId));
	}

	@PostMapping("/shares/{shareId}/users")
	public ResponseEntity<ShareUserResponseDto> addUser(@PathVariable UUID shareId,
			@Valid @RequestBody ShareUserCreateDto dto) {
		var response = shareService.addUser(shareId, dto);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@DeleteMapping("/shares/{shareId}/users/{userId}")
	public ResponseEntity<Void> removeUser(@PathVariable UUID shareId, @PathVariable UUID userId) {
		shareService.removeUser(shareId, userId);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/shares/{shareId}/users")
	public ResponseEntity<Void> removeAllUsers(@PathVariable UUID shareId) {
		shareService.removeAllUsers(shareId);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/shares/by-token/{token}")
	public ResponseEntity<SharedSnippetResponseDto> getByToken(@PathVariable String token,
			@RequestParam(required = false) UUID userId) {
		return ResponseEntity.ok(shareService.getByToken(token, userId));
	}
}
