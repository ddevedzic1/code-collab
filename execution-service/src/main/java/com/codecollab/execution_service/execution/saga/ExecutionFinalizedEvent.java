package com.codecollab.execution_service.execution.saga;

import java.util.UUID;

public record ExecutionFinalizedEvent(
		UUID executionId,
		UUID userId,
		UUID snippetId,
		String status,
		Integer exitCode,
		Integer durationMs) {
}
