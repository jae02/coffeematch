import asyncio
import json
import os
from typing import List, Optional
from dataclasses import asdict
from common import BasePlatformCrawler, AntiBot, CafeData, ReviewData

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

async def main():
    print("=" * 50)
    print("Kakao Map Crawler Started")
    print("=" * 50)
    
    crawler = KakaoMapCrawler(headless=True)
    # 예시: 성수동 카페 검색
    cafes = await crawler.discover_new_cafes("성수동", limit=5)
    
    all_reviews = []
    
    for cafe in cafes:
        print(f"[NEW] {cafe.name} - {cafe.address} (ID: {cafe.platform_id})")
        # 리뷰 수집 (첫 번째 카페만 예시로 수행하거나 전체 수행 가능)
        # 여기서는 예시로 첫 번째만
        if cafe == cafes[0]:
             print(f"  Fetching reviews for {cafe.name}...")
             reviews = await crawler.crawl_reviews(cafe.platform_id)
             all_reviews.extend(reviews)
             for review in reviews[:3]:
                 print(f"    [REVIEW] {review.reviewer_nickname}: {review.content[:30]}...")

    # 결과 저장
    result = {
        "cafes": [asdict(cafe) for cafe in cafes],
        "reviews": [asdict(review) for review in all_reviews]
    }
    
    script_dir = os.path.dirname(os.path.abspath(__file__))
    output_path = os.path.join(script_dir, "kakao_result.json")
    
    with open(output_path, 'w', encoding='utf-8') as f:
        json.dump(result, f, ensure_ascii=False, indent=2)
    
    print(f"\nSaved kakao results to {output_path}")

if __name__ == "__main__":
    asyncio.run(main())
