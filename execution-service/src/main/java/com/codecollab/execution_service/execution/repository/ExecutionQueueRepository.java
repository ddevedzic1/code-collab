package com.codecollab.execution_service.execution.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.codecollab.execution_service.execution.model.ExecutionQueue;

public interface ExecutionQueueRepository extends JpaRepository<ExecutionQueue, UUID> {

	Optional<ExecutionQueue> findByExecutionId(UUID executionId);
}
