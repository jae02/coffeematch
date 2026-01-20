package com.coffeematch.backend.service;

import com.coffeematch.backend.entity.Cafe;
import com.coffeematch.backend.repository.CafeRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 데이터 정규화 서비스
 * - 주소 통일 (지번 ↔ 도로명)
 * - 좌표 기반 중복 제거
 * - 가게명 유사도 비교
 */
@Service
public class DataNormalizationService {

    private final CafeRepository cafeRepository;

    // 중복 판정 기준
    private static final double DISTANCE_THRESHOLD_METERS = 50.0; // 50m 이내
    private static final double NAME_SIMILARITY_THRESHOLD = 0.8; // 80% 이상 유사

    public DataNormalizationService(CafeRepository cafeRepository) {
        this.cafeRepository = cafeRepository;
    }

    /**
     * 주소 정규화 - 괄호 안 정보 제거, 공백 정리
     */
    public String normalizeAddress(String address) {
        if (address == null || address.isEmpty()) {
            return "";
        }

        // 괄호 안 내용 제거 (예: "서울 성수동1가(성수동1가)" -> "서울 성수동1가")
        String normalized = address.replaceAll("\\([^)]*\\)", "");

        // 연속된 공백 제거
        normalized = normalized.replaceAll("\\s+", " ").trim();

        // 전각 문자를 반각으로 변환 (필요시)
        normalized = normalized.replace("１", "1")
                .replace("２", "2")
                .replace("３", "3");

        return normalized;
    }

    /**
     * 지번 주소 추출
     */
    public String extractJibunAddress(String fullAddress) {
        // 간단한 패턴 매칭 (실제로는 더 정교한 로직 필요)
        Pattern pattern = Pattern.compile("([가-힣]+동|[가-힣]+로)\\s+\\d+(-\\d+)?");
        Matcher matcher = pattern.matcher(fullAddress);

        if (matcher.find()) {
            return matcher.group();
        }

        return fullAddress;
    }

    /**
     * 두 좌표 간 거리 계산 (Haversine formula)
     * 
     * @return 거리 (미터)
     */
    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371000; // 지구 반지름 (미터)

        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c; // 미터 단위
    }

    /**
     * 문자열 유사도 계산 (Levenshtein Distance 기반)
     * 
     * @return 유사도 (0.0 ~ 1.0)
     */
    public double calculateSimilarity(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return 0.0;
        }

        if (s1.equals(s2)) {
            return 1.0;
        }

        int distance = levenshteinDistance(s1, s2);
        int maxLength = Math.max(s1.length(), s2.length());

        return 1.0 - ((double) distance / maxLength);
    }

    /**
     * Levenshtein Distance 알고리즘
     */
    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }

        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = Math.min(
                            Math.min(dp[i - 1][j], dp[i][j - 1]),
                            dp[i - 1][j - 1]) + 1;
                }
            }
        }

        return dp[s1.length()][s2.length()];
    }

    /**
     * 중복 카페 탐지
     * - 좌표 기반 (50m 이내)
     * - 가게명 유사도 (80% 이상)
     * - 전화번호 일치
     */
    public List<Cafe> findDuplicates(Cafe newCafe) {
        List<Cafe> duplicates = new ArrayList<>();
        List<Cafe> allCafes = cafeRepository.findAll();

        for (Cafe existing : allCafes) {
            if (existing.getId().equals(newCafe.getId())) {
                continue; // 자기 자신 제외
            }

            boolean isDuplicate = false;

            // 1. 좌표 기반 검사
            if (newCafe.getLatitude() != null && newCafe.getLongitude() != null &&
                    existing.getLatitude() != null && existing.getLongitude() != null) {

                double distance = calculateDistance(
                        newCafe.getLatitude(), newCafe.getLongitude(),
                        existing.getLatitude(), existing.getLongitude());

                if (distance <= DISTANCE_THRESHOLD_METERS) {
                    // 가게명 유사도 추가 확인
                    double similarity = calculateSimilarity(
                            newCafe.getName(),
                            existing.getName());

                    if (similarity >= NAME_SIMILARITY_THRESHOLD) {
                        isDuplicate = true;
                    }
                }
            }

            // 2. 전화번호 일치
            if (newCafe.getPhone() != null && !newCafe.getPhone().isEmpty() &&
                    newCafe.getPhone().equals(existing.getPhone())) {
                isDuplicate = true;
            }

            // 3. 가게명 + 주소 정규화 후 완전 일치
            String newAddress = normalizeAddress(newCafe.getAddress());
            String existingAddress = normalizeAddress(existing.getAddress());

            if (newCafe.getName().equals(existing.getName()) &&
                    newAddress.equals(existingAddress)) {
                isDuplicate = true;
            }

            if (isDuplicate) {
                duplicates.add(existing);
            }
        }

        return duplicates;
    }

    /**
     * 플랫폼별 주소 정규화 (플랫폼마다 주소 형식이 다를 수 있음)
     */
    public String normalizePlatformSpecificAddress(String address, String platform) {
        String normalized = normalizeAddress(address);

        // 플랫폼별 특수 처리
        switch (platform) {
            case "KAKAO_MAP":
                // 카카오맵은 "지번" 표시를 제거
                normalized = normalized.replace("지번", "").trim();
                break;
            case "NAVER_MAP":
                // 네이버맵은 "도로명" 표시를 제거
                normalized = normalized.replace("도로명", "").trim();
                break;
            default:
                break;
        }

        return normalized;
    }

    /**
     * 전화번호 정규화
     */
    public String normalizePhoneNumber(String phone) {
        if (phone == null || phone.isEmpty()) {
            return "";
        }

        // 모든 특수문자 제거
        String normalized = phone.replaceAll("[^0-9]", "");

        // 010-1234-5678 형식으로 변환 (선택적)
        if (normalized.length() == 11 && normalized.startsWith("010")) {
            return normalized.substring(0, 3) + "-" +
                    normalized.substring(3, 7) + "-" +
                    normalized.substring(7);
        } else if (normalized.length() == 10) {
            return normalized.substring(0, 3) + "-" +
                    normalized.substring(3, 6) + "-" +
                    normalized.substring(6);
        }

        return normalized;
    }
}
