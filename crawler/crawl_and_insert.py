import asyncio
import json
import requests
from kakao_crawler import KakaoMapCrawler

async def main():
    print("=" * 50)
    print("Crawling 1 Cafe from Kakao Map")
    print("=" * 50)
    
    crawler = KakaoMapCrawler(headless=True)
    
    # 1. Crawl
    cafes = await crawler.discover_new_cafes("성수동", limit=1)
    
    if not cafes:
        print("No cafes found.")
        return

    cafe = cafes[0]
    print(f"Found: {cafe.name}")
    print(f"Address: {cafe.address}")
    
    # 2. Convert to DTO format
    # CafeRequestDto: name, address, phone, description, imageUrl
    payload = {
        "name": cafe.name,
        "address": cafe.address,
        "phone": cafe.phone,
        "description": f"Category: {cafe.category}", # Using category as description for now
        "imageUrl": "" # No image URL in list view usually, but we can leave empty
    }
    
    # 3. POST to Backend
    api_url = "http://localhost:8080/api/admin/cafes"
    print(f"Sending data to {api_url}...")
    
    try:
        response = requests.post(api_url, json=payload)
        
        if response.status_code == 200:
            print("Successfully inserted cafe!")
            with open('crawl_result.log', 'w') as f:
                f.write(f"Success: {response.json()}")
        else:
            print(f"Failed to insert cafe. Status: {response.status_code}")
            with open('crawl_result.log', 'w') as f:
                f.write(f"Failed: {response.status_code} - {response.text}")
            
    except Exception as e:
        print(f"Error sending request: {e}")
        with open('crawl_error.log', 'w') as f:
            f.write(f"Error: {e}")

    # Log success to file
    with open('crawl_status.txt', 'w') as f:
        f.write("Finished")

if __name__ == "__main__":
    asyncio.run(main())
