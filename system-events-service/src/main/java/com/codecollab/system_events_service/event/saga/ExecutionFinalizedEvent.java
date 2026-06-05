package com.codecollab.system_events_service.event.saga;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ExecutionFinalizedEvent(
		UUID executionId,
		UUID userId,
		UUID snippetId,
		String status,
		Integer exitCode,
		Integer durationMs) {
}
