package com.example.orderservice.messaging;

import com.example.orderservice.config.RabbitMqConfig;
import com.example.shared.messaging.OrderCreatedEvent;
import com.example.shared.messaging.OrderStatusEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class OrderEventPublisher {

	private static final Logger log = LoggerFactory.getLogger(OrderEventPublisher.class);

	private final RabbitTemplate rabbitTemplate;

	public OrderEventPublisher(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}

	public void publishOrderCreated(OrderCreatedEvent event) {
		Runnable publishTask = () -> rabbitTemplate.convertAndSend(
				RabbitMqConfig.ORDER_EXCHANGE,
				RabbitMqConfig.ORDER_CREATED_ROUTING_KEY,
				event
		);

		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					try {
						publishTask.run();
						log.info("Published order-created event for order {}", event.getOrderId());
					} catch (Exception ex) {
						log.error("Failed to publish order-created event for order {}", event.getOrderId(), ex);
					}
				}
			});
			return;
		}

		try {
			publishTask.run();
			log.info("Published order-created event for order {}", event.getOrderId());
		} catch (Exception ex) {
			log.error("Failed to publish order-created event for order {}", event.getOrderId(), ex);
		}
	}

	public void publishOrderStatusEvent(OrderStatusEvent event) {
		try {
			rabbitTemplate.convertAndSend(
					RabbitMqConfig.ORDER_EXCHANGE,
					RabbitMqConfig.ORDER_STATUS_ROUTING_KEY,
					event
			);
			log.info("Published order-status event for order {}: {}", event.getOrderId(), event.getStatus());
		} catch (Exception ex) {
			log.error("Failed to publish order-status event for order {}", event.getOrderId(), ex);
		}
	}
}
