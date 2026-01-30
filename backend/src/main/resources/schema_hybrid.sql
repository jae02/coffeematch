-- Creama 하이브리드 데이터 파이프라인 스키마
-- Phase 1: 마스터 데이터 (공공 API 기준)

CREATE TABLE IF NOT EXISTS cafe_master (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- 공공데이터 원본
    business_number VARCHAR(20) UNIQUE NOT NULL COMMENT '인허가번호 (고유키)',
    business_name VARCHAR(200) NOT NULL COMMENT '상호명',
    
    -- 주소
    jibun_address VARCHAR(300) COMMENT '지번주소',
    road_address VARCHAR(300) COMMENT '도로명주소',
    
    -- 좌표 (매칭의 핵심!)
    latitude DECIMAL(10, 7) NOT NULL COMMENT '위도',
    longitude DECIMAL(11, 7) NOT NULL COMMENT '경도',
    
    -- 분류
    industry_code VARCHAR(10) COMMENT '업종코드',
    industry_name VARCHAR(50) COMMENT '업종명',
    
    -- 상태
    status ENUM('ACTIVE', 'CLOSED', 'UNKNOWN') DEFAULT 'ACTIVE',
    opened_at DATE COMMENT '인허가일자',
    
    -- 메타데이터
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- 인덱스 (좌표 검색 최적화)
    INDEX idx_coordinates (latitude, longitude),
    INDEX idx_business_name (business_name),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Phase 2: 지도 데이터 풍부화

CREATE TABLE IF NOT EXISTS cafe_detail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    master_id BIGINT NOT NULL COMMENT 'cafe_master FK',
    
    -- 플랫폼 정보
    platform ENUM('KAKAO', 'NAVER') NOT NULL,
    place_id VARCHAR(100) NOT NULL COMMENT '플랫폼별 장소 ID',
    place_url VARCHAR(500) COMMENT '플랫폼 페이지 URL',
    
    -- 평점 및 리뷰
    rating DECIMAL(2, 1) COMMENT '별점 (0.0 ~ 5.0)',
    review_count INT DEFAULT 0 COMMENT '리뷰 개수',
    
    -- 추가 정보
    phone VARCHAR(20),
    business_hours TEXT COMMENT '영업시간 JSON',
    
    -- 메타데이터
    matched_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '매칭 시각',
    last_synced_at TIMESTAMP COMMENT '최종 동기화 시각',
    
    FOREIGN KEY (master_id) REFERENCES cafe_master(id) ON DELETE CASCADE,
    UNIQUE KEY unique_master_platform (master_id, platform),
    INDEX idx_platform_place (platform, place_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Phase 3: 크리마 지수

CREATE TABLE IF NOT EXISTS crema_score (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    master_id BIGINT UNIQUE NOT NULL,
    
    -- 개별 점수
    kakao_score DECIMAL(3, 1) COMMENT '카카오 점수 (0-5)',
    naver_score DECIMAL(3, 1) COMMENT '네이버 점수 (0-5)',
    review_volume_score INT COMMENT '리뷰 볼륨 점수 (0-100)',
    
    -- 최종 크리마 지수
    -- 공식: (kakao * 50% + naver * 30% + volume * 20%) * 20
    crema_score INT NOT NULL COMMENT '최종 점수 (0-100)',
    
    calculated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (master_id) REFERENCES cafe_master(id) ON DELETE CASCADE,
    INDEX idx_crema_score (crema_score DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Phase 4: AI 분석 결과 (리뷰 원문 저장 금지!)

CREATE TABLE IF NOT EXISTS cafe_insights (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    master_id BIGINT NOT NULL,
    
    -- AI 분석 결과만 저장
    summary VARCHAR(500) NOT NULL COMMENT '한 줄 요약',
    keywords JSON NOT NULL COMMENT '키워드 태그',
    sentiment_score INT COMMENT '감성 점수 (0-100)',
    
    -- 메타데이터
    source_platform ENUM('NAVER', 'KAKAO') NOT NULL,
    review_count INT NOT NULL COMMENT '분석된 리뷰 수',
    analyzed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (master_id) REFERENCES cafe_master(id) ON DELETE CASCADE,
    UNIQUE KEY unique_master_platform (master_id, source_platform)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
