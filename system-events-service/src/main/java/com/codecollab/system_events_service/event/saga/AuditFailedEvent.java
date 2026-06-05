package com.codecollab.system_events_service.event.saga;

import java.util.UUID;

public record AuditFailedEvent(UUID executionId, String reason) {
}
