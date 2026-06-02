package com.codecollab.execution_service.execution.dto;

import java.util.UUID;

import com.codecollab.execution_service.execution.model.ExecutionStatus;

import lombok.Data;

@Data
public class ExecutionResponseDto {

	private UUID id;
	private UUID userId;
	private UUID snippetId;
	private UUID languageId;
	private String codeSnapshot;
	private ExecutionStatus status;
	private String stdout;
	private String stderr;
	private Integer exitCode;
	private Integer durationMs;
}
