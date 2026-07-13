package com.example.apigateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtAuthGatewayFilter implements GlobalFilter, Ordered {

    private static final String[] PUBLIC_PATHS = {"/api/auth", "/api/products", "/api/banners", "/api/categories", "/api/post-categories", "/api/posts", "/api/search", "/uploads", "/api/customers"};

    private final SecretKey secretKey;

    public JwtAuthGatewayFilter(@Value("${jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            exchange = exchange.mutate()
                    .request(r -> r
                            .header("X-User-Id", claims.get("userId", Long.class).toString())
                            .header("X-User-Role", claims.get("role", String.class))
                            .header("X-Username", claims.getSubject())
                            .header("X-Authorization", authHeader))
                    .build();

            return chain.filter(exchange);
        } catch (JwtException e) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private boolean isPublicPath(String path) {
        if (path.equals("/api/auth/login") || path.equals("/api/auth/register")
                || path.equals("/api/auth/refresh") || path.equals("/api/auth/validate")) {
            return true;
        }
        for (String p : PUBLIC_PATHS) {
            if (path.startsWith(p) && !path.startsWith("/api/auth/")) return true;
        }
        return false;
    }
}
