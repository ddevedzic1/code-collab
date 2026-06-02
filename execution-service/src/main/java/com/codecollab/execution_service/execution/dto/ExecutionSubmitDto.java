package com.codecollab.execution_service.execution.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ExecutionSubmitDto {

	private UUID userId;

	@NotNull(message = "{validation.execution.snippetId.required}")
	private UUID snippetId;

	@NotNull(message = "{validation.execution.languageId.required}")
	private UUID languageId;

	@NotBlank(message = "{validation.execution.codeSnapshot.required}")
	private String codeSnapshot;
}
