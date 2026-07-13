package com.example.Address_service.repository;

import com.example.Address_service.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByCustomerId(Long customerId);
    Optional<Address> findByCustomerIdAndIsDefaultTrue(Long customerId);
}
