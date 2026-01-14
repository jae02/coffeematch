package com.coffeematch.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
public class PlatformData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cafe_id")
    @JsonIgnore
    private Cafe cafe;

    @Column(name = "platform_type")
    private String platformType; // NAVER, KAKAO

    private Double rating;
    private Integer reviewCount;
    private String link;

    public PlatformData() {
    }

    public PlatformData(Cafe cafe, String platformType, Double rating, Integer reviewCount, String link) {
        this.cafe = cafe;
        this.platformType = platformType;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.link = link;
    }

    public Long getId() {
        return id;
    }

    public Cafe getCafe() {
        return cafe;
    }

    public String getPlatformType() {
        return platformType;
    }

    public Double getRating() {
        return rating;
    }

    public Integer getReviewCount() {
        return reviewCount;
    }

    public String getLink() {
        return link;
    }

    public void setCafe(Cafe cafe) {
        this.cafe = cafe;
    }
}
