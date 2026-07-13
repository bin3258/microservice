package com.example.Payment_service.service;

import com.example.Payment_service.client.OrderClient;
import com.example.Payment_service.dto.OrderStatusRequest;
import com.example.Payment_service.dto.PaymentRequest;
import com.example.Payment_service.dto.PaymentResponse;
import com.example.Payment_service.entity.Payment;
import com.example.Payment_service.messaging.PaymentEventPublisher;
import com.example.Payment_service.repository.PaymentRepository;
import com.example.shared.messaging.PaymentCompletedEvent;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {
    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    private final PaymentRepository paymentRepository;
    private final OrderClient orderClient;
    private final PaymentEventPublisher paymentEventPublisher;

    public PaymentService(PaymentRepository paymentRepository, OrderClient orderClient, PaymentEventPublisher paymentEventPublisher) {
        this.paymentRepository = paymentRepository;
        this.orderClient = orderClient;
        this.paymentEventPublisher = paymentEventPublisher;
    }

    public List<PaymentResponse> getAll() {
        return paymentRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public PaymentResponse updateStatus(Long id, String newStatus) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thanh toán với mã " + id));
        payment.setStatus(newStatus);
        return toResponse(paymentRepository.save(payment));
    }

    public PaymentResponse getById(Long id) {
        return paymentRepository.findById(id).map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thanh toán"));
    }

    public PaymentResponse getByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId).map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thanh toán cho đơn hàng " + orderId));
    }

    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        if (paymentRepository.findByOrderId(request.getOrderId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Đã tồn tại thanh toán cho đơn hàng " + request.getOrderId());
        }

        Payment payment = new Payment();
        payment.setOrderId(request.getOrderId());
        payment.setAmount(request.getAmount());
        payment.setStatus("PROCESSING");
        payment.setTransactionId(request.getTransactionId() != null ? request.getTransactionId() : UUID.randomUUID().toString());
        payment = paymentRepository.save(payment);

        boolean success = simulatePaymentGateway();

        if (success) {
            payment.setStatus("COMPLETED");
            payment = paymentRepository.save(payment);
            notifyOrderService(payment.getOrderId(), "PAID");
            publishPaymentEvent(payment, "COMPLETED");
        } else {
            payment.setStatus("FAILED");
            payment = paymentRepository.save(payment);
            publishPaymentEvent(payment, "FAILED");
        }

        return toResponse(payment);
    }

    private boolean simulatePaymentGateway() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return Math.random() > 0.1;
    }

    private void notifyOrderService(Long orderId, String status) {
        try {
            orderClient.updateOrderStatus(orderId, new OrderStatusRequest(orderId, status));
        } catch (FeignException e) {
            log.warn("Failed to notify order-service about order {} status {}", orderId, status);
        }
    }

    private void publishPaymentEvent(Payment payment, String status) {
        PaymentCompletedEvent event = new PaymentCompletedEvent(
                payment.getId(), payment.getOrderId(), status,
                payment.getAmount(), payment.getTransactionId());
        paymentEventPublisher.publishPaymentCompleted(event);
    }

    private PaymentResponse toResponse(Payment p) {
        return new PaymentResponse(p.getId(), p.getOrderId(), p.getAmount(),
                p.getStatus(), p.getTransactionId(), p.getCreatedAt(), p.getUpdatedAt());
    }
}
