package com.example.Payment_service.service;

import com.example.Payment_service.client.OrderClient;
import com.example.Payment_service.config.VNPayConfig;
import com.example.Payment_service.config.VNPayUtil;
import com.example.Payment_service.dto.PaymentResponse;
import com.example.Payment_service.entity.Payment;
import com.example.Payment_service.repository.PaymentRepository;
import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

@Service
public class VNPayService {
    private static final Logger log = LoggerFactory.getLogger(VNPayService.class);
    private static final DateTimeFormatter VNP_DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final VNPayConfig vnpayConfig;
    private final PaymentRepository paymentRepository;
    private final OrderClient orderClient;

    public VNPayService(VNPayConfig vnpayConfig, PaymentRepository paymentRepository,
                        OrderClient orderClient) {
        this.vnpayConfig = vnpayConfig;
        this.paymentRepository = paymentRepository;
        this.orderClient = orderClient;
    }

    public String createPaymentUrl(Long orderId, Double amount, String paymentMethod, HttpServletRequest request) {
        log.info("Creating VNPay payment URL for order {}", orderId);
        Payment payment = paymentRepository.findByOrderId(orderId).orElse(null);
        if (payment == null) {
            payment = new Payment();
            payment.setOrderId(orderId);
            payment.setAmount(amount);
            payment.setStatus("PENDING");
            payment.setPaymentMethod(paymentMethod != null ? paymentMethod : "VNPAY");
            payment.setTransactionId(UUID.randomUUID().toString());
            paymentRepository.save(payment);
            log.info("Saved new payment record for order {}", orderId);
        }

        String ipAddr = getIpAddress(request);
        String txnRef = orderId + "_" + System.currentTimeMillis();
        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", vnpayConfig.getTmnCode());
        params.put("vnp_Amount", String.valueOf(Math.round(amount * 100)));
        params.put("vnp_CreateDate", LocalDateTime.now().format(VNP_DATE_FMT));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_IpAddr", ipAddr);
        params.put("vnp_Locale", "vn");
        params.put("vnp_OrderInfo", "Thanh toan don hang #" + orderId);
        params.put("vnp_OrderType", "other");
        params.put("vnp_ReturnUrl", vnpayConfig.getReturnUrl());
        params.put("vnp_TxnRef", txnRef);

        return VNPayUtil.buildPaymentUrl(vnpayConfig.getUrl(), vnpayConfig.getHashSecret(), params);
    }

    @Transactional
    public PaymentResponse handleReturn(Map<String, String> params) {
        log.info("VNPay return called with txnRef={}", params.get("vnp_TxnRef"));
        String secureHash = params.remove("vnp_SecureHash");

        boolean valid = VNPayUtil.verifySignature(vnpayConfig.getHashSecret(), params, secureHash);
        if (!valid) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid VNPay signature");
        }

        String txnRef = params.get("vnp_TxnRef");
        Long orderId = Long.parseLong(txnRef.split("_")[0]);
        String responseCode = params.get("vnp_ResponseCode");

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Không tìm thấy thanh toán cho đơn hàng " + orderId));

        payment.setVnpTxnRef(txnRef);
        payment.setVnpResponseCode(responseCode);
        payment.setVnpTransactionNo(params.get("vnp_TransactionNo"));
        payment.setVnpBankCode(params.get("vnp_BankCode"));
        payment.setVnpPayDate(params.get("vnp_PayDate"));

		if ("00".equals(responseCode)) {
            payment.setStatus("COMPLETED");
            payment.setTransactionId(params.get("vnp_TransactionNo"));
            payment = paymentRepository.save(payment);
            log.info("VNPay payment SUCCESS for order {}, calling confirmPayment", orderId);
            confirmOrderPayment(orderId);
        } else {
            payment.setStatus("FAILED");
            payment = paymentRepository.save(payment);
            log.info("VNPay payment FAILED for order {}, responseCode={}", orderId, responseCode);
        }

        return toResponse(payment);
    }

    @Transactional
    public void handleIpn(Map<String, String> params) {
        String secureHash = params.remove("vnp_SecureHash");

        boolean valid = VNPayUtil.verifySignature(vnpayConfig.getHashSecret(), params, secureHash);
        if (!valid) {
            log.warn("Invalid VNPay IPN signature for txnRef={}", params.get("vnp_TxnRef"));
            return;
        }

        String txnRef = params.get("vnp_TxnRef");
        Long orderId = Long.parseLong(txnRef.split("_")[0]);
        String responseCode = params.get("vnp_ResponseCode");

        Payment payment = paymentRepository.findByOrderId(orderId).orElse(null);
        if (payment == null) {
            log.warn("No payment found for order {} in IPN", orderId);
            return;
        }

        payment.setVnpTxnRef(txnRef);
        payment.setVnpResponseCode(responseCode);
        payment.setVnpTransactionNo(params.get("vnp_TransactionNo"));
        payment.setVnpBankCode(params.get("vnp_BankCode"));
        payment.setVnpPayDate(params.get("vnp_PayDate"));

        if ("00".equals(responseCode)) {
            payment.setStatus("COMPLETED");
            payment.setTransactionId(params.get("vnp_TransactionNo"));
            paymentRepository.save(payment);
            confirmOrderPayment(orderId);
        } else if (!"COMPLETED".equals(payment.getStatus())) {
            payment.setStatus("FAILED");
            paymentRepository.save(payment);
        }

        log.info("VNPay IPN processed for order {}: {}", orderId, responseCode);
    }

    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) ip = request.getRemoteAddr();
        return ip;
    }

    private void confirmOrderPayment(Long orderId) {
        try {
            orderClient.confirmPayment(orderId);
        } catch (FeignException e) {
            log.warn("Failed to confirm payment for order {}: {}", orderId, e.getMessage());
        }
    }

    private PaymentResponse toResponse(Payment p) {
        return new PaymentResponse(p.getId(), p.getOrderId(), p.getAmount(),
                p.getStatus(), p.getPaymentMethod(), p.getTransactionId(),
                p.getVnpTxnRef(), p.getVnpResponseCode(), p.getVnpTransactionNo(),
                p.getVnpBankCode(), p.getCreatedAt(), p.getUpdatedAt());
    }
}
