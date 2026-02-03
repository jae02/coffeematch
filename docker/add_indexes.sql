-- Database indexes for CoffeeMatch performance optimization
-- Run this on the production database

-- Index for name search
CREATE INDEX idx_cafe_name ON cafe(name);

-- Index for address search
CREATE INDEX idx_cafe_address ON cafe(address(100));

-- Index for location-based queries
CREATE INDEX idx_cafe_location ON cafe(latitude, longitude);

-- Index for business type filter
CREATE INDEX idx_cafe_business_type ON cafe(business_type);

-- Index for status filter
CREATE INDEX idx_cafe_status ON cafe(status);

-- Index for platform queries
CREATE INDEX idx_cafe_platform ON cafe(source_platform, platform_id);

-- Show created indexes
SHOW INDEX FROM cafe;
