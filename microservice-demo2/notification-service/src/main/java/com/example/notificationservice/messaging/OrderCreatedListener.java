package com.example.notificationservice.messaging;

import com.example.notificationservice.config.RabbitMqConfig;
import com.example.notificationservice.service.EmailService;
import com.example.shared.messaging.OrderCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class OrderCreatedListener {

	private static final Logger log = LoggerFactory.getLogger(OrderCreatedListener.class);

	private final EmailService emailService;

	public OrderCreatedListener(EmailService emailService) {
		this.emailService = emailService;
	}

	@RabbitListener(queues = RabbitMqConfig.ORDER_NOTIFICATION_CREATED_QUEUE)
	public void handleOrderCreated(OrderCreatedEvent event) {
		log.info("Received OrderCreatedEvent for order {}", event.getOrderId());

		List<Map<String, Object>> items = event.getItems().stream()
				.map(item -> Map.<String, Object>ofEntries(
						Map.entry("productName", item.getProductName()),
						Map.entry("productImg", item.getProductImg() != null ? item.getProductImg() : ""),
						Map.entry("ram", item.getRam() != null ? item.getRam() : ""),
						Map.entry("storage", item.getStorage() != null ? item.getStorage() : ""),
						Map.entry("screenResolution", item.getScreenResolution() != null ? item.getScreenResolution() : ""),
						Map.entry("screenTechnology", item.getScreenTechnology() != null ? item.getScreenTechnology() : ""),
						Map.entry("battery", item.getBattery() != null ? item.getBattery() : ""),
						Map.entry("color", item.getColor() != null ? item.getColor() : ""),
						Map.entry("quantity", item.getQuantity()),
						Map.entry("unitPrice", item.getUnitPrice()),
						Map.entry("lineTotal", item.getLineTotal())
				))
				.toList();

		emailService.sendOrderConfirmation(
				event.getUserEmail(),
				event.getUserName(),
				event.getOrderId(),
				items,
				event.getTotalQuantity(),
				event.getTotalPrice(),
				event.getDiscountCode(),
				event.getDiscountAmount()
		);
	}
}
