package com.coffeematch.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class UserCafeBookmark {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cafe_id")
    private Cafe cafe;

    private LocalDateTime createdAt = LocalDateTime.now();

    public UserCafeBookmark() {
    }

    public UserCafeBookmark(User user, Cafe cafe) {
        this.user = user;
        this.cafe = cafe;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
