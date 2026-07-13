package com.example.productservice.storage;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class LocalFileStorageService {

	private final Path uploadRoot = Paths.get("uploads", "products").toAbsolutePath().normalize();
	private final Path bannerUploadRoot = Paths.get("uploads", "banners").toAbsolutePath().normalize();

	public String saveImage(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			return null;
		}

		try {
			Files.createDirectories(uploadRoot);
			String originalName = file.getOriginalFilename() == null ? "image" : file.getOriginalFilename();
			String extension = getExtension(originalName);
			String fileName = UUID.randomUUID() + extension;
			Path destination = uploadRoot.resolve(fileName).normalize();
			Files.copy(file.getInputStream(), destination);
			return "/uploads/products/" + fileName;
		} catch (IOException ex) {
			throw new IllegalStateException("Lưu hình ảnh thất bại", ex);
		}
	}

	public String saveImageForBanner(MultipartFile file) {
		if (file == null || file.isEmpty()) return null;
		try {
			Files.createDirectories(bannerUploadRoot);
			String originalName = file.getOriginalFilename() == null ? "image" : file.getOriginalFilename();
			String extension = getExtension(originalName);
			String fileName = UUID.randomUUID() + extension;
			Path destination = bannerUploadRoot.resolve(fileName).normalize();
			Files.copy(file.getInputStream(), destination);
			return "/uploads/banners/" + fileName;
		} catch (IOException ex) {
			throw new IllegalStateException("Lưu banner thất bại", ex);
		}
	}

	private String getExtension(String fileName) {
		int dotIndex = fileName.lastIndexOf('.');
		if (dotIndex < 0) {
			return "";
		}
		return fileName.substring(dotIndex);
	}
}
