import asyncio
import json
import os
from typing import List, Dict, Any
from dataclasses import asdict
from datetime import datetime
from common import BasePlatformCrawler, AntiBot, CafeData

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

async def main():
    print("=" * 50)
    print("Naver Crawler Started")
    print("=" * 50)
    
    # 1. 네이버 블로그 검색 예시
    blog_crawler = NaverBlogCrawler(headless=True)
    search_keyword = "성수동 카페"
    print(f"Searching blogs for: {search_keyword}")
    blog_posts = await blog_crawler.search_cafe_posts(search_keyword, limit=3)
    
    for post in blog_posts:
        print(f"[BLOG] {post['title']}")
        print(f"       {post['url']}")
    
    # 2. 네이버 맵 검색 예시 (선택적)
    # map_crawler = NaverMapCrawler(headless=True)
    # map_cafes = await map_crawler.discover_new_cafes("성수동", limit=3)
    # for cafe in map_cafes:
    #     print(f"[MAP] {cafe.name}")

    # 결과 저장
    result = {
        "blog_posts": blog_posts,
        # "map_cafes": [asdict(c) for c in map_cafes]
    }
    
    script_dir = os.path.dirname(os.path.abspath(__file__))
    output_path = os.path.join(script_dir, "naver_result.json")
    
    with open(output_path, 'w', encoding='utf-8') as f:
        json.dump(result, f, ensure_ascii=False, indent=2)
    
    print(f"\nSaved naver results to {output_path}")

if __name__ == "__main__":
    asyncio.run(main())
