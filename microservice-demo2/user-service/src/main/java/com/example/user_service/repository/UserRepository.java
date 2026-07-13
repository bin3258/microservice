package com.example.user_service.repository;

import com.example.user_service.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface UserRepository extends MongoRepository<User, Long> {

	List<User> findByRole(String role);
	boolean existsByEmail(String email);
	boolean existsByPhone(String phone);
	boolean existsByEmailAndIdNot(String email, Long id);
	boolean existsByPhoneAndIdNot(String phone, Long id);
}
