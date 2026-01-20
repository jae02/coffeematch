package com.coffeematch.backend.service;

import com.coffeematch.backend.entity.Cafe;
import com.coffeematch.backend.entity.Platform;
import com.coffeematch.backend.entity.Review;
import com.coffeematch.backend.repository.CafeRepository;
import com.coffeematch.backend.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 증분 업데이트 서비스
 * - 카페별 최신 리뷰 날짜 기준으로 새 리뷰만 수집
 * - 중복 방지 및 효율적인 크롤링
 */
@Service
public class IncrementalUpdateService {

    private final CafeRepository cafeRepository;
    private final ReviewRepository reviewRepository;

    public IncrementalUpdateService(CafeRepository cafeRepository, ReviewRepository reviewRepository) {
        this.cafeRepository = cafeRepository;
        this.reviewRepository = reviewRepository;
    }

    /**
     * 특정 카페의 최신 리뷰 날짜 조회
     * 
     * @param cafeId   카페 ID
     * @param platform 플랫폼
     * @return 최신 리뷰 날짜 (없으면 null)
     */
    public LocalDateTime getLatestReviewDate(Long cafeId, Platform platform) {
        Optional<LocalDateTime> latest = reviewRepository.findLatestReviewDateByCafeAndPlatform(cafeId, platform);
        return latest.orElse(null);
    }

    /**
     * 신규 리뷰만 수집하여 저장
     * 
     * @param cafe           카페
     * @param crawledReviews 크롤링된 리뷰 목록
     * @return 새로 저장된 리뷰 수
     */
    @Transactional
    public int saveNewReviews(Cafe cafe, List<CrawledReviewData> crawledReviews) {
        int savedCount = 0;

        // 최신 리뷰 날짜 조회
        LocalDateTime latestDate = getLatestReviewDate(cafe.getId(), cafe.getSourcePlatform());

        for (CrawledReviewData crawledData : crawledReviews) {
            // 중복 체크 - platform_review_id로 확인
            Optional<Review> existing = reviewRepository.findBySourcePlatformAndPlatformReviewId(
                    cafe.getSourcePlatform(),
                    crawledData.getPlatformReviewId());

            if (existing.isPresent()) {
                continue; // 이미 존재하는 리뷰
            }

            // 날짜 필터링 (선택적)
            if (latestDate != null && crawledData.getReviewDate() != null) {
                if (crawledData.getReviewDate().isBefore(latestDate)) {
                    continue; // 이미 수집된 리뷰보다 오래됨
                }
            }

            // 새 리뷰 저장
            Review review = new Review();
            review.setCafe(cafe);
            review.setAuthor(crawledData.getReviewerNickname());
            review.setRating(crawledData.getRating());
            review.setContent(crawledData.getContent());
            review.setCreatedAt(crawledData.getReviewDate());
            review.setImageUrl(crawledData.getImageUrl());
            review.setSourcePlatform(cafe.getSourcePlatform());
            review.setPlatformReviewId(crawledData.getPlatformReviewId());
            review.setCrawledAt(LocalDateTime.now());

            reviewRepository.save(review);
            savedCount++;
        }

        // 카페의 last_synced_at 업데이트
        cafe.setLastSyncedAt(LocalDateTime.now());
        cafeRepository.save(cafe);

        return savedCount;
    }

    /**
     * 플랫폼의 모든 카페에 대해 증분 리뷰 수집
     * 
     * @param platform 플랫폼
     * @return 수집된 총 리뷰 수
     */
    @Transactional
    public int collectNewReviewsForAllCafes(Platform platform) {
        List<Cafe> cafes = cafeRepository.findBySourcePlatform(platform);
        int totalReviews = 0;

        for (Cafe cafe : cafes) {
            try {
                // TODO: 실제 크롤러 호출
                // List<CrawledReviewData> reviews =
                // crawlerService.getReviews(cafe.getPlatformId());
                List<CrawledReviewData> reviews = new ArrayList<>();

                int saved = saveNewReviews(cafe, reviews);
                totalReviews += saved;

                // Anti-bot: 카페간 지연
                Thread.sleep((long) (1000 + Math.random() * 2000));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("Error collecting reviews for cafe " + cafe.getId() + ": " + e.getMessage());
            }
        }

        return totalReviews;
    }

    /**
     * 오래된 카페 우선 리뷰 수집 (last_synced_at 기준)
     * 
     * @param platform 플랫폼
     * @param daysOld  며칠 이상 업데이트되지 않은 카페
     * @param limit    수집할 카페 수 제한
     * @return 수집된 총 리뷰 수
     */
    @Transactional
    public int collectReviewsForOldCafes(Platform platform, int daysOld, int limit) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(daysOld);
        List<Cafe> oldCafes = cafeRepository.findByLastSyncedAtBefore(threshold);

        int totalReviews = 0;
        int count = 0;

        for (Cafe cafe : oldCafes) {
            if (!cafe.getSourcePlatform().equals(platform)) {
                continue;
            }

            if (count >= limit) {
                break;
            }

            try {
                // TODO: 크롤러 호출
                List<CrawledReviewData> reviews = new ArrayList<>();

                int saved = saveNewReviews(cafe, reviews);
                totalReviews += saved;
                count++;

                Thread.sleep((long) (1000 + Math.random() * 2000));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("Error collecting reviews for cafe " + cafe.getId() + ": " + e.getMessage());
            }
        }

        return totalReviews;
    }

    /**
     * 크롤링된 리뷰 데이터 DTO
     */
    public static class CrawledReviewData {
        private String platformReviewId;
        private String reviewerNickname;
        private Integer rating;
        private String content;
        private LocalDateTime reviewDate;
        private String imageUrl;

        public CrawledReviewData() {
        }

        public CrawledReviewData(String platformReviewId, String reviewerNickname, Integer rating,
                String content, LocalDateTime reviewDate, String imageUrl) {
            this.platformReviewId = platformReviewId;
            this.reviewerNickname = reviewerNickname;
            this.rating = rating;
            this.content = content;
            this.reviewDate = reviewDate;
            this.imageUrl = imageUrl;
        }

        // Getters and setters
        public String getPlatformReviewId() {
            return platformReviewId;
        }

        public void setPlatformReviewId(String platformReviewId) {
            this.platformReviewId = platformReviewId;
        }

        public String getReviewerNickname() {
            return reviewerNickname;
        }

        public void setReviewerNickname(String reviewerNickname) {
            this.reviewerNickname = reviewerNickname;
        }

        public Integer getRating() {
            return rating;
        }

        public void setRating(Integer rating) {
            this.rating = rating;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public LocalDateTime getReviewDate() {
            return reviewDate;
        }

        public void setReviewDate(LocalDateTime reviewDate) {
            this.reviewDate = reviewDate;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }
    }
}
