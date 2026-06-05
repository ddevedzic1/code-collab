package com.codecollab.execution_service.execution.saga;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SagaEventPublisher {

	private final RabbitTemplate rabbitTemplate;

	@Value("${app.rabbitmq.events.exchange}")
	private String eventsExchange;

	@Value("${app.rabbitmq.events.execution-finalized.routing-key}")
	private String executionFinalizedRoutingKey;

	public void publishExecutionFinalized(ExecutionFinalizedEvent event) {
		publishAfterCommit(executionFinalizedRoutingKey, event, event.executionId());
	}

	private void publishAfterCommit(String routingKey, Object payload, Object correlationId) {
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					tryPublish(routingKey, payload, correlationId);
				}
			});
		} else {
			tryPublish(routingKey, payload, correlationId);
		}
	}

	private void tryPublish(String routingKey, Object payload, Object correlationId) {
		try {
			rabbitTemplate.convertAndSend(eventsExchange, routingKey, payload);
			log.debug("Published event [{}] for {}", routingKey, correlationId);
		} catch (Exception ex) {
			log.error("Failed to publish event [{}] for {}; saga may stall and need manual reconciliation",
					routingKey, correlationId, ex);
		}
	}
}
