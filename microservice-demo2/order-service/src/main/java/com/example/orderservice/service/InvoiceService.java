package com.example.orderservice.service;

import com.example.orderservice.client.PaymentServiceClient;
import com.example.orderservice.entity.Order;
import com.example.orderservice.entity.OrderItem;
import com.example.orderservice.payload.InvoiceData;
import com.example.orderservice.payload.InvoiceData.Item;
import com.example.orderservice.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class InvoiceService {

    private static final Logger log = LoggerFactory.getLogger(InvoiceService.class);

    private final OrderRepository orderRepository;
    private final TemplateEngine templateEngine;
    private final PaymentServiceClient paymentClient;

    public InvoiceService(OrderRepository orderRepository,
                          TemplateEngine templateEngine,
                          PaymentServiceClient paymentClient) {
        this.orderRepository = orderRepository;
        this.templateEngine = templateEngine;
        this.paymentClient = paymentClient;
    }

    public byte[] generateInvoice(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        InvoiceData data = buildInvoiceData(order);
        enrichWithPayment(data, orderId);

        Context ctx = new Context(new Locale("vi", "VN"));
        ctx.setVariable("invoice", data);

        String html = templateEngine.process("invoice", ctx);

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ITextRenderer renderer = new ITextRenderer();

            registerFonts(renderer);

            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF invoice", e);
        }
    }

    private void registerFonts(ITextRenderer renderer) {
        try {
            renderer.getFontResolver().addFont("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf", "Identity-H", true);
            renderer.getFontResolver().addFont("/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf", "Identity-H", true);
        } catch (Exception e) {
            log.warn("Could not register DejaVu fonts: {}", e.getMessage());
        }
    }

    private InvoiceData buildInvoiceData(Order order) {
        InvoiceData data = new InvoiceData();
        data.setOrderId(order.getId());
        data.setCreatedAt(order.getId() != null ? java.time.LocalDateTime.now() : null);
        data.setCustomerName(order.getUserName());
        data.setCustomerPhone(order.getUserPhone());
        data.setCustomerEmail(order.getUserEmail());
        data.setShippingAddress(order.getShippingAddress());
        data.setNote(order.getNote());

        List<Item> items = order.getItems().stream().map(this::toItem).collect(Collectors.toList());
        data.setItems(items);
        data.setTotalQuantity(order.getTotalQuantity());

        double subtotal = items.stream().mapToDouble(Item::getLineTotal).sum();
        data.setSubtotal(subtotal);
        data.setShippingFee(Optional.ofNullable(order.getShippingFee()).orElse(0.0));
        data.setDiscountCode(order.getDiscountCode());
        data.setDiscountAmount(Optional.ofNullable(order.getDiscountAmount()).orElse(0.0));
        data.setTotalPrice(Optional.ofNullable(order.getTotalPrice()).orElse(0.0));

        return data;
    }

    private void enrichWithPayment(InvoiceData data, Long orderId) {
        try {
            Map<String, Object> payment = paymentClient.getPaymentByOrderId(orderId);
            if (payment != null) {
                data.setPaymentMethod(payment.get("paymentMethod") != null
                        ? payment.get("paymentMethod").toString() : null);
                data.setPaymentStatus(payment.get("status") != null
                        ? payment.get("status").toString() : null);
                String txnId = payment.get("transactionId") != null
                        ? payment.get("transactionId").toString() : null;
                if (txnId == null) {
                    txnId = payment.get("vnpTransactionNo") != null
                            ? payment.get("vnpTransactionNo").toString() : null;
                }
                data.setTransactionId(txnId);
            }
        } catch (Exception e) {
            log.warn("Could not fetch payment info for order {}: {}", orderId, e.getMessage());
        }
    }

    private Item toItem(OrderItem oi) {
        Item item = new Item();
        item.setName(oi.getProductName());
        item.setQuantity(oi.getQuantity());
        item.setUnitPrice(Optional.ofNullable(oi.getUnitPrice()).orElse(0.0));
        item.setLineTotal(Optional.ofNullable(oi.getLineTotal()).orElse(0.0));
        return item;
    }
}
