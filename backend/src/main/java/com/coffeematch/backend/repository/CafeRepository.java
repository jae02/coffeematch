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

    // Platform tracking queries
    Optional<Cafe> findBySourcePlatformAndPlatformId(Platform platform, String platformId);

    List<Cafe> findByStatus(CafeStatus status);

    List<Cafe> findByLastSyncedAtBefore(LocalDateTime threshold);

    List<Cafe> findBySourcePlatform(Platform platform);
}
