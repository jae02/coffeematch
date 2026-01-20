package com.coffeematch.backend.repository;

import com.coffeematch.backend.entity.Platform;
import com.coffeematch.backend.entity.PlatformData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlatformDataRepository extends JpaRepository<PlatformData, Long> {
    List<PlatformData> findByCafeId(Long cafeId);

    Optional<PlatformData> findByCafeIdAndPlatform(Long cafeId, Platform platform);
}
