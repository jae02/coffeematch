package com.coffeematch.backend.repository;

import com.coffeematch.backend.entity.Platform;
import com.coffeematch.backend.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByCafeId(Long cafeId);

    // Incremental update queries
    @Query("SELECT MAX(r.createdAt) FROM Review r WHERE r.cafe.id = :cafeId AND r.sourcePlatform = :platform")
    Optional<LocalDateTime> findLatestReviewDateByCafeAndPlatform(Long cafeId, Platform platform);

    Optional<Review> findBySourcePlatformAndPlatformReviewId(Platform platform, String platformReviewId);

    List<Review> findByCafeIdAndSourcePlatform(Long cafeId, Platform platform);
}
