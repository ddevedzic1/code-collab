package com.codecollab.system_events_service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

	@Value("${app.rabbitmq.events.exchange}")
	private String eventsExchangeName;

	@Value("${app.rabbitmq.events.execution-finalized.routing-key}")
	private String executionFinalizedRoutingKey;

	@Value("${app.rabbitmq.events.execution-finalized.queue}")
	private String executionFinalizedQueueName;

	@Bean
	public TopicExchange eventsExchange() {
		return new TopicExchange(eventsExchangeName);
	}

	@Bean
	public Queue executionFinalizedQueue() {
		return new Queue(executionFinalizedQueueName, true);
	}

	@Bean
	public Binding executionFinalizedBinding(Queue executionFinalizedQueue, TopicExchange eventsExchange) {
		return BindingBuilder.bind(executionFinalizedQueue).to(eventsExchange).with(executionFinalizedRoutingKey);
	}

	@Bean
	public MessageConverter jsonMessageConverter() {
		return new Jackson2JsonMessageConverter();
	}
}
