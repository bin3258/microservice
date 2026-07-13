package com.example.user_service.messaging;

import com.example.shared.messaging.OrderCreatedEvent;
import com.example.user_service.config.RabbitMqConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class OrderCreatedListener {

	private static final Logger log = LoggerFactory.getLogger(OrderCreatedListener.class);

	@RabbitListener(queues = RabbitMqConfig.ORDER_CREATED_QUEUE)
	public void handleOrderCreated(OrderCreatedEvent event) {
		String itemSummary = event.getItems().stream()
				.map(item -> item.getProductName() + " x" + item.getQuantity())
				.collect(Collectors.joining(", "));

		log.info(
				"Async order notification handled on thread {} for order {} sent to {} <{}>. Items: {}. Total quantity: {}, total price: {}",
				Thread.currentThread().getName(),
				event.getOrderId(),
				event.getUserName(),
				event.getUserEmail(),
				itemSummary,
				event.getTotalQuantity(),
				event.getTotalPrice()
		);
	}
}
