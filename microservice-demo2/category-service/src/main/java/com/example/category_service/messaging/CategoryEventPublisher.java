package com.example.category_service.messaging;

import com.example.category_service.config.RabbitMqConfig;
import com.example.shared.messaging.CategoryChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class CategoryEventPublisher {

	private static final Logger log = LoggerFactory.getLogger(CategoryEventPublisher.class);

	private final RabbitTemplate rabbitTemplate;

	public CategoryEventPublisher(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}

	public void publishCategoryChanged(String action, Long categoryId, String categoryName, String description) {
		CategoryChangedEvent event = new CategoryChangedEvent(action, categoryId, categoryName, description);
		rabbitTemplate.convertAndSend(
				RabbitMqConfig.CATEGORY_EXCHANGE,
				RabbitMqConfig.CATEGORY_CHANGED_ROUTING_KEY,
				event
		);
		log.info("Published category change event: {} for category {}", action, categoryId);
	}
}
