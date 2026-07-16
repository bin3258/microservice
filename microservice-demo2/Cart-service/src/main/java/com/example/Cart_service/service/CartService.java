package com.example.Cart_service.service;

import com.example.Cart_service.client.InventoryClient;
import com.example.Cart_service.dto.CartRequest;
import com.example.Cart_service.dto.CartResponse;
import com.example.Cart_service.entity.CartItem;
import com.example.Cart_service.repository.CartRepository;
import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class CartService {
    private final CartRepository cartRepository;
    private final InventoryClient inventoryClient;

    public CartService(CartRepository cartRepository, InventoryClient inventoryClient) {
        this.cartRepository = cartRepository;
        this.inventoryClient = inventoryClient;
    }

    public List<CartResponse> getCart(Long userId) {
        return cartRepository.findByUserId(userId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public CartResponse addItem(Long userId, CartRequest request) {
        List<CartItem> existing = cartRepository.findByUserIdAndProductId(userId, request.getProductId());
        if (!existing.isEmpty()) {
            CartItem item = existing.get(0);
            if (existing.size() > 1) {
                cartRepository.deleteAll(existing.subList(1, existing.size()));
            }
            int newQty = item.getQuantity() + request.getQuantity();
            checkStock(request.getProductId(), newQty);
            item.setQuantity(newQty);
            return toResponse(cartRepository.save(item));
        }

        checkStock(request.getProductId(), request.getQuantity());

        CartItem item = new CartItem();
        item.setUserId(userId);
        item.setProductId(request.getProductId());
        item.setProductName(request.getProductName());
        item.setProductImg(request.getProductImg());
        item.setQuantity(request.getQuantity());
        item.setUnitPrice(request.getUnitPrice());
        return toResponse(cartRepository.save(item));
    }

    @Transactional
    public CartResponse updateQuantity(Long itemId, Integer quantity) {
        CartItem item = cartRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm trong giỏ hàng"));
        checkStock(item.getProductId(), quantity);
        item.setQuantity(quantity);
        return toResponse(cartRepository.save(item));
    }

    @Transactional
    public void removeItem(Long itemId) {
        CartItem item = cartRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm trong giỏ hàng"));
        cartRepository.delete(item);
    }

    @Transactional
    public CartResponse updateQuantityByProduct(Long userId, Long productId, Integer quantity) {
        List<CartItem> items = cartRepository.findByUserIdAndProductId(userId, productId);
        if (items.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm trong giỏ hàng");
        }
        CartItem item = items.get(0);
        checkStock(productId, quantity);
        item.setQuantity(quantity);
        if (items.size() > 1) {
            cartRepository.deleteAll(items.subList(1, items.size()));
        }
        return toResponse(cartRepository.save(item));
    }

    @Transactional
    public void removeItemByProduct(Long userId, Long productId) {
        List<CartItem> items = cartRepository.findByUserIdAndProductId(userId, productId);
        if (items.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm trong giỏ hàng");
        }
        cartRepository.deleteAll(items);
    }

    @Transactional
    public void clearCart(Long userId) {
        cartRepository.deleteByUserId(userId);
    }

    private void checkStock(Long productId, Integer quantity) {
        try {
            var stock = inventoryClient.getStock(productId);
            if (stock.getAvailableQuantity() < quantity) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Rất tiếc, sản phẩm này trong kho không đủ số lượng bạn yêu cầu. Hiện chỉ còn " + stock.getAvailableQuantity() + " sản phẩm.");
            }
        } catch (FeignException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rất tiếc, sản phẩm này hiện không có trong kho.");
        } catch (FeignException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Rất tiếc, hiện tại không thể kiểm tra tồn kho. Vui lòng thử lại sau.");
        }
    }

    private CartResponse toResponse(CartItem item) {
        return new CartResponse(
                item.getId(), item.getProductId(), item.getProductName(), item.getProductImg(),
                item.getQuantity(), item.getUnitPrice(),
                item.getUnitPrice() * item.getQuantity(), item.getUserId());
    }
}
