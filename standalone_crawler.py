
import requests
from bs4 import BeautifulSoup
import csv
import time
import urllib.parse
import os

def crawl_naver_blog(keyword, limit=5):
    encoded_query = urllib.parse.quote(keyword + " 카페")
    url = f"https://search.naver.com/search.naver?where=blog&query={encoded_query}"
    
    headers = {
        "User-Agent": "Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.0 Mobile/15E148 Safari/604.1"
    }

    print(f"Crawling URL: {url}")
    response = requests.get(url, headers=headers)
    soup = BeautifulSoup(response.text, 'html.parser')
    
    results = []
    # Selectors based on NaverBlogCrawler.java
    posts = soup.select("li.bx, .view_wrap, .detail_box")
    
    print(f"Found {len(posts)} posts")

    for post in posts:
        if len(results) >= limit:
            break
            
        try:
            # Title
            title_el = post.select_one(".title_link, .total_tit, .tit, .api_txt_lines")
            if not title_el:
                continue
            title = title_el.get_text(strip=True)
            link = title_el.get("href")
            
            # Content
            snippet_el = post.select_one(".dsc_link, .dsc_txt, .api_txt_lines.dsc_txt, .total_dsc")
            snippet = snippet_el.get_text(strip=True) if snippet_el else ""
            
            print(f"Found: {title}")
            results.append([title, link, snippet])
            
        except Exception as e:
            print(f"Error parsing post: {e}")

    return results

def save_csv(data, filename):
    with open(filename, 'w', newline='', encoding='utf-8-sig') as f:
        writer = csv.writer(f)
        writer.writerow(["Title", "Link", "Content"])
        writer.writerows(data)
    print(f"Saved to {filename}")

if __name__ == "__main__":
    data = crawl_naver_blog("홍대")
    # Save to the same directory
    script_dir = os.path.dirname(os.path.abspath(__file__))
    output_path = os.path.join(script_dir, "crawled_sample.csv")
    save_csv(data, output_path)
