package com.example.user_service.messaging;

import com.example.shared.messaging.PostCreatedEvent;
import com.example.user_service.config.RabbitMqConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class PostCreatedListener {

	private static final Logger log = LoggerFactory.getLogger(PostCreatedListener.class);

	@RabbitListener(queues = RabbitMqConfig.POST_CREATED_QUEUE)
	public void handlePostCreated(PostCreatedEvent event) {
		String action = event.getAction() == null ? "UNKNOWN" : event.getAction();
		log.info(
				"Received post event {} for article {} in category {} ({}) with status {} and img {}",
				action,
				event.getTitle(),
				event.getCategoryId(),
				event.getCategoryName(),
				event.getStatus(),
				event.getImg()
		);
	}
}
