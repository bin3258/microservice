package com.example.productservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(-100)
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("X-Authorization");
        if (authHeader == null) {
            authHeader = request.getHeader("Authorization");
        }

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String role = jwtUtil.getRoleFromToken(token);
            Long userId = jwtUtil.getUserIdFromToken(token);
            String username = jwtUtil.getUsernameFromToken(token);

            if (role != null) {
                request.setAttribute("X-User-Role", role);
                request.setAttribute("X-User-Id", userId);
                request.setAttribute("X-Username", username);
            }
        }

        String roleHeader = request.getHeader("X-User-Role");
        if (roleHeader != null && request.getAttribute("X-User-Role") == null) {
            request.setAttribute("X-User-Role", roleHeader);
        }
        String userIdHeader = request.getHeader("X-User-Id");
        if (userIdHeader != null && request.getAttribute("X-User-Id") == null) {
            request.setAttribute("X-User-Id", Long.parseLong(userIdHeader));
        }

        filterChain.doFilter(request, response);
    }
}
