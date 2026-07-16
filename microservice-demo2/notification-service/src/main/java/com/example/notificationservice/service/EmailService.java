package com.example.notificationservice.service;

import com.example.notificationservice.dto.PasswordResetRequest;
import jakarta.mail.Message;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;
import java.util.Map;

@Service
public class EmailService {

	private static final Logger log = LoggerFactory.getLogger(EmailService.class);

	private final JavaMailSender mailSender;
	private final TemplateEngine templateEngine;
	private final String fromEmail;
	private final String fromName;

	public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine,
						@Value("${spring.mail.username}") String fromEmail,
						@Value("${app.mail.from-name}") String fromName) {
		this.mailSender = mailSender;
		this.templateEngine = templateEngine;
		this.fromEmail = fromEmail;
		this.fromName = fromName;
		log.info("EmailService initialized: fromEmail={}, fromName={}", fromEmail, fromName);
	}

	public void sendOrderConfirmation(String to, String userName, Long orderId,
									  List<Map<String, Object>> items,
									  Integer totalQuantity, Double totalPrice,
									  String discountCode, Double discountAmount) {
		try {
			Context context = new Context();
			context.setVariable("userName", userName);
			context.setVariable("orderId", orderId);
			context.setVariable("items", items);
			context.setVariable("totalQuantity", totalQuantity);
			context.setVariable("totalPrice", totalPrice);
			context.setVariable("discountCode", discountCode);
			context.setVariable("discountAmount", discountAmount);

			MimeMessage message = mailSender.createMimeMessage();
			message.setFrom(new InternetAddress(fromEmail, fromName));
			message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
			message.setSubject("Xác nhận đơn hàng #" + orderId, "UTF-8");
			message.setContent(templateEngine.process("order-confirmation", context), "text/html; charset=UTF-8");

			mailSender.send(message);
			log.info("Order confirmation email sent to {} for order {}", to, orderId);
		} catch (Exception e) {
			log.error("Failed to send order confirmation email to {}: {}", to, e.getMessage());
		}
	}

	public void sendOrderDelivered(String to, String userName, Long orderId,
								   List<Map<String, Object>> items,
								   Integer totalQuantity, Double totalPrice,
								   Double shippingFee,
								   String discountCode, Double discountAmount) {
		try {
			Context context = new Context();
			context.setVariable("userName", userName);
			context.setVariable("orderId", orderId);
			context.setVariable("items", items);
			context.setVariable("totalQuantity", totalQuantity);
			context.setVariable("totalPrice", totalPrice);
			context.setVariable("shippingFee", shippingFee != null ? shippingFee : 0);
			context.setVariable("discountCode", discountCode);
			context.setVariable("discountAmount", discountAmount);

			MimeMessage message = mailSender.createMimeMessage();
			message.setFrom(new InternetAddress(fromEmail, fromName));
			message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
			message.setSubject("Đơn hàng #" + orderId + " đã giao thành công", "UTF-8");
			message.setContent(templateEngine.process("order-delivered", context), "text/html; charset=UTF-8");

			mailSender.send(message);
			log.info("Order delivered email sent to {} for order {}", to, orderId);
		} catch (Exception e) {
			log.error("Failed to send order delivered email to {}: {}", to, e.getMessage());
		}
	}

	public void sendOrderCancelled(String to, String userName, Long orderId,
								   List<Map<String, Object>> items,
								   Integer totalQuantity, Double totalPrice,
								   Double shippingFee,
								   String discountCode, Double discountAmount) {
		try {
			Context context = new Context();
			context.setVariable("userName", userName);
			context.setVariable("orderId", orderId);
			context.setVariable("items", items);
			context.setVariable("totalQuantity", totalQuantity);
			context.setVariable("totalPrice", totalPrice);
			context.setVariable("shippingFee", shippingFee != null ? shippingFee : 0);
			context.setVariable("discountCode", discountCode);
			context.setVariable("discountAmount", discountAmount);

			MimeMessage message = mailSender.createMimeMessage();
			message.setFrom(new InternetAddress(fromEmail, fromName));
			message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
			message.setSubject("Đơn hàng #" + orderId + " đã bị hủy", "UTF-8");
			message.setContent(templateEngine.process("order-cancelled", context), "text/html; charset=UTF-8");

			mailSender.send(message);
			log.info("Order cancelled email sent to {} for order {}", to, orderId);
		} catch (Exception e) {
			log.error("Failed to send order cancelled email to {}: {}", to, e.getMessage());
		}
	}

	public void sendPasswordReset(PasswordResetRequest request) {
		try {
			Context context = new Context();
			context.setVariable("userName", request.getUserName());
			context.setVariable("resetUrl", request.getResetUrl());

			MimeMessage message = mailSender.createMimeMessage();
			message.setFrom(new InternetAddress(fromEmail, fromName));
			message.setRecipient(Message.RecipientType.TO, new InternetAddress(request.getEmail()));
			message.setSubject("Đặt lại mật khẩu", "UTF-8");
			message.setContent(templateEngine.process("password-reset", context), "text/html; charset=UTF-8");

			mailSender.send(message);
			log.info("Password reset email sent to {}", request.getEmail());
		} catch (Exception e) {
			log.error("Failed to send password reset email to {}: {}", request.getEmail(), e.getMessage());
		}
	}

}
