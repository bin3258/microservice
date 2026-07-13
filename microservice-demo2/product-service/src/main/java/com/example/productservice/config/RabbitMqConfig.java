package com.example.productservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

	public static final String CATEGORY_EXCHANGE = "category.exchange";
	public static final String CATEGORY_CHANGED_QUEUE = "product.category.events.queue";
	public static final String CATEGORY_CHANGED_ROUTING_KEY = "category.changed";

	@Bean
	public DirectExchange categoryExchange() {
		return new DirectExchange(CATEGORY_EXCHANGE, true, false);
	}

	@Bean
	public Queue categoryChangedQueue() {
		return new Queue(CATEGORY_CHANGED_QUEUE, true);
	}

	@Bean
	public Binding categoryChangedBinding(Queue categoryChangedQueue, DirectExchange categoryExchange) {
		return BindingBuilder.bind(categoryChangedQueue).to(categoryExchange).with(CATEGORY_CHANGED_ROUTING_KEY);
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

	@Bean
	public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter rabbitMessageConverter) {
		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory);
		factory.setMessageConverter(rabbitMessageConverter);
		return factory;
	}
}
