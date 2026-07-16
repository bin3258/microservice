package com.example.discount_service.service;

import com.example.discount_service.dto.*;
import com.example.discount_service.entity.Discount;
import com.example.discount_service.entity.DiscountUsage;
import com.example.discount_service.entity.DiscountUser;
import com.example.discount_service.repository.DiscountRepository;
import com.example.discount_service.repository.DiscountUsageRepository;
import com.example.discount_service.repository.DiscountUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DiscountService {

    private static final Logger log = LoggerFactory.getLogger(DiscountService.class);
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final DiscountRepository discountRepository;
    private final DiscountUserRepository discountUserRepository;
    private final DiscountUsageRepository discountUsageRepository;

    public DiscountService(DiscountRepository discountRepository,
                           DiscountUserRepository discountUserRepository,
                           DiscountUsageRepository discountUsageRepository) {
        this.discountRepository = discountRepository;
        this.discountUserRepository = discountUserRepository;
        this.discountUsageRepository = discountUsageRepository;
    }

    public List<Discount> getAll() {
        return discountRepository.findAll();
    }

    public Discount getById(Long id) {
        return discountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mã giảm giá"));
    }

    @Transactional
    public Discount create(DiscountRequest req) {
        if (discountRepository.existsByCode(req.getCode())) {
            throw new RuntimeException("Mã giảm giá '" + req.getCode() + "' đã tồn tại");
        }
        Discount d = new Discount();
        d.setCode(req.getCode().toUpperCase());
        d.setType(req.getType());
        d.setDiscountValue(req.getDiscountValue());
        d.setMinOrderValue(req.getMinOrderValue() != null ? req.getMinOrderValue() : 0.0);
        d.setUsageLimit(req.getUsageLimit());
        d.setIsActive(req.getIsActive() != null ? req.getIsActive() : true);
        d.setDescription(req.getDescription());
        if (req.getStartDate() != null && !req.getStartDate().isEmpty()) {
            d.setStartDate(LocalDateTime.parse(req.getStartDate(), DTF));
        }
        if (req.getEndDate() != null && !req.getEndDate().isEmpty()) {
            d.setEndDate(LocalDateTime.parse(req.getEndDate(), DTF));
        }
        return discountRepository.save(d);
    }

    @Transactional
    public Discount update(Long id, DiscountRequest req) {
        Discount d = getById(id);
        if (!d.getCode().equals(req.getCode()) && discountRepository.existsByCode(req.getCode())) {
            throw new RuntimeException("Mã giảm giá '" + req.getCode() + "' đã tồn tại");
        }
        d.setCode(req.getCode().toUpperCase());
        d.setType(req.getType());
        d.setDiscountValue(req.getDiscountValue());
        d.setMinOrderValue(req.getMinOrderValue() != null ? req.getMinOrderValue() : 0.0);
        d.setUsageLimit(req.getUsageLimit());
        d.setIsActive(req.getIsActive() != null ? req.getIsActive() : true);
        d.setDescription(req.getDescription());
        if (req.getStartDate() != null && !req.getStartDate().isEmpty()) {
            d.setStartDate(LocalDateTime.parse(req.getStartDate(), DTF));
        } else {
            d.setStartDate(null);
        }
        if (req.getEndDate() != null && !req.getEndDate().isEmpty()) {
            d.setEndDate(LocalDateTime.parse(req.getEndDate(), DTF));
        } else {
            d.setEndDate(null);
        }
        return discountRepository.save(d);
    }

    @Transactional
    public void delete(Long id) {
        discountRepository.deleteById(id);
    }

    public List<DiscountUser> getAssignedUsers(Long discountId) {
        return discountUserRepository.findByDiscountId(discountId);
    }

    @Transactional
    public void assignUsers(Long discountId, List<Long> userIds) {
        getById(discountId);
        discountUserRepository.deleteByDiscountId(discountId);
        for (Long userId : userIds) {
            discountUserRepository.save(new DiscountUser(discountId, userId));
        }
    }

    @Transactional
    public void removeAllUsers(Long discountId) {
        discountUserRepository.deleteByDiscountId(discountId);
    }

    public List<Discount> getAvailableDiscounts(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        return discountRepository.findAllByIsActiveTrue().stream()
                .filter(d -> d.getStartDate() == null || !now.isBefore(d.getStartDate()))
                .filter(d -> d.getEndDate() == null || !now.isAfter(d.getEndDate()))
                .filter(d -> d.getUsageLimit() == null || d.getUsedCount() < d.getUsageLimit())
                .filter(d -> {
                    List<DiscountUser> assigned = discountUserRepository.findByDiscountId(d.getId());
                    if (assigned.isEmpty()) return true;
                    return assigned.stream().anyMatch(u -> u.getUserId().equals(userId));
                })
                .collect(Collectors.toList());
    }

    public ValidateResponse validate(ValidateRequest req) {
        Optional<Discount> opt = discountRepository.findByCode(req.getCode().toUpperCase());
        if (opt.isEmpty()) {
            return ValidateResponse.error("Mã giảm giá không tồn tại");
        }
        Discount d = opt.get();

        if (!d.getIsActive()) {
            return ValidateResponse.error("Mã giảm giá đã bị vô hiệu hóa");
        }

        LocalDateTime now = LocalDateTime.now();
        if (d.getStartDate() != null && now.isBefore(d.getStartDate())) {
            return ValidateResponse.error("Mã giảm giá chưa đến hạn sử dụng");
        }
        if (d.getEndDate() != null && now.isAfter(d.getEndDate())) {
            return ValidateResponse.error("Mã giảm giá đã hết hạn");
        }

        if (d.getUsageLimit() != null && d.getUsedCount() >= d.getUsageLimit()) {
            return ValidateResponse.error("Mã giảm giá đã hết lượt sử dụng");
        }

        List<DiscountUser> assignedUsers = discountUserRepository.findByDiscountId(d.getId());
        if (!assignedUsers.isEmpty()) {
            boolean userAllowed = assignedUsers.stream().anyMatch(u -> u.getUserId().equals(req.getUserId()));
            if (!userAllowed) {
                return ValidateResponse.error("Bạn không có quyền sử dụng mã giảm giá này");
            }
        }

        Double total = req.getOrderTotal() != null ? req.getOrderTotal() : 0.0;
        if ("SHIPPING".equals(d.getType())) {
            total = req.getShippingFee() != null ? req.getShippingFee() : 0.0;
        }
        if (total < d.getMinOrderValue()) {
            return ValidateResponse.error("Đơn hàng chưa đạt giá trị tối thiểu " + String.format("%,.0f", d.getMinOrderValue()) + "đ");
        }

        double discountAmount = Math.min(d.getDiscountValue(), total);
        return ValidateResponse.success(d.getType(), discountAmount, d.getId());
    }

    @Transactional
    public void release(MarkUsedRequest req) {
        Discount d = discountRepository.findByCode(req.getCode().toUpperCase())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mã giảm giá"));
        if (d.getUsedCount() != null && d.getUsedCount() > 0) {
            d.setUsedCount(d.getUsedCount() - 1);
            discountRepository.save(d);
        }
        discountUsageRepository.findByDiscountIdAndUserIdAndOrderId(d.getId(), req.getUserId(), req.getOrderId())
                .ifPresent(usage -> discountUsageRepository.delete(usage));
        log.info("Discount {} released for user {} order {}", req.getCode(), req.getUserId(), req.getOrderId());
    }

    @Transactional
    public void markUsed(MarkUsedRequest req) {
        Discount d = discountRepository.findByCode(req.getCode().toUpperCase())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mã giảm giá"));
        d.setUsedCount(d.getUsedCount() != null ? d.getUsedCount() + 1 : 1);
        discountRepository.save(d);
        discountUsageRepository.save(new DiscountUsage(d.getId(), req.getUserId(), req.getOrderId(), req.getDiscountAmount()));
        log.info("Discount {} used by user {} for order {}, amount {}", req.getCode(), req.getUserId(), req.getOrderId(), req.getDiscountAmount());
    }
}
