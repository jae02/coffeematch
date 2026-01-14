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

-- Insert seed data for Cafes
INSERT INTO cafe (name, address, phone, description, image_url, bookmark_count, review_count, internal_rating_avg) VALUES 
('Starbrew', '123 Coffee Lane, Seoul', '02-1234-5678', 'A cozy place for coding and coffee.', 'https://placehold.co/600x400/D2B48C/ffffff?text=Starbrew', 15, 5, 4.5),
('Espresso Lab', '456 Caffeine Blvd, Seoul', '02-8765-4321', 'Modern interior with specalty beans.', 'https://placehold.co/600x400/8B4513/ffffff?text=Espresso+Lab', 8, 3, 4.0),
('Morning Dew', '789 Sunrise Ave, Seoul', '02-1111-2222', 'Fresh coffee to start your day.', 'https://placehold.co/600x400/A0522D/ffffff?text=Morning+Dew', 22, 10, 4.8),
('Cafe Noir', '101 Midnight St, Seoul', '02-3333-4444', 'Dark roast specialist. Open late.', 'https://placehold.co/600x400/2F4F4F/ffffff?text=Cafe+Noir', 5, 1, 3.5),
('Golden Mug', '202 Luxury Rd, Seoul', '02-5555-6666', 'Premium atmosphere for meetings.', 'https://placehold.co/600x400/DAA520/ffffff?text=Golden+Mug', 30, 12, 4.2);

-- Insert Platform Data
INSERT INTO platform_data (cafe_id, platform_type, rating, review_count, link) VALUES
(1, 'NAVER', 4.5, 120, 'https://naver.com'), (1, 'KAKAO', 4.2, 50, 'https://kakao.com'),
(2, 'NAVER', 4.0, 80, 'https://naver.com'), (2, 'KAKAO', 3.8, 30, 'https://kakao.com'),
(3, 'NAVER', 4.8, 200, 'https://naver.com'), (3, 'KAKAO', 4.7, 150, 'https://kakao.com');

-- Insert Menu items
INSERT INTO menu (cafe_id, item_name, price, is_recommended) VALUES
(1, 'Americano', 4500, true), (1, 'Latte', 5000, false), (1, 'Cheesecake', 6500, true),
(2, 'Espresso', 4000, true), (2, 'Flat White', 5500, false), (2, 'Brownie', 4500, false),
(3, 'Cold Brew', 5000, true), (3, 'Bagel', 3500, false), (3, 'Sandwich', 7000, true),
(4, 'Dark Roast', 4500, true), (4, 'Affogato', 6000, false),
(5, 'Gold Latte', 6500, true), (5, 'Tiramisu', 7500, false);
