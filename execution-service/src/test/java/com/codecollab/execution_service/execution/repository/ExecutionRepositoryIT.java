package com.codecollab.execution_service.execution.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;

import com.codecollab.execution_service.execution.model.AuditState;
import com.codecollab.execution_service.execution.model.Execution;
import com.codecollab.execution_service.execution.model.ExecutionStatus;
import com.codecollab.execution_service.support.AbstractRepositoryIT;

class ExecutionRepositoryIT extends AbstractRepositoryIT {

	@Autowired
	private ExecutionRepository executionRepository;

	@Autowired
	private TestEntityManager entityManager;

	private Execution persistExecution(UUID userId, UUID snippetId, ExecutionStatus status) {
		var execution = new Execution();
		execution.setUserId(userId);
		execution.setSnippetId(snippetId);
		execution.setLanguageId(UUID.randomUUID());
		execution.setCodeSnapshot("print('hi')");
		execution.setRuntimeImage("python:3.11-slim");
		execution.setStatus(status);
		execution.setAuditState(AuditState.PENDING_AUDIT);
		return entityManager.persistAndFlush(execution);
	}

	@Test
	void search_returnsOnlyOwnExecutions() {
		var owner = UUID.randomUUID();
		persistExecution(owner, UUID.randomUUID(), ExecutionStatus.COMPLETED);
		persistExecution(UUID.randomUUID(), UUID.randomUUID(), ExecutionStatus.COMPLETED);
		entityManager.clear();

		var page = executionRepository.search(owner, null, null, PageRequest.of(0, 10));

		assertThat(page.getTotalElements()).isEqualTo(1);
	}

	@Test
	void search_filtersBySnippetId() {
		var owner = UUID.randomUUID();
		var snippetId = UUID.randomUUID();
		persistExecution(owner, snippetId, ExecutionStatus.COMPLETED);
		persistExecution(owner, UUID.randomUUID(), ExecutionStatus.COMPLETED);
		entityManager.clear();

		var page = executionRepository.search(owner, snippetId, null, PageRequest.of(0, 10));

		assertThat(page.getTotalElements()).isEqualTo(1);
		assertThat(page.getContent().get(0).getSnippetId()).isEqualTo(snippetId);
	}

	@Test
	void search_filtersByStatus() {
		var owner = UUID.randomUUID();
		persistExecution(owner, UUID.randomUUID(), ExecutionStatus.COMPLETED);
		persistExecution(owner, UUID.randomUUID(), ExecutionStatus.FAILED);
		entityManager.clear();

		var page = executionRepository.search(owner, null, ExecutionStatus.FAILED, PageRequest.of(0, 10));

		assertThat(page.getTotalElements()).isEqualTo(1);
		assertThat(page.getContent().get(0).getStatus()).isEqualTo(ExecutionStatus.FAILED);
	}

	@Test
	void search_excludesSoftDeletedExecutions() {
		var owner = UUID.randomUUID();
		var execution = persistExecution(owner, UUID.randomUUID(), ExecutionStatus.COMPLETED);
		execution.setEndDate(LocalDateTime.now());
		entityManager.persistAndFlush(execution);
		entityManager.clear();

		var page = executionRepository.search(owner, null, null, PageRequest.of(0, 10));

		assertThat(page.getTotalElements()).isZero();
	}

	@Test
	void search_runsSingleQueryWithoutNPlusOne() {
		var owner = UUID.randomUUID();
		persistExecution(owner, UUID.randomUUID(), ExecutionStatus.COMPLETED);
		persistExecution(owner, UUID.randomUUID(), ExecutionStatus.FAILED);
		persistExecution(owner, UUID.randomUUID(), ExecutionStatus.RUNNING);
		entityManager.clear();

		var stats = statistics(entityManager.getEntityManager());
		stats.clear();

		var page = executionRepository.search(owner, null, null, PageRequest.of(0, 10));
		page.getContent().forEach(Execution::getCodeSnapshot);

		assertThat(page.getTotalElements()).isEqualTo(3);
		assertThat(stats.getPrepareStatementCount()).isEqualTo(1);
	}
}
