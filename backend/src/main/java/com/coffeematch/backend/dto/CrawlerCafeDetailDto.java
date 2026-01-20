package com.coffeematch.backend.dto;

import java.util.ArrayList;
import java.util.List;

public class CrawlerCafeDetailDto {
    private String name;
    private String address;
    private String category;
    private String businessHours;
    private String url;

    private List<ReviewDetailDto> reviews = new ArrayList<>();

    public CrawlerCafeDetailDto() {
    }

    public CrawlerCafeDetailDto(String name, String address, String category, String businessHours, String url,
            List<ReviewDetailDto> reviews) {
        this.name = name;
        this.address = address;
        this.category = category;
        this.businessHours = businessHours;
        this.url = url;
        this.reviews = reviews;
    }

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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getBusinessHours() {
        return businessHours;
    }

    public void setBusinessHours(String businessHours) {
        this.businessHours = businessHours;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<ReviewDetailDto> getReviews() {
        return reviews;
    }

    public void setReviews(List<ReviewDetailDto> reviews) {
        this.reviews = reviews;
    }
}
