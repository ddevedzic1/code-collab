package com.codecollab.execution_service.execution.saga;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditOutcomeListener {

	private final AuditOutcomeService auditOutcomeService;

	@RabbitListener(queues = "${app.rabbitmq.events.audit-recorded.queue}")
	public void onAuditRecorded(AuditRecordedEvent event) {
		log.info("Received audit-recorded for execution {}", event.executionId());
		auditOutcomeService.markFinalized(event.executionId());
	}

	@RabbitListener(queues = "${app.rabbitmq.events.audit-failed.queue}")
	public void onAuditFailed(AuditFailedEvent event) {
		log.info("Received audit-failed for execution {}", event.executionId());
		auditOutcomeService.markAuditFailed(event.executionId(), event.reason());
	}
}
