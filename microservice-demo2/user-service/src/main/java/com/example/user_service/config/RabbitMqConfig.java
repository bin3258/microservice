package com.example.user_service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

	public static final String ORDER_EXCHANGE = "order.exchange";
	public static final String ORDER_CREATED_QUEUE = "order.created.queue";
	public static final String ORDER_CREATED_ROUTING_KEY = "order.created";
	public static final String POST_EXCHANGE = "post.exchange";
	public static final String POST_CREATED_QUEUE = "post.created.queue";
	public static final String POST_CREATED_ROUTING_KEY = "post.created";

	@Bean
	public DirectExchange orderExchange() {
		return new DirectExchange(ORDER_EXCHANGE, true, false);
	}

	@Bean
	public Queue orderCreatedQueue() {
		return new Queue(ORDER_CREATED_QUEUE, true);
	}

	@Bean
	public Binding orderCreatedBinding(@Qualifier("orderCreatedQueue") Queue orderCreatedQueue, @Qualifier("orderExchange") DirectExchange orderExchange) {
		return BindingBuilder.bind(orderCreatedQueue).to(orderExchange).with(ORDER_CREATED_ROUTING_KEY);
	}

	@Bean
	public DirectExchange postExchange() {
		return new DirectExchange(POST_EXCHANGE, true, false);
	}

	@Bean
	public Queue postCreatedQueue() {
		return new Queue(POST_CREATED_QUEUE, true);
	}

	@Bean
	public Binding postCreatedBinding(@Qualifier("postCreatedQueue") Queue postCreatedQueue, @Qualifier("postExchange") DirectExchange postExchange) {
		return BindingBuilder.bind(postCreatedQueue).to(postExchange).with(POST_CREATED_ROUTING_KEY);
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
	public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter rabbitMessageConverter) {
		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory);
		factory.setMessageConverter(rabbitMessageConverter);
		return factory;
	}
}
