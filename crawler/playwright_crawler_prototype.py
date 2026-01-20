"""
Multi-Platform Cafe Crawler Prototype using Playwright
지원 플랫폼: 네이버 블로그, 카카오맵, 네이버맵
"""

import asyncio
import random
import time
from datetime import datetime
from typing import List, Dict, Any, Optional
from dataclasses import dataclass, field, asdict
from playwright.async_api import async_playwright, Browser, Page, BrowserContext
import json


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


# ==================== 카카오맵 크롤러 ====================

class KakaoMapCrawler(BasePlatformCrawler):
    """카카오맵 크롤러"""
    
    BASE_URL = "https://map.kakao.com"
    
    async def discover_new_cafes(self, region: str, keyword: str = "카페", limit: int = 10) -> List[CafeData]:
        """신규 카페 탐색"""
        await self.init_browser()
        page = await self.context.new_page()
        cafes = []
        
        try:
            await page.goto(self.BASE_URL)
            await asyncio.sleep(3)
            
            # 모달이나 dimmed layer가 있으면 제거
            try:
                await page.evaluate("""
                    const dimmed = document.getElementById('dimmedLayer');
                    if (dimmed) dimmed.style.display = 'none';
                """)
            except:
                pass
            
            # 검색어 입력 - 더 간단한 방법 사용
            search_query = f"{region} {keyword}"
            try:
                # 검색창에 직접 값 설정
                await page.fill('input#search\\.keyword\\.query', search_query)
                await asyncio.sleep(1)
                await page.click('button#search\\.keyword\\.submit')
                await asyncio.sleep(4)
            except Exception as e:
                print(f"Search failed: {e}")
                # 대체 방법 시도
                await page.evaluate(f"""
                    document.querySelector('input#search\\\\.keyword\\\\.query').value = '{search_query}';
                    document.querySelector('button#search\\\\.keyword\\\\.submit').click();
                """)
                await asyncio.sleep(4)
            
            # 검색 결과에서 카페 정보 추출
            cafe_items = await page.query_selector_all('#info\\.search\\.place\\.list > li.PlaceItem')
            
            for idx, item in enumerate(cafe_items[:limit]):
                try:
                    # 카페 정보 추출
                    name_elem = await item.query_selector('.head_item .tit_name')
                    name = await name_elem.inner_text() if name_elem else ""
                    
                    # 상세 페이지 링크에서 ID 추출
                    link_elem = await item.query_selector('.moreview')
                    detail_url = await link_elem.get_attribute('href') if link_elem else ""
                    platform_id = detail_url.split('/')[-1] if detail_url else f"kakao_{idx}"
                    
                    # 주소
                    addr_elem = await item.query_selector('.addr p')
                    address = await addr_elem.inner_text() if addr_elem else ""
                    
                    # 카테고리
                    category_elem = await item.query_selector('.subcategory')
                    category = await category_elem.inner_text() if category_elem else ""
                    
                    cafe = CafeData(
                        name=name,
                        address=address,
                        category=category,
                        source_platform="KAKAO_MAP",
                        platform_id=platform_id,
                        status="NEW",
                        raw_data={"detail_url": detail_url}
                    )
                    cafes.append(cafe)
                    
                    await AntiBot.random_scroll(page)
                    
                except Exception as e:
                    print(f"Error parsing cafe {idx}: {e}")
                    continue
        
        finally:
            await page.close()
            await self.close_browser()
        
        return cafes
    
    async def crawl_reviews(self, platform_id: str, since_date: Optional[str] = None) -> List[ReviewData]:
        """증분 리뷰 수집"""
        await self.init_browser()
        page = await self.context.new_page()
        reviews = []
        
        try:
            detail_url = f"{self.BASE_URL}/{platform_id}"
            await page.goto(detail_url)
            await asyncio.sleep(2)
            
            # 리뷰 탭 클릭 (존재하는 경우)
            review_tab = await page.query_selector('.link_evaluation')
            if review_tab:
                await review_tab.click()
                await asyncio.sleep(1)
            
            # 리뷰 항목 추출
            review_items = await page.query_selector_all('.list_evaluation > li')
            
            for idx, item in enumerate(review_items):
                try:
                    # 리뷰 날짜 확인 (증분 수집)
                    date_elem = await item.query_selector('.time_write')
                    review_date = await date_elem.inner_text() if date_elem else ""
                    
                    # since_date 이전이면 중단
                    if since_date and review_date < since_date:
                        break
                    
                    # 작성자
                    author_elem = await item.query_selector('.link_user')
                    author = await author_elem.inner_text() if author_elem else ""
                    
                    # 평점
                    rating_elem = await item.query_selector('.grade_star')
                    rating_text = await rating_elem.inner_text() if rating_elem else "0"
                    rating = int(rating_text.replace('점', '')) if rating_text else None
                    
                    # 내용
                    content_elem = await item.query_selector('.txt_comment')
                    content = await content_elem.inner_text() if content_elem else ""
                    
                    # 이미지
                    img_elem = await item.query_selector('.link_photo img')
                    image_url = await img_elem.get_attribute('src') if img_elem else ""
                    
                    review = ReviewData(
                        cafe_platform_id=platform_id,
                        reviewer_nickname=author,
                        rating=rating,
                        content=content,
                        review_date=review_date,
                        image_url=image_url,
                        source_platform="KAKAO_MAP",
                        platform_review_id=f"{platform_id}_review_{idx}"
                    )
                    reviews.append(review)
                    
                except Exception as e:
                    print(f"Error parsing review {idx}: {e}")
                    continue
        
        finally:
            await page.close()
            await self.close_browser()
        
        return reviews


# ==================== 네이버 맵 크롤러 ====================

class NaverMapCrawler(BasePlatformCrawler):
    """네이버 맵 크롤러"""
    
    BASE_URL = "https://map.naver.com"
    
    async def discover_new_cafes(self, region: str, keyword: str = "카페", limit: int = 10) -> List[CafeData]:
        """신규 카페 탐색"""
        await self.init_browser()
        page = await self.context.new_page()
        cafes = []
        
        try:
            await page.goto(self.BASE_URL)
            await asyncio.sleep(2)
            
            # 검색어 입력
            search_query = f"{region} {keyword}"
            await AntiBot.human_like_typing(page, '.input_search', search_query)
            await page.click('.btn_search')
            await asyncio.sleep(3)
            
            # iframe으로 전환 (필요한 경우)
            search_iframe = page.frame_locator('iframe#searchIframe')
            
            # 검색 결과에서 카페 정보 추출
            cafe_items = await search_iframe.locator('.place_bluelink').all()
            
            for idx, item in enumerate(cafe_items[:limit]):
                try:
                    name = await item.inner_text()
                    
                    # 상세 정보는 클릭 후 가져오기 (필요 시)
                    cafe = CafeData(
                        name=name,
                        source_platform="NAVER_MAP",
                        platform_id=f"naver_map_{idx}",
                        status="NEW"
                    )
                    cafes.append(cafe)
                    
                    await AntiBot.random_scroll(page)
                    
                except Exception as e:
                    print(f"Error parsing cafe {idx}: {e}")
                    continue
        
        finally:
            await page.close()
            await self.close_browser()
        
        return cafes


# ==================== 네이버 블로그 크롤러 ====================

class NaverBlogCrawler(BasePlatformCrawler):
    """네이버 블로그 크롤러"""
    
    BASE_URL = "https://search.naver.com/search.naver"
    
    async def search_cafe_posts(self, cafe_name: str, limit: int = 10) -> List[Dict[str, Any]]:
        """특정 카페 관련 블로그 포스트 검색"""
        await self.init_browser()
        page = await self.context.new_page()
        posts = []
        
        try:
            # 블로그 검색
            search_url = f"{self.BASE_URL}?where=blog&query={cafe_name}+카페"
            await page.goto(search_url)
            await asyncio.sleep(2)
            
            # 검색 결과 추출
            blog_items = await page.query_selector_all('.view_wrap .bx')
            
            for idx, item in enumerate(blog_items[:limit]):
                try:
                    # 제목
                    title_elem = await item.query_selector('.title_link')
                    title = await title_elem.inner_text() if title_elem else ""
                    blog_url = await title_elem.get_attribute('href') if title_elem else ""
                    
                    # 요약
                    desc_elem = await item.query_selector('.dsc_link')
                    description = await desc_elem.inner_text() if desc_elem else ""
                    
                    # 작성일
                    date_elem = await item.query_selector('.sub_time')
                    post_date = await date_elem.inner_text() if date_elem else ""
                    
                    post = {
                        "title": title,
                        "url": blog_url,
                        "description": description,
                        "post_date": post_date,
                        "source_platform": "NAVER_BLOG",
                        "crawled_at": datetime.now().isoformat()
                    }
                    posts.append(post)
                    
                except Exception as e:
                    print(f"Error parsing post {idx}: {e}")
                    continue
        
        finally:
            await page.close()
            await self.close_browser()
        
        return posts


# ==================== 메인 실행 ====================

async def main():
    """프로토타입 실행 예제"""
    
    # 1. 카카오맵에서 성수동 카페 탐색
    print("=" * 50)
    print("1. 카카오맵 신규 카페 탐색")
    print("=" * 50)
    kakao_crawler = KakaoMapCrawler(headless=True)
    kakao_cafes = await kakao_crawler.discover_new_cafes("성수동", limit=5)
    
    for cafe in kakao_cafes:
        print(f"[NEW] {cafe.name} - {cafe.address} (ID: {cafe.platform_id})")
    
    # 2. 첫 번째 카페의 리뷰 수집
    if kakao_cafes:
        print("\n" + "=" * 50)
        print("2. 카카오맵 리뷰 수집")
        print("=" * 50)
        reviews = await kakao_crawler.crawl_reviews(kakao_cafes[0].platform_id)
        for review in reviews[:3]:
            print(f"[REVIEW] {review.reviewer_nickname}: {review.content[:50]}...")
    
    # 3. 네이버 블로그 검색
    print("\n" + "=" * 50)
    print("3. 네이버 블로그 검색")
    print("=" * 50)
    blog_crawler = NaverBlogCrawler(headless=True)
    blog_posts = await blog_crawler.search_cafe_posts("성수동 카페", limit=3)
    
    for post in blog_posts:
        print(f"[BLOG] {post['title']}")
        print(f"       {post['url']}")
    
    # 4. 결과를 JSON으로 저장
    result = {
        "cafes": [asdict(cafe) for cafe in kakao_cafes],
        "reviews": [asdict(review) for review in reviews] if kakao_cafes else [],
        "blog_posts": blog_posts
    }
    
    with open('/Users/user/Desktop/coffee/crawler/sample_crawl_result.json', 'w', encoding='utf-8') as f:
        json.dump(result, f, ensure_ascii=False, indent=2)
    
    print("\n결과가 sample_crawl_result.json에 저장되었습니다.")


if __name__ == "__main__":
    asyncio.run(main())
