package com.codecollab.execution_service.execution.saga;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codecollab.execution_service.execution.model.AuditState;
import com.codecollab.execution_service.execution.repository.ExecutionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditOutcomeService {

	private final ExecutionRepository executionRepository;

	@Transactional
	public void markFinalized(UUID executionId) {
		executionRepository.findById(executionId).ifPresentOrElse(execution -> {
			if (execution.getAuditState() == AuditState.FINALIZED) {
				return;
			}
			execution.setAuditState(AuditState.FINALIZED);
			executionRepository.save(execution);
			log.info("Saga complete: execution {} finalized", executionId);
		}, () -> log.warn("Cannot finalize unknown execution {}", executionId));
	}

	@Transactional
	public void markAuditFailed(UUID executionId, String reason) {
		executionRepository.findById(executionId).ifPresentOrElse(execution -> {
			execution.setAuditState(AuditState.AUDIT_FAILED);
			executionRepository.save(execution);
			log.warn("Saga compensated: execution {} marked AUDIT_FAILED ({})", executionId, reason);
		}, () -> log.warn("Cannot compensate unknown execution {}", executionId));
	}
}
