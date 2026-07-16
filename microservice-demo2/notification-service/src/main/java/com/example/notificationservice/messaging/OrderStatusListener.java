package com.example.notificationservice.messaging;

import com.example.notificationservice.config.RabbitMqConfig;
import com.example.notificationservice.service.EmailService;
import com.example.shared.messaging.OrderStatusEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class OrderStatusListener {

	private static final Logger log = LoggerFactory.getLogger(OrderStatusListener.class);

	private final EmailService emailService;

	public OrderStatusListener(EmailService emailService) {
		this.emailService = emailService;
	}

	@RabbitListener(queues = RabbitMqConfig.ORDER_NOTIFICATION_STATUS_QUEUE)
	public void handleOrderStatus(OrderStatusEvent event) {
		String status = event.getStatus();
		log.info("Received OrderStatusEvent for order {}: {}", event.getOrderId(), status);

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

		if ("DELIVERED".equals(status)) {
			emailService.sendOrderDelivered(
					event.getUserEmail(),
					event.getUserName(),
					event.getOrderId(),
					items,
					event.getTotalQuantity(),
					event.getTotalPrice(),
					event.getShippingFee(),
					event.getDiscountCode(),
					event.getDiscountAmount()
			);
		} else if ("CANCELLED".equals(status)) {
			emailService.sendOrderCancelled(
					event.getUserEmail(),
					event.getUserName(),
					event.getOrderId(),
					items,
					event.getTotalQuantity(),
					event.getTotalPrice(),
					event.getShippingFee(),
					event.getDiscountCode(),
					event.getDiscountAmount()
			);
		}
	}
}
