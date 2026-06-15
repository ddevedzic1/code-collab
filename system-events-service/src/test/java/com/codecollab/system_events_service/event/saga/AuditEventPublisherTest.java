package com.codecollab.system_events_service.event.saga;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuditEventPublisherTest {

	@Mock
	private RabbitTemplate rabbitTemplate;

	@InjectMocks
	private AuditEventPublisher publisher;

	private final UUID executionId = UUID.randomUUID();

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(publisher, "eventsExchange", "events.exchange");
		ReflectionTestUtils.setField(publisher, "auditRecordedRoutingKey", "events.audit.recorded");
		ReflectionTestUtils.setField(publisher, "auditFailedRoutingKey", "events.audit.failed");
	}

	@Test
	void publishAuditRecorded_sendsRecordedEventToCorrectRoute() {
		publisher.publishAuditRecorded(executionId);

		var payloadCaptor = ArgumentCaptor.forClass(AuditRecordedEvent.class);
		verify(rabbitTemplate).convertAndSend(
				org.mockito.ArgumentMatchers.eq("events.exchange"),
				org.mockito.ArgumentMatchers.eq("events.audit.recorded"),
				payloadCaptor.capture());
		assertThat(payloadCaptor.getValue().executionId()).isEqualTo(executionId);
	}

	@Test
	void publishAuditFailed_sendsFailedEventWithReason() {
		publisher.publishAuditFailed(executionId, "boom");

		var payloadCaptor = ArgumentCaptor.forClass(AuditFailedEvent.class);
		verify(rabbitTemplate).convertAndSend(
				org.mockito.ArgumentMatchers.eq("events.exchange"),
				org.mockito.ArgumentMatchers.eq("events.audit.failed"),
				payloadCaptor.capture());
		assertThat(payloadCaptor.getValue().executionId()).isEqualTo(executionId);
		assertThat(payloadCaptor.getValue().reason()).isEqualTo("boom");
	}
}
