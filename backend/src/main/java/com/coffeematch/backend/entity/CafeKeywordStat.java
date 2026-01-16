package com.coffeematch.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
public class CafeKeywordStat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cafe_id")
    @JsonIgnore
    private Cafe cafe;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "keyword_id")
    private Keyword keyword;

    private Integer count = 0;

    public CafeKeywordStat() {
    }

    public CafeKeywordStat(Cafe cafe, Keyword keyword, Integer count) {
        this.cafe = cafe;
        this.keyword = keyword;
        this.count = count;
    }

    public Long getId() {
        return id;
    }

    public Cafe getCafe() {
        return cafe;
    }

    public Keyword getKeyword() {
        return keyword;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public void incrementCount() {
        this.count++;
    }
}
