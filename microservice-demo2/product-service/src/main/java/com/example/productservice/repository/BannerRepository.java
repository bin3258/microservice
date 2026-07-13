package com.example.productservice.repository;

import com.example.productservice.entity.Banner;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface BannerRepository extends MongoRepository<Banner, String> {
	List<Banner> findByActiveTrueOrderBySortOrderAsc();
	List<Banner> findAllByOrderBySortOrderAsc();
}
