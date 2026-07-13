package com.example.post_service.config;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

	public static final String POST_EXCHANGE = "post.exchange";
	public static final String POST_CREATED_ROUTING_KEY = "post.created";

	@Bean
	public DirectExchange postExchange() {
		return new DirectExchange(POST_EXCHANGE, true, false);
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
