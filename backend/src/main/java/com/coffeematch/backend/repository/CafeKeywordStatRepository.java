package com.coffeematch.backend.repository;

import com.coffeematch.backend.entity.CafeKeywordStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CafeKeywordStatRepository extends JpaRepository<CafeKeywordStat, Long> {
    List<CafeKeywordStat> findByCafeId(Long cafeId);

    Optional<CafeKeywordStat> findByCafeIdAndKeywordId(Long cafeId, Long keywordId);
}
