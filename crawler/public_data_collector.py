"""
서울시 공공데이터 수집기
- 서울시 열린데이터광장에서 식품위생업소 현황 데이터 수집
- 카페/베이커리/제과점 업태만 필터링
- CoffeeMatch 백엔드 API로 전송
"""

import requests
import json
import time
import argparse
from typing import List, Dict, Any

# 서울시 열린데이터광장 API 설정
SEOUL_OPEN_DATA_URL = "http://openapi.seoul.go.kr:8088"

# 백엔드 API 설정
BACKEND_API_URL = "http://localhost:8080/api/admin/cafes/crawl/batch"

# 필터링할 업태 목록 (휴게음식점 + 일반음식점)
CAFE_BUSINESS_TYPES = [
    # 휴게음식점 (필수)
    "커피숍",       # 가장 많음
    "다방",         # 오래된 카페, 개인 카페
    # 베이커리류
    "과자점",       # 디저트/베이커리 카페
    "제과점영업",   # 제과점
    # 특수 카페
    "전통찻집",     # 전통 카페
    "키즈카페",     # 키즈카페
    # 일반음식점에서 확인 필요
    "라이브카페",   # 라이브 카페
]

def fetch_seoul_food_establishments(api_key: str, start_index: int = 1, end_index: int = 1000) -> Dict[str, Any]:
    """
    서울시 식품위생업소 현황 데이터 조회
    API: 서울시 열린데이터광장 휴게음식점 인허가 정보 (카페/제과점 포함)
    """
    # 휴게음식점 인허가 정보 API (카페, 제과점 등 포함)
    url = f"{SEOUL_OPEN_DATA_URL}/{api_key}/json/LOCALDATA_072405/{start_index}/{end_index}/"
    
    try:
        response = requests.get(url, timeout=30)
        response.raise_for_status()
        return response.json()
    except requests.exceptions.RequestException as e:
        print(f"❌ API 요청 실패: {e}")
        return {}

def filter_cafes_and_bakeries(data: Dict[str, Any]) -> List[Dict[str, Any]]:
    """
    카페/베이커리 업태만 필터링
    """
    filtered = []
    
    result = data.get("LOCALDATA_072405", {})
    rows = result.get("row", [])
    
    if not rows:
        print("⚠️ 데이터가 없습니다.")
        return filtered
    
    for row in rows:
        # 영업상태 확인 (01: 영업중)
        status = row.get("TRDSTATEGBN", "")
        if status != "01":
            continue
        
        # 업태 확인
        business_type = row.get("UPTAENM", "")
        
        # 카페/베이커리 관련 업태인지 확인
        is_target = False
        for target_type in CAFE_BUSINESS_TYPES:
            if target_type in business_type:
                is_target = True
                break
        
        if not is_target:
            continue
        
        # 서울시 주소인지 확인
        address = row.get("RDNWHLADDR", "") or row.get("SITEWHLADDR", "")
        if not address.startswith("서울"):
            continue
        
        # 데이터 변환
        cafe_data = {
            "name": row.get("BPLCNM", ""),  # 사업장명
            "address": address,
            "phone": row.get("SITETEL", ""),  # 전화번호
            "businessHours": None,  # 공공데이터에는 영업시간 없음
            "category": row.get("UPTAENM", ""),  # 업태
            "sourcePlatform": "PUBLIC_DATA",
            "platformId": f"public_{row.get('MGTNO', '')}",  # 관리번호를 ID로 사용
            "latitude": None,  # 공공데이터에는 좌표 없음 (추후 카카오 API로 보완 가능)
            "longitude": None,
            "status": "NEW",
            "businessType": business_type,  # 업태 저장
            "rawData": {
                "mgtNo": row.get("MGTNO", ""),  # 관리번호
                "apvPermYmd": row.get("APVPERMYMD", ""),  # 인허가일자
                "uptaeNm": row.get("UPTAENM", ""),  # 업태
                "sitePostNo": row.get("SITEPOSTNO", ""),  # 우편번호
            }
        }
        
        filtered.append(cafe_data)
    
    return filtered

def send_to_backend(cafes: List[Dict[str, Any]], dry_run: bool = False) -> int:
    """
    백엔드 API로 카페 데이터 전송
    """
    if not cafes:
        print("⚠️ 전송할 데이터가 없습니다.")
        return 0
    
    if dry_run:
        print(f"🔍 [DRY RUN] {len(cafes)}개 카페 데이터가 전송될 예정입니다.")
        for cafe in cafes[:5]:
            print(f"  - {cafe['name']} ({cafe['businessType']}) - {cafe['address'][:30]}...")
        if len(cafes) > 5:
            print(f"  ... 외 {len(cafes) - 5}개")
        return len(cafes)
    
    print(f"📤 {len(cafes)}개 카페 데이터를 백엔드로 전송 중...")
    
    try:
        response = requests.post(
            BACKEND_API_URL,
            json=cafes,
            headers={"Content-Type": "application/json"},
            timeout=120
        )
        
        if response.status_code == 200:
            result = response.json()
            print(f"✅ {len(result)}개 카페가 성공적으로 저장되었습니다!")
            return len(result)
        else:
            print(f"❌ 전송 실패. Status: {response.status_code}")
            print(f"   Response: {response.text[:200]}")
            return 0
            
    except requests.exceptions.RequestException as e:
        print(f"❌ 전송 오류: {e}")
        return 0

def main():
    parser = argparse.ArgumentParser(description="서울시 공공데이터에서 카페/베이커리 정보 수집")
    parser.add_argument("--api-key", "-k", required=True, help="서울시 열린데이터광장 API 키")
    parser.add_argument("--test", action="store_true", help="테스트 모드 (데이터 100개만 조회, DB 저장 안함)")
    parser.add_argument("--dry-run", action="store_true", help="드라이런 모드 (DB 저장 안하고 미리보기만)")
    parser.add_argument("--batch-size", type=int, default=1000, help="API 호출당 조회할 데이터 수 (기본: 1000)")
    parser.add_argument("--max-pages", type=int, default=50, help="최대 페이지 수 (기본: 50, 약 50,000건)")
    parser.add_argument("--output", "-o", help="결과를 JSON 파일로 저장")
    
    args = parser.parse_args()
    
    print("=" * 60)
    print("🏪 서울시 공공데이터 카페/베이커리 수집기")
    print("=" * 60)
    
    all_cafes = []
    batch_size = 100 if args.test else args.batch_size
    max_pages = 1 if args.test else args.max_pages
    
    for page in range(max_pages):
        start_idx = page * batch_size + 1
        end_idx = start_idx + batch_size - 1
        
        print(f"\n📥 페이지 {page + 1}/{max_pages} 조회 중 ({start_idx} ~ {end_idx})...")
        
        data = fetch_seoul_food_establishments(args.api_key, start_idx, end_idx)
        
        if not data:
            print("⚠️ 더 이상 데이터가 없습니다.")
            break
        
        # 총 건수 확인
        result = data.get("LOCALDATA_072405", {})
        total_count = result.get("list_total_count", 0)
        
        if page == 0:
            print(f"📊 전체 데이터: {total_count:,}건")
        
        cafes = filter_cafes_and_bakeries(data)
        all_cafes.extend(cafes)
        
        print(f"   ✅ 카페/베이커리 {len(cafes)}개 필터링 완료 (누적: {len(all_cafes)}개)")
        
        # 모든 데이터를 조회했으면 중단
        if end_idx >= total_count:
            print(f"📌 모든 데이터 조회 완료!")
            break
        
        # API 요청 간 딜레이
        time.sleep(0.5)
    
    print(f"\n{'=' * 60}")
    print(f"📊 수집 결과: 총 {len(all_cafes)}개 카페/베이커리")
    print("=" * 60)
    
    # 업태별 통계
    type_stats = {}
    for cafe in all_cafes:
        bt = cafe.get("businessType", "기타")
        type_stats[bt] = type_stats.get(bt, 0) + 1
    
    print("\n📈 업태별 통계:")
    for bt, count in sorted(type_stats.items(), key=lambda x: -x[1]):
        print(f"   {bt}: {count}개")
    
    # JSON 파일로 저장
    if args.output:
        with open(args.output, "w", encoding="utf-8") as f:
            json.dump(all_cafes, f, ensure_ascii=False, indent=2)
        print(f"\n💾 결과가 {args.output}에 저장되었습니다.")
    
    # 백엔드로 전송
    if not args.test:
        print()
        success_count = send_to_backend(all_cafes, dry_run=args.dry_run)
        print(f"\n🎉 완료! 총 {success_count}개 카페가 처리되었습니다.")
    else:
        print("\n🔍 테스트 모드로 실행되었습니다. --test 옵션을 제거하면 전체 데이터를 수집합니다.")

if __name__ == "__main__":
    main()
