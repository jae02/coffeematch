package com.coffeematch.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
public class PlatformData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cafe_id")
    @JsonIgnore
    private Cafe cafe;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", length = 20, nullable = false)
    private Platform platform;

    // Legacy fields (keeping for backward compatibility)
    private Double rating;
    private Integer reviewCount;
    private String link;

    // New fields for raw data storage
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSON")
    private Map<String, Object> rawData;

    private LocalDateTime lastCheckedAt;

    public PlatformData() {
    }

    public PlatformData(Cafe cafe, Platform platform, Map<String, Object> rawData) {
        this.cafe = cafe;
        this.platform = platform;
        this.rawData = rawData;
        this.lastCheckedAt = LocalDateTime.now();
    }

    // Legacy constructor
    public PlatformData(Cafe cafe, String platformType, Double rating, Integer reviewCount, String link) {
        this.cafe = cafe;
        this.platform = Platform.valueOf(platformType.toUpperCase() + "_MAP");
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.link = link;
        this.lastCheckedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Cafe getCafe() {
        return cafe;
    }

    public void setCafe(Cafe cafe) {
        this.cafe = cafe;
    }

    public Platform getPlatform() {
        return platform;
    }

    public void setPlatform(Platform platform) {
        this.platform = platform;
    }

    public String getPlatformType() {
        return platform != null ? platform.name() : null;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Integer getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(Integer reviewCount) {
        this.reviewCount = reviewCount;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Map<String, Object> getRawData() {
        return rawData;
    }

    public void setRawData(Map<String, Object> rawData) {
        this.rawData = rawData;
    }

    public LocalDateTime getLastCheckedAt() {
        return lastCheckedAt;
    }

    public void setLastCheckedAt(LocalDateTime lastCheckedAt) {
        this.lastCheckedAt = lastCheckedAt;
    }
}
