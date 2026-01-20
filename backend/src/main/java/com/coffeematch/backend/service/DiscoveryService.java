package com.coffeematch.backend.service;

import com.coffeematch.backend.entity.Cafe;
import com.coffeematch.backend.entity.CafeStatus;
import com.coffeematch.backend.entity.Platform;
import com.coffeematch.backend.repository.CafeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 신규 카페 탐색 서비스
 * - 플랫폼별 키워드 검색 결과와 DB 비교
 * - 새로운 platform_id 발견 시 NEW 상태로 등록
 */
@Service
public class DiscoveryService {

    private final CafeRepository cafeRepository;

    public DiscoveryService(CafeRepository cafeRepository) {
        this.cafeRepository = cafeRepository;
    }

    /**
     * 특정 플랫폼에서 수집한 카페 목록을 DB와 비교하여 신규 카페 탐색
     * 
     * @param platform     플랫폼 (KAKAO_MAP, NAVER_MAP, etc.)
     * @param crawledCafes 크롤링으로 수집한 카페 정보 리스트
     * @return 신규로 등록된 카페 리스트
     */
    @Transactional
    public List<Cafe> discoverNewCafes(Platform platform, List<CrawledCafeData> crawledCafes) {
        List<Cafe> newCafes = new ArrayList<>();

        for (CrawledCafeData crawledData : crawledCafes) {
            // DB에 해당 platform_id가 존재하는지 확인
            Optional<Cafe> existing = cafeRepository.findBySourcePlatformAndPlatformId(
                    platform,
                    crawledData.getPlatformId());

            if (existing.isEmpty()) {
                // 새로운 카페 발견
                Cafe newCafe = new Cafe();
                newCafe.setName(crawledData.getName());
                newCafe.setAddress(crawledData.getAddress());
                newCafe.setPhone(crawledData.getPhone());
                newCafe.setSourcePlatform(platform);
                newCafe.setPlatformId(crawledData.getPlatformId());
                newCafe.setStatus(CafeStatus.NEW);
                newCafe.setLatitude(crawledData.getLatitude());
                newCafe.setLongitude(crawledData.getLongitude());
                newCafe.setLastSyncedAt(LocalDateTime.now());

                Cafe saved = cafeRepository.save(newCafe);
                newCafes.add(saved);
            } else {
                // 기존 카페 - last_synced_at만 업데이트
                Cafe cafe = existing.get();
                cafe.setLastSyncedAt(LocalDateTime.now());
                cafeRepository.save(cafe);
            }
        }

        return newCafes;
    }

    /**
     * 지역별 키워드 검색을 통한 대량 탐색
     * 
     * @param platform 플랫폼
     * @param region   지역명 (예: "성수동", "강남구")
     * @param keyword  키워드 (예: "카페", "베이커리")
     * @return 신규 발견된 카페 수
     */
    @Transactional
    public int discoverByRegion(Platform platform, String region, String keyword) {
        // 실제 구현 시 크롤러 서비스 호출
        // 예: KakaoMapCrawler.crawlDetail(region + " " + keyword, limit)

        // 임시 구현 - 실제로는 크롤러에서 데이터를 가져와야 함
        List<CrawledCafeData> crawledData = new ArrayList<>();
        // TODO: 크롤러 통합

        List<Cafe> newCafes = discoverNewCafes(platform, crawledData);
        return newCafes.size();
    }

    /**
     * 모든 행정구역에 대해 순회하며 신규 카페 탐색
     * 
     * @param platform 플랫폼
     * @param regions  행정구역 목록
     * @param keyword  검색 키워드
     * @return 총 발견된 신규 카페 수
     */
    @Transactional
    public int bulkDiscovery(Platform platform, List<String> regions, String keyword) {
        int totalNew = 0;

        for (String region : regions) {
            try {
                int newCount = discoverByRegion(platform, region, keyword);
                totalNew += newCount;

                // Anti-bot: 지역간 랜덤 지연
                Thread.sleep((long) (2000 + Math.random() * 3000));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                // 로그 기록 후 계속 진행
                System.err.println("Error discovering in " + region + ": " + e.getMessage());
            }
        }

        return totalNew;
    }

    /**
     * 크롤링된 카페 데이터 DTO
     */
    public static class CrawledCafeData {
        private String platformId;
        private String name;
        private String address;
        private String phone;
        private Double latitude;
        private Double longitude;

        public CrawledCafeData() {
        }

        public CrawledCafeData(String platformId, String name, String address, String phone,
                Double latitude, Double longitude) {
            this.platformId = platformId;
            this.name = name;
            this.address = address;
            this.phone = phone;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        // Getters and setters
        public String getPlatformId() {
            return platformId;
        }

        public void setPlatformId(String platformId) {
            this.platformId = platformId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public Double getLatitude() {
            return latitude;
        }

        public void setLatitude(Double latitude) {
            this.latitude = latitude;
        }

        public Double getLongitude() {
            return longitude;
        }

        public void setLongitude(Double longitude) {
            this.longitude = longitude;
        }
    }
}
