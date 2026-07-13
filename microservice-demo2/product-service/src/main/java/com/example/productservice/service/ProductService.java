package com.example.productservice.service;

import com.example.productservice.client.CategoryClient;
import com.example.productservice.client.InventoryClient;
import com.example.productservice.dto.CategoryInfo;
import com.example.productservice.dto.InventoryResponse;
import com.example.productservice.dto.ProductRequest;
import com.example.productservice.dto.ProductResponse;
import com.example.productservice.entity.Product;
import com.example.productservice.repository.ProductRepository;
import com.example.productservice.storage.LocalFileStorageService;
import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ProductService {

	private final ProductRepository productRepository;
	private final CategoryClient categoryClient;
	private final InventoryClient inventoryClient;
	private final LocalFileStorageService localFileStorageService;
	private final SequenceGeneratorService sequenceGenerator;

	public ProductService(ProductRepository productRepository, CategoryClient categoryClient, InventoryClient inventoryClient, LocalFileStorageService localFileStorageService, SequenceGeneratorService sequenceGenerator) {
		this.productRepository = productRepository;
		this.categoryClient = categoryClient;
		this.inventoryClient = inventoryClient;
		this.localFileStorageService = localFileStorageService;
		this.sequenceGenerator = sequenceGenerator;
	}

	public List<ProductResponse> getAllProducts() {
		return productRepository.findAllByDeletedFalse().stream()
				.map(this::toResponse)
				.toList();
	}

	public List<ProductResponse> getDeletedProducts() {
		return productRepository.findAllByDeletedTrue().stream()
				.map(this::toResponse)
				.toList();
	}

	public ProductResponse getProductById(Long id) {
		return productRepository.findById(id)
				.map(this::toResponse)
				.orElse(null);
	}

	public List<ProductResponse> getProductsByCategory(Long categoryId) {
		return productRepository.findByCategoryIdAndDeletedFalse(categoryId).stream()
				.map(this::toResponse)
				.toList();
	}

	public ProductResponse createProduct(ProductRequest request) {
		validateRequest(request);
		CategoryInfo category = getCategoryOrThrow(request.getCategoryId());
		Product product = new Product();
		product.setId(sequenceGenerator.generateSequence("product_seq"));
		product.setName(request.getName());
		product.setPrice(request.getPrice());
		product.setImg(resolveImagePath(request, null));
		product.setCategoryId(category.getId());
		product.setDescription(request.getDescription());
		product.setRam(request.getRam());
		product.setStorage(request.getStorage());
		product.setScreenResolution(request.getScreenResolution());
		product.setScreenTechnology(request.getScreenTechnology());
		product.setBattery(request.getBattery());
		product.setColor(request.getColor());
		product.setSalePrice(request.getSalePrice() != null ? request.getSalePrice() : 0d);
		return toResponse(productRepository.save(product));
	}

	public ProductResponse updateProduct(Long id, ProductRequest request) {
		validateRequest(request);
		Product product = productRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm với mã " + id));
		getCategoryOrThrow(request.getCategoryId());
		product.setName(request.getName());
		product.setPrice(request.getPrice());
		product.setImg(resolveImagePath(request, product.getImg()));
		product.setCategoryId(request.getCategoryId());
		product.setDescription(request.getDescription());
		product.setRam(request.getRam());
		product.setStorage(request.getStorage());
		product.setScreenResolution(request.getScreenResolution());
		product.setScreenTechnology(request.getScreenTechnology());
		product.setBattery(request.getBattery());
		product.setColor(request.getColor());
		product.setSalePrice(request.getSalePrice() != null ? request.getSalePrice() : 0d);
		return toResponse(productRepository.save(product));
	}

	public void deleteProduct(Long id) {
		Product product = productRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm với mã " + id));
		boolean hasInventory = false;
		try {
			InventoryResponse inv = inventoryClient.getStock(id);
			if (inv != null && inv.getQuantity() > 0) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
						"Không thể xóa sản phẩm ID " + id + " vì còn " + inv.getQuantity() + " sản phẩm trong kho");
			}
			hasInventory = inv != null;
		} catch (FeignException.NotFound e) {
			// no inventory record => safe to delete
		}
		product.setDeleted(true);
		productRepository.save(product);
		if (hasInventory) {
			inventoryClient.deleteStock(id);
		}
	}

	public ProductResponse restoreProduct(Long id) {
		Product product = productRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm với mã " + id));
		product.setDeleted(false);
		return toResponse(productRepository.save(product));
	}

	public void hardDeleteProduct(Long id) {
		if (!productRepository.existsById(id)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm với mã " + id);
		}
		productRepository.deleteById(id);
	}

	private ProductResponse toResponse(Product product) {
		CategoryInfo category = null;
		if (product.getCategoryId() != null) {
			try {
				category = categoryClient.getCategoryById(product.getCategoryId());
			} catch (FeignException.NotFound ex) {
				// category may have been deleted; just leave it null
			} catch (FeignException ex) {
				// ignore connectivity issues for deleted products
			}
		}
		ProductResponse resp = new ProductResponse(
				product.getId(),
				product.getName(),
				product.getPrice(),
				product.getImg(),
				product.getCategoryId(),
				category != null ? category.getName() : null,
				product.isDeleted()
		);
		resp.setDescription(product.getDescription());
		resp.setRam(product.getRam());
		resp.setStorage(product.getStorage());
		resp.setScreenResolution(product.getScreenResolution());
		resp.setScreenTechnology(product.getScreenTechnology());
		resp.setBattery(product.getBattery());
		resp.setColor(product.getColor());
		resp.setSalePrice(product.getSalePrice());
		try {
			InventoryResponse inv = inventoryClient.getStock(product.getId());
			if (inv != null) {
				resp.setQuantity(inv.getQuantity());
				resp.setReservedQuantity(inv.getReservedQuantity());
				resp.setAvailableQuantity(inv.getAvailableQuantity());
			}
		} catch (FeignException e) {
			// no inventory record or connectivity issue; leave fields null
		}
		return resp;
	}

	private void validateRequest(ProductRequest request) {
		if (request == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Yêu cầu sản phẩm không được để trống");
		}
		if (request.getName() == null || request.getName().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vui lòng nhập tên sản phẩm");
		}
		if (request.getPrice() == null || request.getPrice() < 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Giá sản phẩm phải lớn hơn hoặc bằng 0");
		}
		// img is optional; allow empty value so existing requests still work.
		if (request.getCategoryId() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vui lòng cung cấp categoryId");
		}
	}

	private String resolveImagePath(ProductRequest request, String currentPath) {
		if (request.getImgFile() != null && !request.getImgFile().isEmpty()) {
			return localFileStorageService.saveImage(request.getImgFile());
		}
		if (request.getImg() != null && !request.getImg().isBlank()) {
			return request.getImg();
		}
		return currentPath;
	}

	private CategoryInfo getCategoryOrThrow(Long categoryId) {
		try {
			CategoryInfo category = categoryClient.getCategoryById(categoryId);
			if (category == null) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy danh mục với mã " + categoryId);
			}
			return category;
		} catch (FeignException.NotFound ex) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy danh mục với mã " + categoryId, ex);
		} catch (FeignException ex) {
			throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Không thể kết nối dịch vụ danh mục", ex);
		}
	}
}
