package com.coffeematch.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class ReviewDetailDto {
    private String nickname;
    private String rating;
    private String content;
    private String date;
    private String imageUrl;

    public ReviewDetailDto() {
    }

    public ReviewDetailDto(String nickname, String rating, String content, String date, String imageUrl) {
        this.nickname = nickname;
        this.rating = rating;
        this.content = content;
        this.date = date;
        this.imageUrl = imageUrl;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
