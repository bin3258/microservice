package com.example.orderservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

	public static final String ORDER_EXCHANGE = "order.exchange";
	public static final String ORDER_CREATED_QUEUE = "order.created.queue";
	public static final String ORDER_CREATED_ROUTING_KEY = "order.created";

	@Bean
	public DirectExchange orderExchange() {
		return new DirectExchange(ORDER_EXCHANGE, true, false);
	}

	@Bean
	public Queue orderCreatedQueue() {
		return new Queue(ORDER_CREATED_QUEUE, true);
	}

	@Bean
	public Binding orderCreatedBinding(Queue orderCreatedQueue, DirectExchange orderExchange) {
		return BindingBuilder.bind(orderCreatedQueue).to(orderExchange).with(ORDER_CREATED_ROUTING_KEY);
	}

	@Bean
	public Jackson2JsonMessageConverter rabbitMessageConverter() {
		Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
		DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
		typeMapper.setTrustedPackages("com.example.shared");
		converter.setJavaTypeMapper(typeMapper);
		return converter;
	}

	@Bean
	public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter rabbitMessageConverter) {
		RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
		rabbitTemplate.setMessageConverter(rabbitMessageConverter);
		return rabbitTemplate;
	}
}
