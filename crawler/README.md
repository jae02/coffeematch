# Multi-Platform Cafe Crawler

Python 기반 Playwright 크롤러 프로토타입

## 설치

```bash
cd crawler
pip3 install -r requirements.txt
playwright install chromium
```

## 실행

```bash
python3 playwright_crawler_prototype.py
```

## 기능

1. **신규 카페 탐색**: 지역별 키워드 검색으로 새로운 카페 발견
2. **증분 리뷰 수집**: 최신 리뷰 날짜 기준으로 새 리뷰만 수집
3. **Anti-Bot 우회**: Stealth 모드, 랜덤 User-Agent, 자연스러운 마우스/타이핑
4. **다중 플랫폼**: 카카오맵, 네이버맵, 네이버 블로그 지원

## 출력

크롤링 결과는 `sample_crawl_result.json`에 저장됩니다.
