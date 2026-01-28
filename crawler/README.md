# CoffeeMatch Crawler

서울시 공공데이터에서 카페/베이커리 정보를 수집하는 도구입니다.

## 설치

```bash
pip install -r requirements.txt
```

## 사용법

### API 키 발급

1. [서울시 열린데이터광장](https://data.seoul.go.kr) 회원가입
2. API 인증키 발급 받기

### 실행

```bash
# 테스트 실행 (100개 데이터만 조회, DB 저장 안함)
python public_data_collector.py --api-key YOUR_API_KEY --test

# 드라이런 (전체 조회, DB 저장 안하고 미리보기만)
python public_data_collector.py --api-key YOUR_API_KEY --dry-run

# 전체 실행 (수집 + DB 저장)
python public_data_collector.py --api-key YOUR_API_KEY

# 결과를 JSON 파일로도 저장
python public_data_collector.py --api-key YOUR_API_KEY --output cafes.json
```

## 수집 대상

- 커피숍 / 카페 / 커피전문점
- 베이커리 / 제과점
- 영업중인 업소만 필터링
- 서울시 주소만 필터링
