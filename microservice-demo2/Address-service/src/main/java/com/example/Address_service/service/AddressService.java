package com.example.Address_service.service;

import com.example.Address_service.dto.AddressRequest;
import com.example.Address_service.dto.AddressResponse;
import com.example.Address_service.entity.Address;
import com.example.Address_service.repository.AddressRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AddressService {

    private final AddressRepository addressRepository;

    public AddressService(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    public List<AddressResponse> getAddressesByCustomerId(Long customerId) {
        return addressRepository.findByCustomerId(customerId).stream()
                .map(this::toResponse)
                .toList();
    }

    public AddressResponse getAddressById(Long id) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy địa chỉ"));
        return toResponse(address);
    }

    @Transactional
    public AddressResponse createAddress(AddressRequest request) {
        if (request.isDefault()) {
            addressRepository.findByCustomerIdAndIsDefaultTrue(request.getCustomerId())
                    .ifPresent(a -> a.setDefault(false));
        }
        Address address = new Address(
                request.getCustomerId(),
                request.getStreet(),
                request.getWard(),
                request.getCity(),
                request.isDefault()
        );
        address.setLatitude(request.getLatitude());
        address.setLongitude(request.getLongitude());
        address = addressRepository.save(address);
        return toResponse(address);
    }

    @Transactional
    public AddressResponse updateAddress(Long id, AddressRequest request) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy địa chỉ"));
        if (request.isDefault() && !address.isDefault()) {
            addressRepository.findByCustomerIdAndIsDefaultTrue(request.getCustomerId())
                    .ifPresent(a -> a.setDefault(false));
        }
        address.setStreet(request.getStreet());
        address.setWard(request.getWard());
        address.setCity(request.getCity());
        address.setDefault(request.isDefault());
        if (request.getLatitude() != null) address.setLatitude(request.getLatitude());
        if (request.getLongitude() != null) address.setLongitude(request.getLongitude());
        address = addressRepository.save(address);
        return toResponse(address);
    }

    @Transactional
    public void setDefaultAddress(Long id, Long customerId) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy địa chỉ"));
        if (!address.getCustomerId().equals(customerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Địa chỉ không thuộc về khách hàng này");
        }
        addressRepository.findByCustomerIdAndIsDefaultTrue(customerId)
                .ifPresent(a -> a.setDefault(false));
        address.setDefault(true);
        addressRepository.save(address);
    }

    public void deleteAddress(Long id) {
        if (!addressRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy địa chỉ");
        }
        addressRepository.deleteById(id);
    }

    private AddressResponse toResponse(Address address) {
        return new AddressResponse(
                address.getId(),
                address.getCustomerId(),
                address.getStreet(),
                address.getWard(),
                address.getCity(),
                address.isDefault(),
                address.getLatitude(),
                address.getLongitude()
        );
    }
}
