package com.example.Customer_service.service;

import com.example.Customer_service.client.AuthClient;
import com.example.Customer_service.dto.*;
import com.example.Customer_service.entity.Customer;
import com.example.Customer_service.repository.CustomerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final AuthClient authClient;

    public CustomerService(CustomerRepository customerRepository, AuthClient authClient) {
        this.customerRepository = customerRepository;
        this.authClient = authClient;
    }

    public CustomerResponse createCustomer(CreateCustomerRequest request) {
        if (customerRepository.existsByPhone(request.getPhone())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Số điện thoại đã tồn tại");
        }
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email đã tồn tại");
        }

        Map<String, Object> authRequest = Map.of(
            "username", request.getEmail(),
            "password", request.getPassword(),
            "email", request.getEmail(),
            "name", request.getFullName(),
            "phone", request.getPhone(),
            "role", "CUSTOMER"
        );

        AuthResponse authResponse;
        try {
            authResponse = authClient.register(authRequest);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Tạo tài khoản thất bại: " + e.getMessage());
        }

        Customer customer = new Customer(authResponse.getUserId(), request.getFullName(), request.getPhone(), request.getEmail());
        customer = customerRepository.save(customer);

        return toResponse(customer, customer.getEmail(), authResponse.getToken());
    }

    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(c -> toResponse(c, c.getEmail(), null))
                .toList();
    }

    public CustomerResponse getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy khách hàng"));
        return toResponse(customer, customer.getEmail(), null);
    }

    public CustomerResponse getCustomerByAuthUserId(Long authUserId) {
        Customer customer = customerRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy khách hàng"));
        return toResponse(customer, customer.getEmail(), null);
    }

    public CustomerResponse updateCustomer(Long id, UpdateCustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy khách hàng"));
        if (!request.getPhone().equals(customer.getPhone()) && customerRepository.existsByPhone(request.getPhone())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Số điện thoại đã tồn tại");
        }
        if (request.getEmail() != null && !request.getEmail().equals(customer.getEmail()) && customerRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email đã tồn tại");
        }
        customer.setFullName(request.getFullName());
        customer.setPhone(request.getPhone());
        if (request.getEmail() != null && !request.getEmail().equals(customer.getEmail())) {
            try {
                authClient.updateEmail(customer.getAuthUserId(), Map.of("email", request.getEmail()));
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Cập nhật email thất bại: " + e.getMessage());
            }
            customer.setEmail(request.getEmail());
        }
        customer = customerRepository.save(customer);
        return toResponse(customer, customer.getEmail(), null);
    }

    public void deleteCustomer(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy khách hàng");
        }
        customerRepository.deleteById(id);
    }

    private CustomerResponse toResponse(Customer customer, String email, String token) {
        return new CustomerResponse(
                customer.getId(),
                customer.getAuthUserId(),
                customer.getFullName(),
                customer.getPhone(),
                email,
                token
        );
    }
}
