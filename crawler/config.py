"""
Configuration for Hybrid Data Pipeline
"""
import os
from dotenv import load_dotenv

load_dotenv()

# Public API
PUBLIC_API_KEY = os.getenv('PUBLIC_API_KEY', '')
PUBLIC_API_URL = "http://apis.data.go.kr/B553077/api/open/sdsc2/storeListInUpjong"

# Kakao API
KAKAO_REST_API_KEY = os.getenv('KAKAO_REST_API_KEY', '')

# Gemini AI
GEMINI_API_KEY = os.getenv('GEMINI_API_KEY', '')
AI_MODEL = 'gemini-1.5-flash'

# Database
DB_CONFIG = {
    'host': os.getenv('DB_HOST', 'localhost'),
    'port': int(os.getenv('DB_PORT', '3307')),
    'user': os.getenv('DB_USER', 'root'),
    'password': os.getenv('DB_PASSWORD', '1234'),
    'database': os.getenv('DB_NAME', 'coffeematch'),
    'charset': 'utf8mb4'
}

# Matching Settings
COORDINATE_MATCH_RADIUS = 50.0  # meters
NAME_SIMILARITY_THRESHOLD = 0.6

# Scraping Settings
MAX_REVIEWS_PER_CAFE = 30
SCRAPE_DELAY_MS = 1000

# Batch Processing
BATCH_SIZE = 100
CONCURRENT_REQUESTS = 5
