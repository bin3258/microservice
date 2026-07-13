package com.example.productservice;

import com.example.productservice.entity.DatabaseSequence;
import com.example.productservice.entity.Product;
import com.example.productservice.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import java.util.List;

@SpringBootApplication
@EnableFeignClients
@EnableRabbit
@EnableDiscoveryClient
public class ProductServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductServiceApplication.class, args);
	}

	@Bean
	public CommandLineRunner loadProducts(ProductRepository repository, MongoOperations mongoOperations) {
		return args -> {
			if (repository.count() == 0) {
				repository.saveAll(List.of(
					new Product(1L, "Apple iPhone", 699.0, "https://example.com/images/iphone.jpg", 1L),
					new Product(2L, "Samsung Galaxy", 599.0, "https://example.com/images/galaxy.jpg", 1L),
					new Product(3L, "Google Pixel", 499.0, "https://example.com/images/pixel.jpg", 1L)
				));
				DatabaseSequence seq = new DatabaseSequence();
				seq.setId("product_seq");
				seq.setSeq(3L);
				mongoOperations.save(seq);
			}
		};
	}
}
