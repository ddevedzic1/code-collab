package com.codecollab.execution_service.execution.worker;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codecollab.execution_service.execution.model.Execution;
import com.codecollab.execution_service.execution.model.ExecutionStatus;
import com.codecollab.execution_service.execution.model.QueueStatus;
import com.codecollab.execution_service.execution.repository.ExecutionQueueRepository;
import com.codecollab.execution_service.execution.repository.ExecutionRepository;
import com.codecollab.execution_service.execution.saga.ExecutionFinalizedEvent;
import com.codecollab.execution_service.execution.saga.SagaEventPublisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExecutionPhaseService {

	private final ExecutionRepository executionRepository;
	private final ExecutionQueueRepository executionQueueRepository;
	private final SagaEventPublisher sagaEventPublisher;

	@Transactional
	public boolean markRunning(UUID executionId) {
		var execution = executionRepository.findById(executionId)
				.orElseThrow(() -> new IllegalStateException("Execution not found: " + executionId));
		if (execution.getStatus() != ExecutionStatus.PENDING) {
			log.warn("Skipping execution {} in non-PENDING status {}", executionId, execution.getStatus());
			return false;
		}
		execution.setStatus(ExecutionStatus.RUNNING);
		executionRepository.save(execution);

		executionQueueRepository.findByExecutionId(executionId).ifPresent(queueEntry -> {
			queueEntry.setStatus(QueueStatus.PROCESSING);
			executionQueueRepository.save(queueEntry);
		});
		return true;
	}

	@Transactional
	public void markCompleted(UUID executionId, String stdout, String stderr, int exitCode, int durationMs) {
		var execution = executionRepository.findById(executionId)
				.orElseThrow(() -> new IllegalStateException("Execution not found: " + executionId));
		execution.setStatus(exitCode == 0 ? ExecutionStatus.COMPLETED : ExecutionStatus.FAILED);
		execution.setStdout(stdout);
		execution.setStderr(stderr);
		execution.setExitCode(exitCode);
		execution.setDurationMs(durationMs);
		executionRepository.save(execution);

		executionQueueRepository.findByExecutionId(executionId).ifPresent(queueEntry -> {
			queueEntry.setStatus(QueueStatus.DONE);
			executionQueueRepository.save(queueEntry);
		});

		publishFinalized(execution);
	}

	@Transactional
	public void markFailed(UUID executionId, String reason) {
		var execution = executionRepository.findById(executionId)
				.orElseThrow(() -> new IllegalStateException("Execution not found: " + executionId));
		execution.setStatus(ExecutionStatus.FAILED);
		execution.setStderr(reason);
		execution.setExitCode(-1);
		executionRepository.save(execution);

		executionQueueRepository.findByExecutionId(executionId).ifPresent(queueEntry -> {
			queueEntry.setStatus(QueueStatus.DONE);
			executionQueueRepository.save(queueEntry);
		});

		publishFinalized(execution);
	}

	private void publishFinalized(Execution execution) {
		var event = new ExecutionFinalizedEvent(
				execution.getId(),
				execution.getUserId(),
				execution.getSnippetId(),
				execution.getStatus().name(),
				execution.getExitCode(),
				execution.getDurationMs());
		sagaEventPublisher.publishExecutionFinalized(event);
	}
}
