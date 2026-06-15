package com.codecollab.system_events_service.event.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.codecollab.system_events_service.event.model.ActionType;
import com.codecollab.system_events_service.event.model.ResponseType;
import com.codecollab.system_events_service.event.model.SystemEvent;
import com.codecollab.system_events_service.event.repository.SystemEventRepository;
import com.codecollab.system_events_service.event.saga.ExecutionFinalizedEvent;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

	@Mock
	private SystemEventRepository systemEventRepository;

	@InjectMocks
	private AuditService auditService;

	private final UUID executionId = UUID.randomUUID();
	private final UUID userId = UUID.randomUUID();
	private final UUID snippetId = UUID.randomUUID();

	private ExecutionFinalizedEvent event(String status, Integer exitCode) {
		return new ExecutionFinalizedEvent(executionId, userId, snippetId, status, exitCode, 1500);
	}

	@Test
	void recordExecutionFinalized_persistsSuccessEvent_whenStatusCompleted() {
		when(systemEventRepository.existsByResourceAndActionType(executionId.toString(), ActionType.EXECUTE))
				.thenReturn(false);

		auditService.recordExecutionFinalized(event("COMPLETED", 0));

		var captor = ArgumentCaptor.forClass(SystemEvent.class);
		verify(systemEventRepository).save(captor.capture());
		var saved = captor.getValue();
		assertThat(saved.getResponseType()).isEqualTo(ResponseType.SUCCESS);
		assertThat(saved.getActionType()).isEqualTo(ActionType.EXECUTE);
		assertThat(saved.getResource()).isEqualTo(executionId.toString());
		assertThat(saved.getUserId()).isEqualTo(userId);
		assertThat(saved.getMicroservice()).isEqualTo("execution-service");
		assertThat(saved.getDetails()).contains("status=COMPLETED", "exitCode=0");
	}

	@Test
	void recordExecutionFinalized_persistsErrorEvent_whenStatusFailed() {
		when(systemEventRepository.existsByResourceAndActionType(executionId.toString(), ActionType.EXECUTE))
				.thenReturn(false);

		auditService.recordExecutionFinalized(event("FAILED", 1));

		var captor = ArgumentCaptor.forClass(SystemEvent.class);
		verify(systemEventRepository).save(captor.capture());
		assertThat(captor.getValue().getResponseType()).isEqualTo(ResponseType.ERROR);
	}

	@Test
	void recordExecutionFinalized_isIdempotent_whenAlreadyRecorded() {
		when(systemEventRepository.existsByResourceAndActionType(executionId.toString(), ActionType.EXECUTE))
				.thenReturn(true);

		auditService.recordExecutionFinalized(event("COMPLETED", 0));

		verify(systemEventRepository, never()).save(org.mockito.ArgumentMatchers.any());
	}
}
