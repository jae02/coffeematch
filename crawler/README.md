# Creama 하이브리드 데이터 파이프라인

공공 데이터를 기반으로 카페 마스터 DB를 구축하고, 네이버/카카오 지도 데이터로 풍부화하여 **크리마 지수**를 산출하는 데이터 파이프라인입니다.

## 아키텍처

```
Phase 1: 공공 API → cafe_master (Ground Truth)
Phase 2: 좌표 매칭 → cafe_detail (지도 데이터)
Phase 3: 크리마 지수 계산 → crema_score
Phase 4: AI 리뷰 분석 → cafe_insights
```

## 핵심 기능

### 1. 좌표 기반 매칭 알고리즘
- **Haversine Formula**로 두 좌표 간 거리 계산 (미터 단위)
- 50m 반경 내 + 이름 유사도 0.6 이상이면 동일 카페로 판정
- 주소 텍스트 비교보다 정확도 ↑

### 2. 크리마 지수 계산
**가중치 공식:**
```
크리마 지수 = (카카오 50% + 네이버 30% + 리뷰볼륨 20%) × 20
```

**등급:**
- 80-100: 우수
- 60-79: 양호
- 40-59: 보통
- 0-39: 미흡

### 3. Upsert 로직
- 신규 카페: INSERT
- 기존 카페: UPDATE
- 폐업 카페: status = CLOSED

## 데이터베이스 스키마

### cafe_master (공공 데이터 마스터)
```sql
- id, business_number (인허가번호)
- business_name, address
- latitude, longitude (좌표 - 매칭 핵심!)
- status (ACTIVE/CLOSED/UNKNOWN)
```

### cafe_detail (지도 데이터 풍부화)
```sql
- master_id (FK to cafe_master)
- platform (KAKAO/NAVER)
- place_id, rating, review_count
```

### crema_score (크리마 지수)
```sql
- master_id (FK to cafe_master)
- kakao_score, naver_score, volume_score
- crema_score (최종 점수 0-100)
```

### cafe_insights (AI 분석 - Phase 4)
```sql
- master_id (FK to cafe_master)
- summary (한 줄 요약)
- keywords (JSON 배열)
- sentiment_score
```

## 설치 및 설정

### 1. 의존성 설치
```bash
cd crawler
pip install -r requirements.txt
```

### 2. 환경 변수 설정
```bash
cp .env.example .env
# .env 파일 편집하여 API 키 입력
```

필요한 API 키:
- **소상공인시장진흥공단 API**: https://www.data.go.kr/data/15083033/fileData.do
- **카카오 REST API**: https://developers.kakao.com/
- **Gemini API** (Phase 4용): https://ai.google.dev/

### 3. 데이터베이스 스키마 생성
```bash
# MySQL에 접속
mysql -h localhost -P 3307 -u root -p coffeematch

# 스키마 실행
SOURCE ../backend/src/main/resources/schema_hybrid.sql;
```

## 사용법

### 전체 파이프라인 실행
```bash
python main_pipeline.py --all --max-pages 10
```

### Phase별 개별 실행

**Phase 1: 공공 데이터 수집**
```bash
python main_pipeline.py --phase 1 --max-pages 10
```

**Phase 2: 좌표 매칭 (100개만 테스트)**
```bash
python main_pipeline.py --phase 2 --match-limit 100
```

**Phase 3: 크리마 지수 계산**
```bash
python main_pipeline.py --phase 3
```

## 디렉토리 구조

```
crawler/
├── phase1_master_data/
│   ├── public_api_client.py      # 공공 API 클라이언트
│   └── master_importer.py         # cafe_master 저장
│
├── phase2_enrichment/
│   ├── coordinate_matcher.py      # 좌표 기반 매칭
│   ├── kakao_enricher.py          # 카카오맵 API
│   └── naver_enricher.py          # 네이버맵 크롤링
│
├── phase3_scoring/
│   └── crema_calculator.py        # 크리마 지수 계산
│
├── phase4_ai_analysis/
│   ├── gemini_analyzer.py         # Gemini AI 분석
│   └── insights_saver.py          # 저장
│
├── main_pipeline.py               # 통합 파이프라인
├── config.py                      # 설정
└── requirements.txt               # 의존성
```

## 예상 결과

### 매칭 정확도
- 좌표 기반 매칭: **70-80%** (주소 매칭보다 높음)
- 실패 원인: 공공 데이터 좌표 부정확, 플랫폼 미등록

### 처리 속도
- Phase 1: ~1000개/분 (API 속도 의존)
- Phase 2: ~10개/초 (카카오 API Rate Limit)
- Phase 3: ~1000개/초 (로컬 계산)

## 주의사항

### API 사용량 제한
- **카카오맵 API**: 일일 300,000회 제한
- **공공 API**: 요청 간 0.5초 딜레이 권장

### 좌표 정확도
- 공공 데이터의 좌표가 부정확한 경우 (~10%) 매칭 실패 가능
- 실패 시 주소 기반 보조 로직 사용 (추후 구현)

### 크롤링 주의
- 네이버맵 크롤링 시 IP 차단 위험
- VPN 또는 프록시 로테이션 권장

## 라이선스
MIT
