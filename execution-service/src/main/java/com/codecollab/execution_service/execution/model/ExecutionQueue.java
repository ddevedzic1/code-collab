package com.codecollab.execution_service.execution.model;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "execution_queue")
@SQLRestriction("end_date IS NULL")
@Getter
@Setter
@NoArgsConstructor
public class ExecutionQueue {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", updatable = false, nullable = false)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "execution_id", nullable = false)
	private Execution execution;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 30)
	private QueueStatus status;

	@Column(name = "priority", nullable = false)
	private Integer priority;

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
