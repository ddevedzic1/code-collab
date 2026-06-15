package com.codecollab.execution_service.execution.worker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.codecollab.execution_service.execution.model.Execution;
import com.codecollab.execution_service.execution.model.ExecutionQueue;
import com.codecollab.execution_service.execution.model.ExecutionStatus;
import com.codecollab.execution_service.execution.model.QueueStatus;
import com.codecollab.execution_service.execution.repository.ExecutionQueueRepository;
import com.codecollab.execution_service.execution.repository.ExecutionRepository;
import com.codecollab.execution_service.execution.saga.ExecutionFinalizedEvent;
import com.codecollab.execution_service.execution.saga.SagaEventPublisher;

@ExtendWith(MockitoExtension.class)
class ExecutionPhaseServiceTest {

	@Mock
	private ExecutionRepository executionRepository;

	@Mock
	private ExecutionQueueRepository executionQueueRepository;

	@Mock
	private SagaEventPublisher sagaEventPublisher;

	@InjectMocks
	private ExecutionPhaseService phaseService;

	private Execution execution(ExecutionStatus status) {
		var execution = new Execution();
		execution.setId(UUID.randomUUID());
		execution.setUserId(UUID.randomUUID());
		execution.setSnippetId(UUID.randomUUID());
		execution.setStatus(status);
		execution.setRuntimeImage("python:3.11-slim");
		execution.setCodeSnapshot("print('hi')");
		return execution;
	}

	private ExecutionQueue queue() {
		var queue = new ExecutionQueue();
		queue.setId(UUID.randomUUID());
		queue.setStatus(QueueStatus.WAITING);
		return queue;
	}

	@Test
	void markRunning_transitionsToRunning_andReturnsContext() {
		var execution = execution(ExecutionStatus.PENDING);
		var queue = queue();
		when(executionRepository.findById(execution.getId())).thenReturn(Optional.of(execution));
		when(executionQueueRepository.findByExecutionId(execution.getId())).thenReturn(Optional.of(queue));

		var context = phaseService.markRunning(execution.getId());

		assertThat(context).isNotNull();
		assertThat(context.runtimeImage()).isEqualTo("python:3.11-slim");
		assertThat(context.codeSnapshot()).isEqualTo("print('hi')");
		assertThat(execution.getStatus()).isEqualTo(ExecutionStatus.RUNNING);
		assertThat(queue.getStatus()).isEqualTo(QueueStatus.PROCESSING);
	}

	@Test
	void markRunning_returnsNull_whenNotPending() {
		var execution = execution(ExecutionStatus.RUNNING);
		when(executionRepository.findById(execution.getId())).thenReturn(Optional.of(execution));

		var context = phaseService.markRunning(execution.getId());

		assertThat(context).isNull();
		verify(executionRepository, never()).save(any());
	}

	@Test
	void markRunning_throws_whenExecutionMissing() {
		var id = UUID.randomUUID();
		when(executionRepository.findById(id)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> phaseService.markRunning(id))
				.isInstanceOf(IllegalStateException.class);
	}

	@Test
	void markCompleted_setsCompleted_whenExitCodeZero() {
		var execution = execution(ExecutionStatus.RUNNING);
		when(executionRepository.findById(execution.getId())).thenReturn(Optional.of(execution));
		when(executionQueueRepository.findByExecutionId(execution.getId())).thenReturn(Optional.of(queue()));

		phaseService.markCompleted(execution.getId(), "out", null, 0, 1200);

		assertThat(execution.getStatus()).isEqualTo(ExecutionStatus.COMPLETED);
		assertThat(execution.getStdout()).isEqualTo("out");
		assertThat(execution.getDurationMs()).isEqualTo(1200);

		var eventCaptor = ArgumentCaptor.forClass(ExecutionFinalizedEvent.class);
		verify(sagaEventPublisher).publishExecutionFinalized(eventCaptor.capture());
		assertThat(eventCaptor.getValue().status()).isEqualTo("COMPLETED");
	}

	@Test
	void markCompleted_setsFailed_whenExitCodeNonZero() {
		var execution = execution(ExecutionStatus.RUNNING);
		when(executionRepository.findById(execution.getId())).thenReturn(Optional.of(execution));
		when(executionQueueRepository.findByExecutionId(execution.getId())).thenReturn(Optional.of(queue()));

		phaseService.markCompleted(execution.getId(), null, "boom", 1, 500);

		assertThat(execution.getStatus()).isEqualTo(ExecutionStatus.FAILED);
		assertThat(execution.getStderr()).isEqualTo("boom");
		verify(sagaEventPublisher).publishExecutionFinalized(any());
	}

	@Test
	void markFailed_setsFailedWithNegativeExitCode_andPublishes() {
		var execution = execution(ExecutionStatus.RUNNING);
		when(executionRepository.findById(execution.getId())).thenReturn(Optional.of(execution));
		when(executionQueueRepository.findByExecutionId(execution.getId())).thenReturn(Optional.of(queue()));

		phaseService.markFailed(execution.getId(), "internal error");

		assertThat(execution.getStatus()).isEqualTo(ExecutionStatus.FAILED);
		assertThat(execution.getStderr()).isEqualTo("internal error");
		assertThat(execution.getExitCode()).isEqualTo(-1);
		verify(sagaEventPublisher).publishExecutionFinalized(any());
	}
}
