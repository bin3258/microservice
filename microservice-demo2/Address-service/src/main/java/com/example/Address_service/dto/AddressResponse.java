package com.example.Address_service.dto;

public class AddressResponse {
    private Long id;
    private Long customerId;
    private String street;
    private String ward;
    private String city;
    private boolean isDefault;
    private Double latitude;
    private Double longitude;

    public AddressResponse() {}

    public AddressResponse(Long id, Long customerId, String street, String ward, String city, boolean isDefault, Double latitude, Double longitude) {
        this.id = id;
        this.customerId = customerId;
        this.street = street;
        this.ward = ward;
        this.city = city;
        this.isDefault = isDefault;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }
    public String getWard() { return ward; }
    public void setWard(String ward) { this.ward = ward; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean isDefault) { this.isDefault = isDefault; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}
