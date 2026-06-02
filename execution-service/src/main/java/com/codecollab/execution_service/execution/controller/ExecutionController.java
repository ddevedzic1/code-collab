package com.codecollab.execution_service.execution.controller;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.codecollab.execution_service.controller.BaseController;
import com.codecollab.execution_service.execution.dto.ExecutionResponseDto;
import com.codecollab.execution_service.execution.dto.ExecutionSubmitDto;
import com.codecollab.execution_service.execution.model.ExecutionStatus;
import com.codecollab.execution_service.execution.service.ExecutionService;
import com.codecollab.execution_service.util.PageResult;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/executions")
@RequiredArgsConstructor
public class ExecutionController extends BaseController {

	private final ExecutionService executionService;

	@PostMapping
	public ResponseEntity<ExecutionResponseDto> submit(@Valid @RequestBody ExecutionSubmitDto dto) {
		var response = executionService.submit(dto);
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
	}

	@GetMapping("/{id}")
	public ResponseEntity<ExecutionResponseDto> getById(@PathVariable UUID id) {
		return ResponseEntity.ok(executionService.getById(id));
	}

	@GetMapping
	public ResponseEntity<PageResult<ExecutionResponseDto>> search(
			@RequestParam UUID userId,
			@RequestParam(required = false) UUID snippetId,
			@RequestParam(required = false) ExecutionStatus status,
			Pageable pageable) {
		return ResponseEntity.ok(executionService.search(userId, snippetId, status, pageable));
	}
}
