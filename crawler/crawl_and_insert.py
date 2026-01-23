import asyncio
import json
import requests
from kakao_crawler import KakaoMapCrawler

async def main():
    print("=" * 50)
    print("Crawling Cafes from Kakao Map and Saving to DB")
    print("=" * 50)
    
    crawler = KakaoMapCrawler(headless=True)
    
    # 1. Crawl
    region = "성수동"
    limit = 5
    cafes = await crawler.discover_new_cafes(region, limit=limit)
    
    if not cafes:
        print("No cafes found.")
        return

    print(f"\nFound {len(cafes)} cafes. Sending to backend...")
    
    # 2. Send each cafe to backend
    api_url = "http://localhost:8080/api/admin/cafes/crawl"
    success_count = 0
    error_count = 0
    
    for cafe in cafes:
        # Convert CafeData to CrawlCafeRequestDto format
        payload = {
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
        
        print(f"\n[{success_count + error_count + 1}/{len(cafes)}] Processing: {cafe.name}")
        print(f"  Address: {cafe.address}")
        print(f"  Platform ID: {cafe.platform_id}")
        
        try:
            response = requests.post(api_url, json=payload, timeout=10)
            
            if response.status_code == 200:
                result = response.json()
                print(f"  ✅ Successfully saved! (DB ID: {result.get('id', 'unknown')})")
                success_count += 1
            else:
                print(f"  ❌ Failed. Status: {response.status_code}")
                print(f"  Response: {response.text[:200]}")
                error_count += 1
                
        except Exception as e:
            print(f"  ❌ Error sending request: {e}")
            error_count += 1

    # 3. Summary
    print("\n" + "=" * 50)
    print(f"SUMMARY:")
    print(f"  Total: {len(cafes)}")
    print(f"  Success: {success_count}")
    print(f"  Errors: {error_count}")
    print("=" * 50)
    
    # Log to file
    with open('crawl_result.log', 'w', encoding='utf-8') as f:
        f.write(f"Crawled {len(cafes)} cafes from {region}\n")
        f.write(f"Success: {success_count}, Errors: {error_count}\n")
        for cafe in cafes:
            f.write(f"- {cafe.name} ({cafe.platform_id})\n")

if __name__ == "__main__":
    asyncio.run(main())

