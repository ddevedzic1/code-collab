package com.codecollab.execution_service.execution.saga;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AuditFailedEvent(UUID executionId, String reason) {
}
