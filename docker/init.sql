-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(100) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'ROLE_USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create cafe table with denormalized columns
CREATE TABLE IF NOT EXISTS cafe (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(255),
    phone VARCHAR(50),
    description TEXT,
    image_url VARCHAR(255),
    business_type VARCHAR(50),           -- 업태 (커피숍, 다방, 과자점 등)
    source_platform VARCHAR(50),         -- 데이터 출처 (PUBLIC_DATA, KAKAO_MAP 등)
    platform_id VARCHAR(100),            -- 플랫폼 고유 ID
    latitude DOUBLE,                     -- 위도
    longitude DOUBLE,                    -- 경도
    status VARCHAR(20) DEFAULT 'ACTIVE', -- 상태 (NEW, ACTIVE, CLOSED 등)
    last_synced_at TIMESTAMP,            -- 마지막 동기화 시간
    bookmark_count INT DEFAULT 0,
    review_count INT DEFAULT 0,
    internal_rating_avg DOUBLE DEFAULT 0.0
);

-- Create platform_data table for external ratings
CREATE TABLE IF NOT EXISTS platform_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cafe_id BIGINT,
    platform_type VARCHAR(20), -- 'NAVER', 'KAKAO'
    rating DOUBLE,
    review_count INT,
    link VARCHAR(500),
    FOREIGN KEY (cafe_id) REFERENCES cafe(id) ON DELETE CASCADE
);

-- Create keyword table
CREATE TABLE IF NOT EXISTS keyword (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- Create cafe_keyword_stat table for vote counts
CREATE TABLE IF NOT EXISTS cafe_keyword_stat (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cafe_id BIGINT,
    keyword_id BIGINT,
    count INT DEFAULT 0,
    FOREIGN KEY (cafe_id) REFERENCES cafe(id) ON DELETE CASCADE,
    FOREIGN KEY (keyword_id) REFERENCES keyword(id) ON DELETE CASCADE
);

-- Create menu table
CREATE TABLE IF NOT EXISTS menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cafe_id BIGINT,
    item_name VARCHAR(255) NOT NULL,
    price INT NOT NULL,
    is_recommended BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (cafe_id) REFERENCES cafe(id) ON DELETE CASCADE
);

-- Create review table
CREATE TABLE IF NOT EXISTS review (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cafe_id BIGINT,
    author VARCHAR(255),
    rating INT,
    content TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (cafe_id) REFERENCES cafe(id) ON DELETE CASCADE
);

-- Create user_cafe_bookmark table
CREATE TABLE IF NOT EXISTS user_cafe_bookmark (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    cafe_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (cafe_id) REFERENCES cafe(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_cafe_bookmark (user_id, cafe_id)
);

-- Create user_keyword_vote table (to prevent duplicate votes)
CREATE TABLE IF NOT EXISTS user_keyword_vote (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    cafe_id BIGINT,
    keyword_id BIGINT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (cafe_id) REFERENCES cafe(id) ON DELETE CASCADE,
    FOREIGN KEY (keyword_id) REFERENCES keyword(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_keyword_vote (user_id, cafe_id, keyword_id)
);

-- Insert Keywords
INSERT INTO keyword (name) VALUES ('조용한'), ('카공하기 좋은'), ('콘센트 많은'), ('디저트맛집'), ('뷰맛집'), ('주차편한'), ('가성비 좋은'), ('친절한'), ('사진찍기 좋은'), ('야외석 있는');

-- Sample data removed - cafes will be populated from public data
