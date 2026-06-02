package com.codecollab.execution_service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

	@Value("${app.rabbitmq.execution.queue}")
	private String queueName;

	@Value("${app.rabbitmq.execution.exchange}")
	private String exchangeName;

	@Value("${app.rabbitmq.execution.routing-key}")
	private String routingKey;

	@Bean
	public Queue executionQueue() {
		return new Queue(queueName, true);
	}

	@Bean
	public DirectExchange executionExchange() {
		return new DirectExchange(exchangeName);
	}

	@Bean
	public Binding executionBinding(Queue executionQueue, DirectExchange executionExchange) {
		return BindingBuilder.bind(executionQueue).to(executionExchange).with(routingKey);
	}

	@Bean
	public MessageConverter jsonMessageConverter() {
		return new Jackson2JsonMessageConverter();
	}
}
