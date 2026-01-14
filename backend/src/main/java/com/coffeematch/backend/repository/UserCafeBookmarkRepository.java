package com.coffeematch.backend.repository;

import com.coffeematch.backend.entity.UserCafeBookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserCafeBookmarkRepository extends JpaRepository<UserCafeBookmark, Long> {
    Optional<UserCafeBookmark> findByUserIdAndCafeId(Long userId, Long cafeId);

    boolean existsByUserIdAndCafeId(Long userId, Long cafeId);
}
