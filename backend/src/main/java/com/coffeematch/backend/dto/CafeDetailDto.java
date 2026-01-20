package com.coffeematch.backend.dto;

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

    public CafeDetailDto() {
    }

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

    public void setCafe(CafeDto cafe) {
        this.cafe = cafe;
    }

    public List<PlatformData> getPlatformData() {
        return platformData;
    }

    public void setPlatformData(List<PlatformData> platformData) {
        this.platformData = platformData;
    }

    public List<CafeKeywordStat> getKeywordStats() {
        return keywordStats;
    }

    public void setKeywordStats(List<CafeKeywordStat> keywordStats) {
        this.keywordStats = keywordStats;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    public boolean isBookmarked() {
        return isBookmarked;
    }

    public void setBookmarked(boolean bookmarked) {
        isBookmarked = bookmarked;
    }
}
