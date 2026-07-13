package com.example.Payment_service.messaging;

import com.example.Payment_service.config.RabbitMqConfig;
import com.example.shared.messaging.PaymentCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class PaymentEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(PaymentEventPublisher.class);
    private final RabbitTemplate rabbitTemplate;

    public PaymentEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishPaymentCompleted(PaymentCompletedEvent event) {
        rabbitTemplate.convertAndSend(RabbitMqConfig.PAYMENT_EXCHANGE, RabbitMqConfig.PAYMENT_COMPLETED_ROUTING_KEY, event);
        log.info("Published payment completed event for order {}", event.getOrderId());
    }
}
