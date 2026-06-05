package com.codecollab.system_events_service.event.saga;

import java.util.UUID;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditEventPublisher {

	private final RabbitTemplate rabbitTemplate;

	@Value("${app.rabbitmq.events.exchange}")
	private String eventsExchange;

	@Value("${app.rabbitmq.events.audit-recorded.routing-key}")
	private String auditRecordedRoutingKey;

	@Value("${app.rabbitmq.events.audit-failed.routing-key}")
	private String auditFailedRoutingKey;

	public void publishAuditRecorded(UUID executionId) {
		rabbitTemplate.convertAndSend(eventsExchange, auditRecordedRoutingKey, new AuditRecordedEvent(executionId));
		log.debug("Published audit-recorded for execution {}", executionId);
	}

	public void publishAuditFailed(UUID executionId, String reason) {
		rabbitTemplate.convertAndSend(eventsExchange, auditFailedRoutingKey, new AuditFailedEvent(executionId, reason));
		log.debug("Published audit-failed for execution {}", executionId);
	}
}
