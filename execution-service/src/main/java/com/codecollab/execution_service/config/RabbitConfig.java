package com.codecollab.execution_service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

	@Value("${app.rabbitmq.execution.queue}")
	private String executionQueueName;

	@Value("${app.rabbitmq.execution.exchange}")
	private String executionExchangeName;

	@Value("${app.rabbitmq.execution.routing-key}")
	private String executionRoutingKey;

	@Value("${app.rabbitmq.events.exchange}")
	private String eventsExchangeName;

	@Value("${app.rabbitmq.events.audit-recorded.routing-key}")
	private String auditRecordedRoutingKey;

	@Value("${app.rabbitmq.events.audit-recorded.queue}")
	private String auditRecordedQueueName;

	@Value("${app.rabbitmq.events.audit-failed.routing-key}")
	private String auditFailedRoutingKey;

	@Value("${app.rabbitmq.events.audit-failed.queue}")
	private String auditFailedQueueName;

	@Bean
	public Queue executionQueue() {
		return new Queue(executionQueueName, true);
	}

	@Bean
	public DirectExchange executionExchange() {
		return new DirectExchange(executionExchangeName);
	}

	@Bean
	public Binding executionBinding(Queue executionQueue, DirectExchange executionExchange) {
		return BindingBuilder.bind(executionQueue).to(executionExchange).with(executionRoutingKey);
	}

	@Bean
	public TopicExchange eventsExchange() {
		return new TopicExchange(eventsExchangeName);
	}

	@Bean
	public Queue auditRecordedQueue() {
		return new Queue(auditRecordedQueueName, true);
	}

	@Bean
	public Binding auditRecordedBinding(Queue auditRecordedQueue, TopicExchange eventsExchange) {
		return BindingBuilder.bind(auditRecordedQueue).to(eventsExchange).with(auditRecordedRoutingKey);
	}

	@Bean
	public Queue auditFailedQueue() {
		return new Queue(auditFailedQueueName, true);
	}

	@Bean
	public Binding auditFailedBinding(Queue auditFailedQueue, TopicExchange eventsExchange) {
		return BindingBuilder.bind(auditFailedQueue).to(eventsExchange).with(auditFailedRoutingKey);
	}

	@Bean
	public MessageConverter jsonMessageConverter() {
		return new Jackson2JsonMessageConverter();
	}
}
