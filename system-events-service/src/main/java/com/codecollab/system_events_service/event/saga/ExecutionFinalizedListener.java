package com.codecollab.system_events_service.event.saga;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.codecollab.system_events_service.event.service.AuditService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExecutionFinalizedListener {

	private final AuditService auditService;
	private final AuditEventPublisher auditEventPublisher;

	@RabbitListener(queues = "${app.rabbitmq.events.execution-finalized.queue}")
	public void onExecutionFinalized(ExecutionFinalizedEvent event) {
		var executionId = event.executionId();
		log.info("Received execution-finalized event for execution {}", executionId);
		try {
			auditService.recordExecutionFinalized(event);
			auditEventPublisher.publishAuditRecorded(executionId);
		} catch (Exception ex) {
			log.error("Failed to record audit for execution {}; compensating", executionId, ex);
			auditEventPublisher.publishAuditFailed(executionId, ex.getMessage());
		}
	}
}
