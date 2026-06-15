package com.codecollab.system_events_service.event.saga;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.codecollab.system_events_service.event.service.AuditService;

@ExtendWith(MockitoExtension.class)
class ExecutionFinalizedListenerTest {

	@Mock
	private AuditService auditService;

	@Mock
	private AuditEventPublisher auditEventPublisher;

	@InjectMocks
	private ExecutionFinalizedListener listener;

	private final UUID executionId = UUID.randomUUID();

	private ExecutionFinalizedEvent event() {
		return new ExecutionFinalizedEvent(executionId, UUID.randomUUID(), UUID.randomUUID(), "COMPLETED", 0, 1000);
	}

	@Test
	void onExecutionFinalized_recordsAudit_andPublishesRecorded() {
		listener.onExecutionFinalized(event());

		verify(auditService).recordExecutionFinalized(any());
		verify(auditEventPublisher).publishAuditRecorded(executionId);
		verify(auditEventPublisher, never()).publishAuditFailed(any(), anyString());
	}

	@Test
	void onExecutionFinalized_compensates_whenAuditFails() {
		doThrow(new RuntimeException("db down")).when(auditService).recordExecutionFinalized(any());

		listener.onExecutionFinalized(event());

		verify(auditEventPublisher).publishAuditFailed(eq(executionId), anyString());
		verify(auditEventPublisher, never()).publishAuditRecorded(any());
	}
}
