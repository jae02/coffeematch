package com.coffeematch.backend.service;

import com.coffeematch.backend.entity.Cafe;
import com.coffeematch.backend.entity.CafeStatus;
import com.coffeematch.backend.entity.Platform;
import com.coffeematch.backend.repository.CafeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 폐업 검증 서비스
 * - 기존 DB의 카페들을 주기적으로 재확인
 * - 플랫폼에서 접근 불가 시 CLOSED_SUSPECTED로 표시
 * - 연속 실패 시 CLOSED_CONFIRMED로 전환
 */
@Service
public class ValidationService {

    private final CafeRepository cafeRepository;
    private static final int CLOSURE_THRESHOLD = 3; // 3회 연속 실패 시 폐업 확정

    public ValidationService(CafeRepository cafeRepository) {
        this.cafeRepository = cafeRepository;
    }

    /**
     * 특정 카페의 존재 여부 검증
     * 
     * @param cafeId      카페 ID
     * @param stillExists 플랫폼에서 여전히 존재하는지 여부
     * @return 업데이트된 카페 상태
     */
    @Transactional
    public CafeStatus validateCafe(Long cafeId, boolean stillExists) {
        Optional<Cafe> cafeOpt = cafeRepository.findById(cafeId);
        if (cafeOpt.isEmpty()) {
            return null;
        }

        Cafe cafe = cafeOpt.get();

        if (stillExists) {
            // 카페가 여전히 존재 - ACTIVE로 복구
            if (cafe.getStatus() == CafeStatus.CLOSED_SUSPECTED) {
                cafe.setStatus(CafeStatus.ACTIVE);
            }
            cafe.setLastSyncedAt(LocalDateTime.now());
        } else {
            // 카페 접근 실패
            if (cafe.getStatus() == CafeStatus.ACTIVE || cafe.getStatus() == CafeStatus.NEW) {
                // 첫 실패 - CLOSED_SUSPECTED로 전환
                cafe.setStatus(CafeStatus.CLOSED_SUSPECTED);
            } else if (cafe.getStatus() == CafeStatus.CLOSED_SUSPECTED) {
                // 이미 의심 상태 - 실패 횟수 체크 (간단하게는 last_synced_at 기준)
                // 실제로는 별도 failed_check_count 필드가 필요할 수 있음
                // 여기서는 3회 검증 실패를 가정하고 CLOSED_CONFIRMED로 전환
                cafe.setStatus(CafeStatus.CLOSED_CONFIRMED);
            }
        }

        cafeRepository.save(cafe);
        return cafe.getStatus();
    }

    /**
     * 플랫폼 ID로 카페가 여전히 존재하는지 확인
     * 
     * @param platform   플랫폼
     * @param platformId 플랫폼 고유 ID
     * @return 존재 여부
     */
    public boolean checkExistence(Platform platform, String platformId) {
        // TODO: 실제 크롤러 호출하여 페이지 접근 확인
        // 예: KakaoMapCrawler.checkCafeExists(platformId)
        // 404 또는 검색 결과 없음 -> false 반환

        // 임시 구현
        return true;
    }

    /**
     * CLOSED_SUSPECTED 상태의 모든 카페 재검증
     * 
     * @return 검증된 카페 수
     */
    @Transactional
    public int validateSuspectedCafes() {
        List<Cafe> suspectedCafes = cafeRepository.findByStatus(CafeStatus.CLOSED_SUSPECTED);
        int validatedCount = 0;

        for (Cafe cafe : suspectedCafes) {
            try {
                boolean exists = checkExistence(cafe.getSourcePlatform(), cafe.getPlatformId());
                validateCafe(cafe.getId(), exists);
                validatedCount++;

                // Anti-bot: 요청간 지연
                Thread.sleep((long) (1000 + Math.random() * 2000));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("Error validating cafe " + cafe.getId() + ": " + e.getMessage());
            }
        }

        return validatedCount;
    }

    /**
     * 모든 ACTIVE 카페의 정기 검증 (샘플링)
     * 
     * @param sampleSize 검증할 카페 수
     * @return 검증된 카페 수
     */
    @Transactional
    public int validateActiveCafesSample(int sampleSize) {
        List<Cafe> activeCafes = cafeRepository.findByStatus(CafeStatus.ACTIVE);

        // 랜덤 샘플링
        int count = Math.min(sampleSize, activeCafes.size());
        int validatedCount = 0;

        for (int i = 0; i < count; i++) {
            Cafe cafe = activeCafes.get(i);
            try {
                boolean exists = checkExistence(cafe.getSourcePlatform(), cafe.getPlatformId());
                validateCafe(cafe.getId(), exists);
                validatedCount++;

                Thread.sleep((long) (1000 + Math.random() * 2000));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("Error validating cafe " + cafe.getId() + ": " + e.getMessage());
            }
        }

        return validatedCount;
    }

    /**
     * 오래된 카페 우선 검증 (last_synced_at 기준)
     * 
     * @param daysOld 며칠 이상 업데이트되지 않은 카페
     * @return 검증된 카페 수
     */
    @Transactional
    public int validateOldCafes(int daysOld) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(daysOld);
        List<Cafe> oldCafes = cafeRepository.findByLastSyncedAtBefore(threshold);
        int validatedCount = 0;

        for (Cafe cafe : oldCafes) {
            try {
                boolean exists = checkExistence(cafe.getSourcePlatform(), cafe.getPlatformId());
                validateCafe(cafe.getId(), exists);
                validatedCount++;

                Thread.sleep((long) (1000 + Math.random() * 2000));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("Error validating cafe " + cafe.getId() + ": " + e.getMessage());
            }
        }

        return validatedCount;
    }
}
