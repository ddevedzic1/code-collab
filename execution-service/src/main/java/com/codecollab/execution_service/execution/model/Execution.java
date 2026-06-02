package com.codecollab.execution_service.execution.model;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "executions")
@SQLRestriction("end_date IS NULL")
@Getter
@Setter
@NoArgsConstructor
public class Execution {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", updatable = false, nullable = false)
	private UUID id;

	@Column(name = "snippet_id", nullable = false)
	private UUID snippetId;

	@Column(name = "user_id", nullable = false)
	private UUID userId;

	@Column(name = "language_id", nullable = false)
	private UUID languageId;

	@Column(name = "code_snapshot", nullable = false, columnDefinition = "TEXT")
	private String codeSnapshot;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 30)
	private ExecutionStatus status;

	@Column(name = "stdout", columnDefinition = "TEXT")
	private String stdout;

	@Column(name = "stderr", columnDefinition = "TEXT")
	private String stderr;

	@Column(name = "exit_code")
	private Integer exitCode;

	@Column(name = "duration_ms")
	private Integer durationMs;

	@Column(name = "start_date", nullable = false, updatable = false, insertable = false)
	private LocalDateTime startDate;

	@Column(name = "end_date")
	private LocalDateTime endDate;

	@Column(name = "created_at", nullable = false, updatable = false, insertable = false)
	private LocalDateTime createdAt;

	@Column(name = "created_by", length = 300)
	private String createdBy;

	@Column(name = "updated_at", insertable = false, updatable = false)
	private LocalDateTime updatedAt;

	@Column(name = "updated_by", length = 300)
	private String updatedBy;
}
