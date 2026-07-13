package com.example.post_service;

import com.example.post_service.entity.Post;
import com.example.post_service.entity.PostCategory;
import com.example.post_service.repository.PostCategoryRepository;
import com.example.post_service.repository.PostRepository;
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
public class PostServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PostServiceApplication.class, args);
	}

	@Bean
	public CommandLineRunner loadData(PostRepository postRepository, PostCategoryRepository categoryRepository) {
		return args -> {
			if (categoryRepository.count() == 0) {
				categoryRepository.saveAll(List.of(
						new PostCategory(null, "Tin công nghệ", "Các bài viết về công nghệ mới"),
						new PostCategory(null, "Đánh giá", "Đánh giá sản phẩm"),
						new PostCategory(null, "Khuyến mãi", "Thông tin khuyến mãi và giảm giá"),
						new PostCategory(null, "Hướng dẫn", "Hướng dẫn sử dụng và mẹo vặt")
				));
			}
			if (postRepository.count() == 0) {
				List<PostCategory> cats = categoryRepository.findAll();
				postRepository.saveAll(List.of(
						new Post(null, "New product launch", "We are launching a new electronics product line.", "https://example.com/images/post-launch.jpg", cats.get(0).getId(), "PUBLISHED"),
						new Post(null, "Reading list", "Recommended books for this quarter.", "https://example.com/images/post-books.jpg", cats.get(1).getId(), "DRAFT"),
						new Post(null, "Lifestyle tips", "Daily habits for a better routine.", "https://example.com/images/post-lifestyle.jpg", cats.get(2).getId(), "PUBLISHED")
				));
			}
		};
	}

}
