package com.example.apigateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

	@Bean
	public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
		return builder.routes()
				.route("banner-service", route -> route.path("/api/banners/**").uri("lb://product-service"))
				.route("banner-upload-service", route -> route.path("/uploads/banners/**").uri("lb://product-service"))
				.route("product-upload-service", route -> route.path("/uploads/products/**").uri("lb://product-service"))
				.route("post-upload-service", route -> route.path("/uploads/posts/**").uri("lb://post-service"))
				.route("order-service", route -> route.path("/api/orders/**").uri("lb://order-service"))
				.route("product-service", route -> route.path("/api/products/**").uri("lb://product-service"))
				.route("user-service", route -> route.path("/api/users/**").uri("lb://user-service"))
				.route("category-service", route -> route.path("/api/categories/**").uri("lb://category-service"))
				.route("post-service", route -> route.path("/api/posts/**").uri("lb://post-service"))
				.route("post-category-service", route -> route.path("/api/post-categories/**").uri("lb://post-service"))
				.route("auth-service", route -> route.path("/api/auth/**").uri("lb://Auth-service"))
				.route("cart-service", route -> route.path("/api/cart/**").uri("lb://Cart-service"))
				.route("search-service", route -> route.path("/api/search/**").uri("lb://Search-service"))
				.route("inventory-service", route -> route.path("/api/inventory/**").uri("lb://Inventory-service"))
				.route("warehouse-service", route -> route.path("/api/warehouses/**").uri("lb://Inventory-service"))
				.route("payment-service", route -> route.path("/api/payments/**").uri("lb://Payment-service"))
				.route("customer-service", route -> route.path("/api/customers/**").uri("lb://Customer-service"))
				.route("address-service", route -> route.path("/api/addresses/**").uri("lb://Address-service"))
				.build();
	}
}
