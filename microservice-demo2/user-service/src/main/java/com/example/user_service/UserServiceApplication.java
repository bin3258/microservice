package com.example.user_service;

import com.example.user_service.entity.User;
import com.example.user_service.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import java.util.List;

@SpringBootApplication
@EnableRabbit
@EnableDiscoveryClient
public class UserServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserServiceApplication.class, args);
	}

	@Bean
	public CommandLineRunner loadUsers(UserRepository repository) {
		return args -> {
			if (repository.count() == 0) {
				repository.saveAll(List.of(
					new User(1L, "Nguyen Van A", "vana@example.com", "0900000001", "ADMIN"),
					new User(2L, "Tran Thi B", "thib@example.com", "0900000002", "MANAGER"),
					new User(3L, "Le Van C", "vanc@example.com", "0900000003", "CUSTOMER")
				));
			}
		};
	}
}
