package com.example.Payment_service.controller;

import com.example.Payment_service.dto.PaymentRequest;
import com.example.Payment_service.dto.PaymentResponse;
import com.example.Payment_service.dto.StatusUpdateRequest;
import com.example.Payment_service.service.PaymentService;
import com.example.Payment_service.service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService paymentService;
    private final VNPayService vnPayService;

    public PaymentController(PaymentService paymentService, VNPayService vnPayService) {
        this.paymentService = paymentService;
        this.vnPayService = vnPayService;
    }

    @GetMapping
    public List<PaymentResponse> getAll() {
        return paymentService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getById(id));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getByOrderId(@PathVariable Long orderId) {
        return ResponseEntity.ok(paymentService.getByOrderId(orderId));
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.processPayment(request));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<PaymentResponse> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request) {
        return ResponseEntity.ok(paymentService.updateStatus(id, request.getStatus()));
    }

    @PostMapping("/vnpay/create")
    public ResponseEntity<Map<String, String>> createVnpayPayment(@Valid @RequestBody PaymentRequest request,
                                                                   HttpServletRequest httpRequest) {
        String paymentUrl = vnPayService.createPaymentUrl(request.getOrderId(), request.getAmount(), request.getPaymentMethod(), httpRequest);
        Map<String, String> result = new HashMap<>();
        result.put("paymentUrl", paymentUrl);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/vnpay/return")
    public ResponseEntity<PaymentResponse> vnpayReturn(@RequestParam Map<String, String> params) {
        PaymentResponse response = vnPayService.handleReturn(params);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/vnpay/ipn")
    public ResponseEntity<String> vnpayIpn(@RequestParam Map<String, String> params) {
        vnPayService.handleIpn(params);
        return ResponseEntity.ok("OK");
    }
}
