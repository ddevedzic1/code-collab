package com.codecollab.execution_service.execution.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.codecollab.execution_service.execution.model.Execution;
import com.codecollab.execution_service.execution.model.ExecutionStatus;

public interface ExecutionRepository extends JpaRepository<Execution, UUID> {

	@Query("""
			SELECT e FROM Execution e
			WHERE e.userId = :userId
			  AND (:snippetId IS NULL OR e.snippetId = :snippetId)
			  AND (:status IS NULL OR e.status = :status)
			""")
	Page<Execution> search(
			@Param("userId") UUID userId,
			@Param("snippetId") UUID snippetId,
			@Param("status") ExecutionStatus status,
			Pageable pageable);
}
