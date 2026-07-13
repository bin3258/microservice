package com.example.category_service;

import com.example.category_service.entity.Category;
import com.example.category_service.repository.CategoryRepository;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
@EnableFeignClients
@EnableRabbit
@EnableDiscoveryClient
public class CategoryServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CategoryServiceApplication.class, args);
	}

	@Bean
	public CommandLineRunner loadCategories(CategoryRepository repository) {
		return args -> {
			if (repository.count() == 0) {
				Category dt = repository.save(new Category(null, "Điện Thoại", "Điện thoại thông minh"));
				Category laptop = repository.save(new Category(null, "Laptop", "Laptop các hãng"));
				Category mtb = repository.save(new Category(null, "Máy tính bảng", "Máy tính bảng"));

				repository.saveAll(List.of(
						new Category(null, "Apple", "iPhone", dt.getId()),
						new Category(null, "Samsung", "Galaxy", dt.getId()),
						new Category(null, "Xiaomi", "Xiaomi", dt.getId()),
						new Category(null, "OPPO", "OPPO", dt.getId()),
						new Category(null, "Dell", "Dell", laptop.getId()),
						new Category(null, "ASUS", "ASUS", laptop.getId()),
						new Category(null, "HP", "HP", laptop.getId()),
						new Category(null, "iPad", "iPad", mtb.getId()),
						new Category(null, "Samsung Tab", "Galaxy Tab", mtb.getId())
				));
			}
		};
	}

}
