from dataclasses import dataclass, field
from typing import List, Dict, Any, Optional
import time
import random
import asyncio
from datetime import datetime
from playwright.async_api import async_playwright, Browser, BrowserContext, Page

# ==================== 데이터 모델 ====================

@dataclass
class CafeData:
    """카페 기본 정보"""
    name: str
    address: str = ""
    phone: str = ""
    business_hours: str = ""
    category: str = ""
    latitude: Optional[float] = None
    longitude: Optional[float] = None
    source_platform: str = ""
    platform_id: str = ""
    last_synced_at: str = field(default_factory=lambda: datetime.now().isoformat())
    status: str = "NEW"
    raw_data: Dict[str, Any] = field(default_factory=dict)


@dataclass
class ReviewData:
    """리뷰 정보"""
    cafe_platform_id: str
    reviewer_nickname: str
    rating: Optional[int] = None
    content: str = ""
    review_date: str = ""
    image_url: str = ""
    source_platform: str = ""
    platform_review_id: str = ""
    crawled_at: str = field(default_factory=lambda: datetime.now().isoformat())


# ==================== Anti-Bot 유틸리티 ====================

class AntiBot:
    """Anti-bot 우회 기법 모음"""
    
    USER_AGENTS = [
        'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
        'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.1 Safari/605.1.15',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:121.0) Gecko/20100101 Firefox/121.0',
        'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36'
    ]
    
    @staticmethod
    def random_delay(min_ms: int = 500, max_ms: int = 2000):
        """랜덤 지연"""
        delay = random.uniform(min_ms, max_ms) / 1000
        time.sleep(delay)
    
    @staticmethod
    async def human_like_typing(page: Page, selector: str, text: str):
        """사람처럼 타이핑"""
        element = await page.query_selector(selector)
        if element:
            await element.click()
            for char in text:
                await element.type(char)
                await asyncio.sleep(random.uniform(0.05, 0.15))
    
    @staticmethod
    async def random_scroll(page: Page):
        """랜덤 스크롤"""
        scroll_amount = random.randint(100, 500)
        await page.evaluate(f"window.scrollBy(0, {scroll_amount})")
        await asyncio.sleep(random.uniform(0.3, 0.8))
    
    @staticmethod
    async def move_mouse_naturally(page: Page):
        """자연스러운 마우스 이동"""
        width = await page.evaluate("window.innerWidth")
        height = await page.evaluate("window.innerHeight")
        x = random.randint(0, int(width))
        y = random.randint(0, int(height))
        await page.mouse.move(x, y)


# ==================== 베이스 크롤러 ====================

class BasePlatformCrawler:
    """모든 플랫폼 크롤러의 기본 클래스"""
    
    def __init__(self, headless: bool = True):
        self.headless = headless
        self.browser: Optional[Browser] = None
        self.context: Optional[BrowserContext] = None
        self.retry_count = 3
        self.retry_delay = 5
    
    async def init_browser(self):
        """브라우저 초기화 (Stealth 모드)"""
        playwright = await async_playwright().start()
        self.browser = await playwright.chromium.launch(
            headless=self.headless,
            args=[
                '--disable-blink-features=AutomationControlled',
                '--disable-dev-shm-usage',
                '--no-sandbox'
            ]
        )
        
        # Stealth context 생성
        self.context = await self.browser.new_context(
            user_agent=random.choice(AntiBot.USER_AGENTS),
            viewport={'width': 1920, 'height': 1080},
            locale='ko-KR',
            timezone_id='Asia/Seoul'
        )
        
        # JavaScript로 webdriver 흔적 제거
        await self.context.add_init_script("""
            Object.defineProperty(navigator, 'webdriver', {
                get: () => false
            });
            Object.defineProperty(navigator, 'plugins', {
                get: () => [1, 2, 3, 4, 5]
            });
            Object.defineProperty(navigator, 'languages', {
                get: () => ['ko-KR', 'ko', 'en-US', 'en']
            });
        """)
    
    async def close_browser(self):
        """브라우저 종료"""
        if self.context:
            await self.context.close()
        if self.browser:
            await self.browser.close()
    
    async def retry_on_failure(self, func, *args, **kwargs):
        """실패 시 재시도 로직"""
        for attempt in range(self.retry_count):
            try:
                return await func(*args, **kwargs)
            except Exception as e:
                if attempt == self.retry_count - 1:
                    raise
                print(f"Attempt {attempt + 1} failed: {e}. Retrying in {self.retry_delay}s...")
                await asyncio.sleep(self.retry_delay)
