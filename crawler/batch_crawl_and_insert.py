import asyncio
import json
import requests
from kakao_crawler import KakaoMapCrawler
from naver_crawler import NaverMapCrawler

async def main():
    print("=" * 70)
    print("BATCH CRAWLER - Multiple Regions & Platforms")
    print("=" * 70)
    
    # Configuration
    regions = ["성수동", "홍대", "강남역"]
    cafes_per_region = 10
    
    all_cafes = []
    
    # 1. Crawl from Kakao Map
    print("\n[KAKAO MAP] Starting crawl...")
    kakao_crawler = KakaoMapCrawler(headless=True)
    
    for region in regions:
        print(f"\n  Crawling {region}...")
        try:
            cafes = await kakao_crawler.discover_new_cafes(region, limit=cafes_per_region)
            all_cafes.extend(cafes)
            print(f"  ✅ Found {len(cafes)} cafes in {region}")
        except Exception as e:
            print(f"  ❌ Error crawling {region}: {e}")
    
    print(f"\n[TOTAL] Collected {len(all_cafes)} cafes")
    
    # 2. Send to backend in batch
    if not all_cafes:
        print("No cafes found. Exiting.")
        return
    
    api_url = "http://localhost:8080/api/admin/cafes/crawl/batch"
    
    # Convert CafeData objects to API payload
    payload = []
    for cafe in all_cafes:
        cafe_dict = {
            "name": cafe.name,
            "address": cafe.address,
            "phone": cafe.phone,
            "businessHours": cafe.business_hours,
            "category": cafe.category,
            "sourcePlatform": cafe.source_platform,
            "platformId": cafe.platform_id,
            "latitude": cafe.latitude,
            "longitude": cafe.longitude,
            "status": cafe.status,
            "rawData": cafe.raw_data
        }
        payload.append(cafe_dict)
    
    print(f"\n[BACKEND] Sending {len(payload)} cafes to {api_url}...")
    
    try:
        response = requests.post(api_url, json=payload, timeout=60)
        
        if response.status_code == 200:
            results = response.json()
            print(f"✅ Successfully processed {len(results)} cafes!")
            
            # Save results to file
            with open('batch_crawl_result.json', 'w', encoding='utf-8') as f:
                json.dump(results, f, ensure_ascii=False, indent=2)
            
            print(f"\nResults saved to batch_crawl_result.json")
        else:
            print(f"❌ Failed. Status: {response.status_code}")
            print(f"Response: {response.text[:500]}")
            
    except Exception as e:
        print(f"❌ Error sending batch request: {e}")
    
    # 3. Summary
    print("\n" + "=" * 70)
    print("BATCH CRAWL COMPLETE")
    print("=" * 70)
    print(f"Total cafes crawled: {len(all_cafes)}")
    print(f"Regions: {', '.join(regions)}")
    print("=" * 70)

if __name__ == "__main__":
    asyncio.run(main())
