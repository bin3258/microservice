package com.example.orderservice;

import com.example.orderservice.entity.Order;
import com.example.orderservice.entity.OrderItem;
import com.example.orderservice.repository.OrderRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import java.util.List;

@SpringBootApplication
@EnableFeignClients
@EnableDiscoveryClient
public class OrderServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrderServiceApplication.class, args);
	}

	@Bean
	public CommandLineRunner loadOrders(OrderRepository repository) {
		return args -> {
			if (repository.count() == 0) {
				repository.saveAll(List.of(
					createSampleOrder(1L, "Nguyen Van A", "vana@example.com", "0900000001", List.of(
						new OrderItem(null, 1L, "Apple iPhone", "https://example.com/images/iphone.jpg", 2, 699.0, 1398.0),
						new OrderItem(null, 3L, "Google Pixel", "https://example.com/images/pixel.jpg", 1, 499.0, 499.0)
					)),
					createSampleOrder(2L, "Tran Thi B", "thib@example.com", "0900000002", List.of(
						new OrderItem(null, 2L, "Samsung Galaxy", "https://example.com/images/galaxy.jpg", 1, 599.0, 599.0),
						new OrderItem(null, 3L, "Google Pixel", "https://example.com/images/pixel.jpg", 2, 499.0, 998.0)
					))
				));
			}
		};
	}

	private Order createSampleOrder(Long userId, String userName, String userEmail, String userPhone, List<OrderItem> items) {
		Order order = new Order();
		order.setId(userId);
		order.setUserId(userId);
		order.setUserName(userName);
		order.setUserEmail(userEmail);
		order.setUserPhone(userPhone);
		order.setTotalQuantity(items.stream().mapToInt(OrderItem::getQuantity).sum());
		order.setTotalPrice(items.stream().mapToDouble(OrderItem::getLineTotal).sum());
		items.forEach(order::addItem);
		return order;
	}
}
