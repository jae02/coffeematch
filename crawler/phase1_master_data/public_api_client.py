"""
소상공인시장진흥공단 상가(상권)정보 API 클라이언트
"""
import requests
import pandas as pd
from typing import List, Dict, Optional
import time

class PublicAPIClient:
    """
    소상공인시장진흥공단 상가(상권)정보 API 클라이언트
    
    API 문서: https://www.data.go.kr/data/15083033/fileData.do
    업종코드: Q01 (커피전문점/카페/다방)
    """
    
    def __init__(self, api_key: str, base_url: str):
        self.api_key = api_key
        self.base_url = base_url
    
    def fetch_coffee_shops(self, page_no: int = 1, rows: int = 1000) -> pd.DataFrame:
        """
        커피전문점/카페 데이터 조회
        
        Args:
            page_no: 페이지 번호
            rows: 페이지당 rows 수
            
        Returns:
            DataFrame with cafe data
        """
        params = {
            'serviceKey': self.api_key,
            'pageNo': page_no,
            'numOfRows': rows,
            'type': 'json'
        }
        
        try:
            response = requests.get(self.base_url, params=params, timeout=30)
            response.raise_for_status()
            
            data = response.json()
            
            # API 응답 구조 확인
            body = data.get('body', {})
            items = body.get('items', [])
            
            if not items:
                print(f"⚠️ 페이지 {page_no}: 데이터 없음")
                return pd.DataFrame()
            
            df = pd.DataFrame(items)
            
            # 필요한 컬럼 선택 및 변환
            df = self._transform_dataframe(df)
            
            print(f"✅ 페이지 {page_no}: {len(df)}개 카페 조회")
            
            return df
            
        except requests.exceptions.RequestException as e:
            print(f"❌ API 조회 실패 (페이지 {page_no}): {e}")
            return pd.DataFrame()
    
    def _transform_dataframe(self, df: pd.DataFrame) -> pd.DataFrame:
        """
        DataFrame 변환 및 검증
        """
        # 컬럼 매핑 (실제 API 응답에 맞게 조정 필요)
        column_mapping = {
            'bizesId': 'business_number',
            'bizesNm': 'business_name',
            'rdnmAdr': 'road_address',
            'lnmAdr': 'jibun_address',
            'lat': 'latitude',
            'lon': 'longitude',
            'upjongCd': 'industry_code',
            'upjongNm': 'industry_name',
            'opnYmd': 'opened_at'
        }
        
        # 컬럼 이름 변경
        available_cols = {k: v for k, v in column_mapping.items() if k in df.columns}
        df = df.rename(columns=available_cols)
        
        # 필수 컬럼 확인
        required_cols = ['business_number', 'business_name', 'latitude', 'longitude']
        
        for col in required_cols:
            if col not in df.columns:
                print(f"⚠️ 필수 컬럼 누락: {col}")
                return pd.DataFrame()
        
        # 좌표 데이터 검증 및 변환
        df = df[df['latitude'].notna() & df['longitude'].notna()]
        df['latitude'] = pd.to_numeric(df['latitude'], errors='coerce')
        df['longitude'] = pd.to_numeric(df['longitude'], errors='coerce')
        
        # 한국 좌표 범위 검증 (대략적)
        df = df[
            (df['latitude'] >= 33.0) & (df['latitude'] <= 39.0) &
            (df['longitude'] >= 124.0) & (df['longitude'] <= 132.0)
        ]
        
        return df
    
    def fetch_all_coffee_shops(self, max_pages: int = 100) -> pd.DataFrame:
        """
        전체 커피전문점 데이터 일괄 조회
        
        Args:
            max_pages: 최대 페이지 수
            
        Returns:
            전체 데이터 DataFrame
        """
        all_data = []
        
        print(f"📥 공공 API 데이터 수집 시작 (최대 {max_pages}페이지)")
        
        for page in range(1, max_pages + 1):
            df = self.fetch_coffee_shops(page_no=page)
            
            if df.empty:
                print(f"📌 페이지 {page}에서 데이터 없음. 수집 종료.")
                break
            
            all_data.append(df)
            
            # API 요청 간 딜레이
            time.sleep(0.5)
        
        if not all_data:
            print("❌ 수집된 데이터 없음")
            return pd.DataFrame()
        
        result = pd.concat(all_data, ignore_index=True)
        result = result.drop_duplicates(subset=['business_number'])
        
        print(f"\n✅ 총 {len(result)}개 카페 수집 완료")
        
        return result
