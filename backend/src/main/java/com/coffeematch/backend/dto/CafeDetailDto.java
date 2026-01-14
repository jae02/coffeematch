package com.coffeematch.backend.dto;

import com.coffeematch.backend.dto.CafeDto;
import com.coffeematch.backend.entity.CafeKeywordStat;
import com.coffeematch.backend.entity.PlatformData;
import com.coffeematch.backend.entity.Review;

import java.util.List;

public class CafeDetailDto {
    private CafeDto cafe;
    private List<PlatformData> platformData;
    private List<CafeKeywordStat> keywordStats;
    private List<Review> reviews;
    private boolean isBookmarked;

    public CafeDetailDto(CafeDto cafe, List<PlatformData> platformData, List<CafeKeywordStat> keywordStats,
            List<Review> reviews, boolean isBookmarked) {
        this.cafe = cafe;
        this.platformData = platformData;
        this.keywordStats = keywordStats;
        this.reviews = reviews;
        this.isBookmarked = isBookmarked;
    }

    public CafeDto getCafe() {
        return cafe;
    }

    public List<PlatformData> getPlatformData() {
        return platformData;
    }

    public List<CafeKeywordStat> getKeywordStats() {
        return keywordStats;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public boolean isBookmarked() {
        return isBookmarked;
    }
}
