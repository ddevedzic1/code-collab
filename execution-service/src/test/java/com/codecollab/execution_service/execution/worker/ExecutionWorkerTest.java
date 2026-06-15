package com.codecollab.execution_service.execution.worker;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExecutionWorkerTest {

	@Mock
	private ExecutionPhaseService phaseService;

	@Mock
	private DockerExecutionRunner dockerExecutionRunner;

	@InjectMocks
	private ExecutionWorker worker;

	private final UUID executionId = UUID.randomUUID();

	@Test
	void process_runsDocker_andMarksCompleted() {
		var context = new ExecutionContext("python:3.11-slim", "print('hi')");
		when(phaseService.markRunning(executionId)).thenReturn(context);
		when(dockerExecutionRunner.run(executionId, "python:3.11-slim", "print('hi')"))
				.thenReturn(new ExecutionResult("hi\n", null, 0, 1200, false));

		worker.process(new ExecutionMessage(executionId));

		verify(phaseService).markCompleted(executionId, "hi\n", null, 0, 1200);
	}

	@Test
	void process_skips_whenMarkRunningReturnsNull() {
		when(phaseService.markRunning(executionId)).thenReturn(null);

		worker.process(new ExecutionMessage(executionId));

		verify(dockerExecutionRunner, never()).run(any(), any(), any());
		verify(phaseService, never()).markCompleted(any(), any(), any(), anyInt(), anyInt());
	}

	@Test
	void process_marksFailed_whenRuntimeImageMissing() {
		var context = new ExecutionContext(null, "print('hi')");
		when(phaseService.markRunning(executionId)).thenReturn(context);

		worker.process(new ExecutionMessage(executionId));

		verify(dockerExecutionRunner, never()).run(any(), any(), any());
		verify(phaseService).markFailed(eq(executionId), anyString());
	}

	@Test
	void process_marksFailed_whenRunnerThrows() {
		var context = new ExecutionContext("python:3.11-slim", "print('hi')");
		when(phaseService.markRunning(executionId)).thenReturn(context);
		when(dockerExecutionRunner.run(any(), any(), any()))
				.thenThrow(new IllegalStateException("docker failed"));

		worker.process(new ExecutionMessage(executionId));

		verify(phaseService).markFailed(eq(executionId), anyString());
		verify(phaseService, never()).markCompleted(any(), any(), isNull(), anyInt(), anyInt());
	}
}
