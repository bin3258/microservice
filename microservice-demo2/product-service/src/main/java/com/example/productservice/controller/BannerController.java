package com.example.productservice.controller;

import com.example.productservice.dto.BannerDTO;
import com.example.productservice.dto.BannerResponse;
import com.example.productservice.dto.ReorderRequest;
import com.example.productservice.service.BannerService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/banners")
public class BannerController {

	private final BannerService bannerService;

	public BannerController(BannerService bannerService) {
		this.bannerService = bannerService;
	}

	private void requireAdmin(HttpServletRequest request) {
		String role = (String) request.getAttribute("X-User-Role");
		if (!"ADMIN".equals(role) && !"MANAGER".equals(role)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Chi ADMIN/MANAGER moi co quyen thuc hien thao tac nay");
		}
	}

	@GetMapping
	public List<BannerResponse> getActiveBanners() {
		return bannerService.getActiveBanners();
	}

	@GetMapping("/admin")
	public List<BannerResponse> getAllBanners(HttpServletRequest request) {
		requireAdmin(request);
		return bannerService.getAllBanners();
	}

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<BannerResponse> createBanner(
			HttpServletRequest servletRequest,
			@ModelAttribute BannerDTO dto,
			@RequestParam(value = "imageFile", required = false) MultipartFile file) {
		requireAdmin(servletRequest);
		return ResponseEntity.status(HttpStatus.CREATED).body(bannerService.createBanner(dto, file));
	}

	@PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<BannerResponse> updateBanner(
			HttpServletRequest servletRequest,
			@PathVariable String id,
			@ModelAttribute BannerDTO dto,
			@RequestParam(value = "imageFile", required = false) MultipartFile file) {
		requireAdmin(servletRequest);
		return ResponseEntity.ok(bannerService.updateBanner(id, dto, file));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteBanner(HttpServletRequest servletRequest, @PathVariable String id) {
		requireAdmin(servletRequest);
		bannerService.deleteBanner(id);
		return ResponseEntity.noContent().build();
	}

	@PutMapping("/reorder")
	public ResponseEntity<Void> reorderBanners(HttpServletRequest servletRequest, @RequestBody ReorderRequest request) {
		requireAdmin(servletRequest);
		bannerService.reorder(request);
		return ResponseEntity.ok().build();
	}
}
