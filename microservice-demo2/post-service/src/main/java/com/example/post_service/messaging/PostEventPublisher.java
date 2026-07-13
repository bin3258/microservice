package com.example.post_service.messaging;

import com.example.post_service.config.RabbitMqConfig;
import com.example.shared.messaging.PostCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class PostEventPublisher {

	private static final Logger log = LoggerFactory.getLogger(PostEventPublisher.class);

	private final RabbitTemplate rabbitTemplate;

	public PostEventPublisher(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}

	public void publishPostChanged(String action, Long postId, String title, Long categoryId, String categoryName, String status, String img) {
		PostCreatedEvent event = new PostCreatedEvent(action, postId, title, categoryId, categoryName, status, img);
		rabbitTemplate.convertAndSend(
				RabbitMqConfig.POST_EXCHANGE,
				RabbitMqConfig.POST_CREATED_ROUTING_KEY,
				event
		);
		log.info("Published post {} event for post {}", action, postId);
	}
}
