package com.coffeematch.backend.dto;

public class CrawlDataDto {
    private String source;
    private String name;
    private String address;
    private String phone;
    private String rating;
    private String reviewCount;
    private String bizHour;
    private String url;
    private String title;
    private String imageUrl;
    private String content;

    public CrawlDataDto() {
    }

    public CrawlDataDto(String source, String name, String address, String phone, String rating, String reviewCount,
            String bizHour, String url, String title, String imageUrl, String content) {
        this.source = source;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.bizHour = bizHour;
        this.url = url;
        this.title = title;
        this.imageUrl = imageUrl;
        this.content = content;
    }

    public static CrawlDataDtoBuilder builder() {
        return new CrawlDataDtoBuilder();
    }

    public String getSource() {
        return source;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }

    public String getRating() {
        return rating;
    }

    public String getReviewCount() {
        return reviewCount;
    }

    public String getBizHour() {
        return bizHour;
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getContent() {
        return content;
    }

    public static class CrawlDataDtoBuilder {
        private String source;
        private String name;
        private String address;
        private String phone;
        private String rating;
        private String reviewCount;
        private String bizHour;
        private String url;
        private String title;
        private String imageUrl;
        private String content;

        public CrawlDataDtoBuilder source(String source) {
            this.source = source;
            return this;
        }

        public CrawlDataDtoBuilder name(String name) {
            this.name = name;
            return this;
        }

        public CrawlDataDtoBuilder address(String address) {
            this.address = address;
            return this;
        }

        public CrawlDataDtoBuilder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public CrawlDataDtoBuilder rating(String rating) {
            this.rating = rating;
            return this;
        }

        public CrawlDataDtoBuilder reviewCount(String reviewCount) {
            this.reviewCount = reviewCount;
            return this;
        }

        public CrawlDataDtoBuilder bizHour(String bizHour) {
            this.bizHour = bizHour;
            return this;
        }

        public CrawlDataDtoBuilder url(String url) {
            this.url = url;
            return this;
        }

        public CrawlDataDtoBuilder title(String title) {
            this.title = title;
            return this;
        }

        public CrawlDataDtoBuilder imageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
            return this;
        }

        public CrawlDataDtoBuilder content(String content) {
            this.content = content;
            return this;
        }

        public CrawlDataDto build() {
            return new CrawlDataDto(source, name, address, phone, rating, reviewCount, bizHour, url, title, imageUrl,
                    content);
        }
    }
}
