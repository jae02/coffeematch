package com.coffeematch.backend.entity;

import jakarta.persistence.*;

@Entity
public class UserKeywordVote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cafe_id")
    private Cafe cafe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "keyword_id")
    private Keyword keyword;

    public UserKeywordVote() {
    }

    public UserKeywordVote(User user, Cafe cafe, Keyword keyword) {
        this.user = user;
        this.cafe = cafe;
        this.keyword = keyword;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Cafe getCafe() {
        return cafe;
    }

    public Keyword getKeyword() {
        return keyword;
    }
}
