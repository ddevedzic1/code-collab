package com.codecollab.system_events_service.event.model;

import java.time.LocalDateTime;
import java.util.UUID;

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
@Table(name = "system_events")
@Getter
@Setter
@NoArgsConstructor
public class SystemEvent {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", updatable = false, nullable = false)
	private UUID id;

	@Column(name = "user_id")
	private UUID userId;

	@Column(name = "microservice", nullable = false, length = 300)
	private String microservice;

	@Enumerated(EnumType.STRING)
	@Column(name = "action_type", nullable = false, length = 30)
	private ActionType actionType;

	@Column(name = "resource", nullable = false, length = 300)
	private String resource;

	@Enumerated(EnumType.STRING)
	@Column(name = "response_type", nullable = false, length = 30)
	private ResponseType responseType;

	@Column(name = "details", columnDefinition = "TEXT")
	private String details;

	@Column(name = "created_at", nullable = false, updatable = false, insertable = false)
	private LocalDateTime createdAt;

	@Column(name = "created_by", length = 300)
	private String createdBy;
}
