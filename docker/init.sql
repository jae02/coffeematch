-- Users Table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(100) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'ROLE_USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Cafes Tables
CREATE TABLE IF NOT EXISTS cafe (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(255),
    phone VARCHAR(50),
    description TEXT,
    image_url VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cafe_id BIGINT,
    item_name VARCHAR(255) NOT NULL,
    price INT NOT NULL,
    is_recommended BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (cafe_id) REFERENCES cafe(id)
);

CREATE TABLE IF NOT EXISTS review (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cafe_id BIGINT,
    author VARCHAR(255),
    rating INT,
    content TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (cafe_id) REFERENCES cafe(id)
);

-- Insert Seed Data (5 Cafes)
INSERT INTO cafe (name, address, phone, description, image_url) VALUES 
('Starbrew', '123 Coffee Lane, Seoul', '02-1234-5678', 'A cozy place for coding and coffee.', 'https://placehold.co/600x400/D2B48C/ffffff?text=Starbrew'),
('Espresso Lab', '456 Bean St, Seoul', '02-8765-4321', 'Experimental blends for the connoisseur.', 'https://placehold.co/600x400/8B4513/ffffff?text=Espresso+Lab'),
('Morning Dew', '789 Morning Ave, Seoul', '02-1111-2222', 'Start your day with the freshest brew.', 'https://placehold.co/600x400/A0522D/ffffff?text=Morning+Dew'),
('Cafe Noir', '321 Dark Rd, Seoul', '02-3333-4444', 'Late night coffee for night owls.', 'https://placehold.co/600x400/2F4F4F/ffffff?text=Cafe+Noir'),
('Golden Mug', '654 Gold St, Seoul', '02-5555-6666', 'Premium coffee in a luxurious setting.', 'https://placehold.co/600x400/DAA520/ffffff?text=Golden+Mug');

-- Insert Seed Data (Menus - 3 per cafe)
-- Starbrew (ID 1)
INSERT INTO menu (cafe_id, item_name, price, is_recommended) VALUES
(1, 'Americano', 4500, true),
(1, 'Latte', 5000, false),
(1, 'Cappuccino', 5000, false);

-- Espresso Lab (ID 2)
INSERT INTO menu (cafe_id, item_name, price, is_recommended) VALUES
(2, 'Espresso', 4000, true),
(2, 'Macchiato', 4500, false),
(2, 'Flat White', 5000, true);

-- Morning Dew (ID 3)
INSERT INTO menu (cafe_id, item_name, price, is_recommended) VALUES
(3, 'Cold Brew', 5500, true),
(3, 'Vanilla Latte', 5500, false),
(3, 'Bagel Set', 8000, true);

-- Cafe Noir (ID 4)
INSERT INTO menu (cafe_id, item_name, price, is_recommended) VALUES
(4, 'Black Coffee', 4000, true),
(4, 'Dark Mocha', 6000, true),
(4, 'Irish Coffee', 7000, false);

-- Golden Mug (ID 5)
INSERT INTO menu (cafe_id, item_name, price, is_recommended) VALUES
(5, 'Gold Blend', 6000, true),
(5, 'Caramel Latte', 6500, false),
(5, 'Affogato', 7000, true);
