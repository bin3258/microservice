package com.example.productservice.messaging;

import com.example.shared.messaging.CategoryChangedEvent;
import com.example.productservice.config.RabbitMqConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class CategoryChangedListener {

	private static final Logger log = LoggerFactory.getLogger(CategoryChangedListener.class);

	@RabbitListener(queues = RabbitMqConfig.CATEGORY_CHANGED_QUEUE)
	public void handleCategoryChanged(CategoryChangedEvent event) {
		log.info(
				"Received category change event type {} for category {} ({})",
				event.getAction(),
				event.getCategoryId(),
				event.getCategoryName()
		);
	}
}
