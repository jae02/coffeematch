package com.coffeematch.backend.repository;

import com.coffeematch.backend.entity.Cafe;
import com.coffeematch.backend.entity.CafeStatus;
import com.coffeematch.backend.entity.Platform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CafeRepository extends JpaRepository<Cafe, Long> {
    List<Cafe> findByNameContainingIgnoreCase(String keyword);

    org.springframework.data.domain.Page<Cafe> findByNameContainingIgnoreCase(String keyword,
            org.springframework.data.domain.Pageable pageable);

    // Platform tracking queries
    Optional<Cafe> findBySourcePlatformAndPlatformId(Platform platform, String platformId);

    List<Cafe> findByStatus(CafeStatus status);

    List<Cafe> findByLastSyncedAtBefore(LocalDateTime threshold);

    List<Cafe> findBySourcePlatform(Platform platform);

    // Location-based query using Haversine formula
    @org.springframework.data.jpa.repository.Query(value = "SELECT * FROM cafe c WHERE " +
            "c.latitude IS NOT NULL AND c.longitude IS NOT NULL AND " +
            "(6371000 * acos(cos(radians(:latitude)) * cos(radians(c.latitude)) * " +
            "cos(radians(c.longitude) - radians(:longitude)) + " +
            "sin(radians(:latitude)) * sin(radians(c.latitude)))) <= :radius " +
            "ORDER BY (6371000 * acos(cos(radians(:latitude)) * cos(radians(c.latitude)) * " +
            "cos(radians(c.longitude) - radians(:longitude)) + " +
            "sin(radians(:latitude)) * sin(radians(c.latitude))))", countQuery = "SELECT COUNT(*) FROM cafe c WHERE " +
                    "c.latitude IS NOT NULL AND c.longitude IS NOT NULL AND " +
                    "(6371000 * acos(cos(radians(:latitude)) * cos(radians(c.latitude)) * " +
                    "cos(radians(c.longitude) - radians(:longitude)) + " +
                    "sin(radians(:latitude)) * sin(radians(c.latitude)))) <= :radius", nativeQuery = true)
    org.springframework.data.domain.Page<Cafe> findNearby(
            @org.springframework.data.repository.query.Param("latitude") Double latitude,
            @org.springframework.data.repository.query.Param("longitude") Double longitude,
            @org.springframework.data.repository.query.Param("radius") Double radius,
            org.springframework.data.domain.Pageable pageable);
}
