package com.coffeematch.backend.dto;

import java.util.Map;

public class CrawlCafeRequestDto {
    private String name;
    private String address;
    private String phone;
    private String businessHours;
    private String category;
    private String sourcePlatform; // KAKAO_MAP, NAVER_MAP, NAVER_BLOG
    private String platformId;
    private Double latitude;
    private Double longitude;
    private String status; // NEW, ACTIVE, CLOSED_SUSPECTED, CLOSED_CONFIRMED
    private Map<String, Object> rawData;

    public CrawlCafeRequestDto() {
    }

    public CrawlCafeRequestDto(String name, String address, String phone, String businessHours,
            String category, String sourcePlatform, String platformId,
            Double latitude, Double longitude, String status,
            Map<String, Object> rawData) {
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.businessHours = businessHours;
        this.category = category;
        this.sourcePlatform = sourcePlatform;
        this.platformId = platformId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = status;
        this.rawData = rawData;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getBusinessHours() {
        return businessHours;
    }

    public void setBusinessHours(String businessHours) {
        this.businessHours = businessHours;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSourcePlatform() {
        return sourcePlatform;
    }

    public void setSourcePlatform(String sourcePlatform) {
        this.sourcePlatform = sourcePlatform;
    }

    public String getPlatformId() {
        return platformId;
    }

    public void setPlatformId(String platformId) {
        this.platformId = platformId;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Map<String, Object> getRawData() {
        return rawData;
    }

    public void setRawData(Map<String, Object> rawData) {
        this.rawData = rawData;
    }
}
