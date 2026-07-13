package com.example.user_service.service;

import com.example.user_service.dto.UserRequest;
import com.example.user_service.dto.UserResponse;
import com.example.user_service.entity.User;
import com.example.user_service.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UserService {

	private final UserRepository userRepository;
	private final SequenceGeneratorService sequenceGenerator;

	public UserService(UserRepository userRepository, SequenceGeneratorService sequenceGenerator) {
		this.userRepository = userRepository;
		this.sequenceGenerator = sequenceGenerator;
	}

	public List<UserResponse> getAllUsers() {
		return userRepository.findAll().stream()
				.map(this::toResponse)
				.toList();
	}

	public List<UserResponse> getUsersByRole(String role) {
		return userRepository.findByRole(normalizeRole(role)).stream()
				.map(this::toResponse)
				.toList();
	}

	public UserResponse getUserById(Long id) {
		return userRepository.findById(id)
				.map(this::toResponse)
				.orElse(null);
	}

	public UserResponse createUser(UserRequest request) {
		validateRequest(request);
		if (userRepository.existsByEmail(request.getEmail())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Email đã tồn tại");
		}
		if (userRepository.existsByPhone(request.getPhone())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Số điện thoại đã tồn tại");
		}
		User user = new User();
		user.setId(request.getId() != null ? request.getId() : sequenceGenerator.generateSequence("user_seq"));
		user.setName(request.getName());
		user.setEmail(request.getEmail());
		user.setPhone(request.getPhone());
		user.setRole(normalizeRole(request.getRole()));
		return toResponse(userRepository.save(user));
	}

	public UserResponse updateUser(Long id, UserRequest request) {
		validateRequest(request);
		User user = userRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng với mã " + id));
		if (userRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Email đã tồn tại");
		}
		if (userRepository.existsByPhoneAndIdNot(request.getPhone(), id)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Số điện thoại đã tồn tại");
		}
		user.setName(request.getName());
		user.setEmail(request.getEmail());
		user.setPhone(request.getPhone());
		user.setRole(normalizeRole(request.getRole()));
		return toResponse(userRepository.save(user));
	}

	public UserResponse updateRole(Long id, String role) {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng với mã " + id));
		user.setRole(normalizeRole(role));
		return toResponse(userRepository.save(user));
	}

	public void deleteUser(Long id) {
		if (!userRepository.existsById(id)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng với mã " + id);
		}
		userRepository.deleteById(id);
	}

	private UserResponse toResponse(User user) {
		return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getPhone(), user.getRole());
	}

	private void validateRequest(UserRequest request) {
		if (request == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Yêu cầu người dùng không được để trống");
		}
		if (request.getName() == null || request.getName().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vui lòng nhập tên người dùng");
		}
		if (request.getEmail() == null || request.getEmail().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vui lòng nhập email");
		}
		if (request.getPhone() == null || request.getPhone().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vui lòng nhập số điện thoại");
		}
	}

	private String normalizeRole(String role) {
		if (role == null || role.isBlank()) {
			return "CUSTOMER";
		}
		return role.trim().toUpperCase();
	}
}
