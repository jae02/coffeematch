"""
서울열린데이터광장(data.seoul.go.kr) API 클라이언트
"""
import requests
import pandas as pd
from typing import Optional, Dict, List
import time
from pyproj import Transformer

class SeoulOpenAPIClient:
    """
    서울열린데이터광장 REST API 클라이언트
    대상 서비스: LOCALDATA_072404 (식품위생업소 - 휴게음식점)
    """
    
    def __init__(self, api_key: str):
        self.api_key = api_key
        self.base_url = "http://openapi.seoul.go.kr:8088"
        self.service_name = "LOCALDATA_072404"
        
        # 좌표 변환기 (TM 중부원점 -> WGS84)
        # 서울시 데이터는 주로 EPSG:5174 (Bessel 1841 중부원점) 사용
        self.transformer = Transformer.from_crs("EPSG:5174", "EPSG:4326")

    def fetch_data(self, start_index: int, end_index: int) -> pd.DataFrame:
        """
        데이터 조회 (최대 1000건)
        """
        url = f"{self.base_url}/{self.api_key}/json/{self.service_name}/{start_index}/{end_index}/"
        
        try:
            response = requests.get(url, timeout=10)
            response.raise_for_status()
            data = response.json()
            
            # 응답 구조 확인
            if self.service_name not in data:
                # 에러 응답인 경우
                if 'RESULT' in data:
                     print(f"⚠️ API Error: {data['RESULT']}")
                return pd.DataFrame()
                
            rows = data[self.service_name]['row']
            if not rows:
                return pd.DataFrame()
                
            df = pd.DataFrame(rows)
            return self._transform_dataframe(df)
            
        except Exception as e:
            print(f"❌ API Request Failed ({start_index}~{end_index}): {e}")
            return pd.DataFrame()

    def _transform_dataframe(self, df: pd.DataFrame) -> pd.DataFrame:
        """
        데이터 전처리 및 스키마 매핑
        """
        # 영업중인 곳만 필터링 (상세영업상태코드: 01=영업, 02=폐업)
        # 또는 전체를 가져와서 DB에서 처리? 일단 여기선 필터링 없이 다 가져가되 status 매핑
        
        # 컬럼 매핑
        mapping = {
            'MGTNO': 'business_number',
            'BPLCNM': 'business_name',
            'RDNWHLADDR': 'road_address',
            'SITEWHLADDR': 'jibun_address',
            'UPTAE_NM': 'industry_name',
            'APVPERMYMD': 'opened_at',
            'TRDSTATENM': 'status_name',
            'DTLSTATENM': 'detail_status_name',
            'X': 'tm_x',
            'Y': 'tm_y'
        }
        
        df = df.rename(columns=mapping)
        
        # 필요한 컬럼만 선택 (존재하는 것만)
        cols = [c for c in mapping.values() if c in df.columns]
        df = df[cols]
        
        # 좌표 변환
        if 'tm_x' in df.columns and 'tm_y' in df.columns:
            df['latitude'], df['longitude'] = zip(*df.apply(self._convert_coords, axis=1))
            
        # 상태 매핑 (Active/Closed)
        # 영업상태코드(TRD_STATE_GBN): 01:영업/정상, 03:폐업
        # 상세영업상태코드(DTL_STATE_GBN): 01:영업, 02:폐업
        # 여기선 한글명으로 매핑하거나, 코드가 있다면 코드 사용. 
        # API 응답에 코드가 있는지 확인 필요. 보통 TRD_STATE_GBN 줌.
        # 일단 영업상태명으로 처리
        df['status'] = df['detail_status_name'].apply(
            lambda x: 'ACTIVE' if x == '영업' else 'CLOSED'
        )
        
        return df

    def _convert_coords(self, row):
        """좌표 변환 Apply 함수"""
        x = row.get('tm_x')
        y = row.get('tm_y')
        
        if pd.isna(x) or pd.isna(y) or x == '' or y == '':
            return None, None
            
        try:
            # X, Y가 바뀌어 있을 수도 있고, 단위가 다를 수도 있음.
            # 중부원점 TM은 보통 X(North), Y(East) 순서인데 pyproj는 (x, y) input
            lat, lon = self.transformer.transform(float(y), float(x))
            return lat, lon
        except:
            return None, None

    def fetch_all(self, max_count: int = 5000) -> pd.DataFrame:
        """
        데이터 일괄 수집
        """
        all_data = []
        batch_size = 1000
        
        print(f"📥 서울시 휴게음식점 데이터 수집 시작 (최대 {max_count}건)")
        
        for start in range(1, max_count, batch_size):
            end = start + batch_size - 1
            if end > max_count:
                end = max_count
                
            print(f"  Fetching {start} ~ {end}...")
            df = self.fetch_data(start, end)
            
            if df.empty:
                print("  No more data.")
                break
                
            all_data.append(df)
            time.sleep(0.2) # Rate limit
            
        if not all_data:
            return pd.DataFrame()
            
        result = pd.concat(all_data, ignore_index=True)
        print(f"✅ 총 {len(result)}건 수집 완료")
        return result
