package com.coffeematch.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cafe_id")
    @JsonIgnore
    private Cafe cafe;

    private String author;
    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String imageUrl;
    private String imageCategory; // GENERAL, STORE, MENU

    private LocalDateTime createdAt = LocalDateTime.now();

    // Platform tracking columns
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Platform sourcePlatform;

    @Column(length = 100)
    private String platformReviewId;

    private LocalDateTime crawledAt;

    public Review() {
    }

    public Review(Cafe cafe, String author, Integer rating, String content) {
        this.cafe = cafe;
        this.author = author;
        this.rating = rating;
        this.content = content;
        this.createdAt = LocalDateTime.now();
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

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageCategory() {
        return imageCategory;
    }

    public void setImageCategory(String imageCategory) {
        this.imageCategory = imageCategory;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Platform getSourcePlatform() {
        return sourcePlatform;
    }

    public void setSourcePlatform(Platform sourcePlatform) {
        this.sourcePlatform = sourcePlatform;
    }

    public String getPlatformReviewId() {
        return platformReviewId;
    }

    public void setPlatformReviewId(String platformReviewId) {
        this.platformReviewId = platformReviewId;
    }

    public LocalDateTime getCrawledAt() {
        return crawledAt;
    }

    public void setCrawledAt(LocalDateTime crawledAt) {
        this.crawledAt = crawledAt;
    }
}
