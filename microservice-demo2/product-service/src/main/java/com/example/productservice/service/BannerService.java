package com.example.productservice.service;

import com.example.productservice.dto.BannerDTO;
import com.example.productservice.dto.BannerResponse;
import com.example.productservice.dto.ReorderRequest;
import com.example.productservice.entity.Banner;
import com.example.productservice.repository.BannerRepository;
import com.example.productservice.storage.LocalFileStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class BannerService {

	private final BannerRepository bannerRepository;
	private final LocalFileStorageService fileStorageService;

	public BannerService(BannerRepository bannerRepository, LocalFileStorageService fileStorageService) {
		this.bannerRepository = bannerRepository;
		this.fileStorageService = fileStorageService;
	}

	public List<BannerResponse> getActiveBanners() {
		return bannerRepository.findByActiveTrueOrderBySortOrderAsc().stream()
				.map(this::toResponse).toList();
	}

	public List<BannerResponse> getAllBanners() {
		return bannerRepository.findAllByOrderBySortOrderAsc().stream()
				.map(this::toResponse).toList();
	}

	public BannerResponse createBanner(BannerDTO dto, MultipartFile file) {
		Banner banner = new Banner();
		applyDto(banner, dto);
		if (file != null && !file.isEmpty()) {
			String img = fileStorageService.saveImageForBanner(file);
			banner.setImage(img);
		}
		if (banner.getSortOrder() == null) {
			long count = bannerRepository.count();
			banner.setSortOrder((int) count + 1);
		}
		return toResponse(bannerRepository.save(banner));
	}

	public BannerResponse updateBanner(String id, BannerDTO dto, MultipartFile file) {
		Banner banner = bannerRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy banner"));
		applyDto(banner, dto);
		if (file != null && !file.isEmpty()) {
			String img = fileStorageService.saveImageForBanner(file);
			banner.setImage(img);
		}
		return toResponse(bannerRepository.save(banner));
	}

	public void deleteBanner(String id) {
		if (!bannerRepository.existsById(id)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy banner");
		}
		bannerRepository.deleteById(id);
	}

	@Transactional
	public void reorder(ReorderRequest request) {
		if (request.getItems() == null) return;
		for (ReorderRequest.Item item : request.getItems()) {
			bannerRepository.findById(item.getId()).ifPresent(b -> {
				b.setSortOrder(item.getSortOrder());
				bannerRepository.save(b);
			});
		}
	}

	private void applyDto(Banner banner, BannerDTO dto) {
		banner.setTitle(dto.getTitle());
		banner.setSubtitle(dto.getSubtitle());
		banner.setLink(dto.getLink());
		banner.setSortOrder(dto.getSortOrder());
		banner.setActive(dto.isActive());
	}

	private BannerResponse toResponse(Banner banner) {
		return new BannerResponse(
				banner.getId(), banner.getTitle(), banner.getSubtitle(),
				banner.getImage(), banner.getLink(),
				banner.getSortOrder(), banner.isActive()
		);
	}
}
