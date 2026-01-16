package com.coffeematch.backend.repository;

import com.coffeematch.backend.entity.UserKeywordVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserKeywordVoteRepository extends JpaRepository<UserKeywordVote, Long> {
    boolean existsByUserIdAndCafeIdAndKeywordId(Long userId, Long cafeId, Long keywordId);

    java.util.Optional<UserKeywordVote> findByUserIdAndCafeId(Long userId, Long cafeId);
}
