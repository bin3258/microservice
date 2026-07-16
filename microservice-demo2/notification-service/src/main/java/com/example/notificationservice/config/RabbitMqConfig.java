package com.example.notificationservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

	public static final String ORDER_EXCHANGE = "order.exchange";
	public static final String ORDER_NOTIFICATION_CREATED_QUEUE = "order.notification.created.queue";
	public static final String ORDER_NOTIFICATION_STATUS_QUEUE = "order.notification.status.queue";
	public static final String ORDER_CREATED_ROUTING_KEY = "order.created";
	public static final String ORDER_STATUS_ROUTING_KEY = "order.status";

	@Bean
	public DirectExchange orderExchange() {
		return new DirectExchange(ORDER_EXCHANGE, true, false);
	}

	@Bean
	public Queue orderNotificationCreatedQueue() {
		return new Queue(ORDER_NOTIFICATION_CREATED_QUEUE, true);
	}

	@Bean
	public Queue orderNotificationStatusQueue() {
		return new Queue(ORDER_NOTIFICATION_STATUS_QUEUE, true);
	}

	@Bean
	public Binding orderCreatedBinding(@Qualifier("orderNotificationCreatedQueue") Queue orderNotificationCreatedQueue,
									   @Qualifier("orderExchange") DirectExchange orderExchange) {
		return BindingBuilder.bind(orderNotificationCreatedQueue).to(orderExchange).with(ORDER_CREATED_ROUTING_KEY);
	}

	@Bean
	public Binding orderStatusBinding(@Qualifier("orderNotificationStatusQueue") Queue orderNotificationStatusQueue,
									  @Qualifier("orderExchange") DirectExchange orderExchange) {
		return BindingBuilder.bind(orderNotificationStatusQueue).to(orderExchange).with(ORDER_STATUS_ROUTING_KEY);
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
	public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
			ConnectionFactory connectionFactory,
			Jackson2JsonMessageConverter rabbitMessageConverter) {
		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory);
		factory.setMessageConverter(rabbitMessageConverter);
		return factory;
	}
}
