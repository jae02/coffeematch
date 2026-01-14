package com.coffeematch.backend.dto;

public class AdminStatsDto {
    private long totalCafes;
    private long totalReviews;

    public AdminStatsDto(long totalCafes, long totalReviews) {
        this.totalCafes = totalCafes;
        this.totalReviews = totalReviews;
    }

    public long getTotalCafes() {
        return totalCafes;
    }

    public long getTotalReviews() {
        return totalReviews;
    }
}
