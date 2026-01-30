"""
카카오맵 API를 통한 데이터 풍부화
"""
import requests
from typing import Dict, List, Optional
import time

class KakaoEnricher:
    """
    카카오맵 API로 카페 검색 및 데이터 수집
    """
    
    def __init__(self, api_key: str):
        self.api_key = api_key
        self.headers = {'Authorization': f'KakaoAK {api_key}'}
        self.search_url = "https://dapi.kakao.com/v2/local/search/keyword.json"
    
    def search_nearby(self, 
                     latitude: float, 
                     longitude: float, 
                     query: str,
                     radius: int = 100) -> List[Dict]:
        """
        좌표 기준 반경 검색
        
        Args:
            latitude: 위도
            longitude: 경도
            query: 검색어 (카페 이름)
            radius: 반경 (미터, 최대 20000)
            
        Returns:
            검색 결과 리스트
        """
        params = {
            'query': query,
            'x': longitude,
            'y': latitude,
            'radius': min(radius, 20000),
            'size': 5,  # 최대 5개
            'sort': 'distance'  # 거리순 정렬
        }
        
        try:
            response = requests.get(
                self.search_url, 
                headers=self.headers, 
                params=params,
                timeout=10
            )
            
            if response.status_code != 200:
                print(f"⚠️ 카카오 API 오류: {response.status_code}")
                return []
            
            data = response.json()
            documents = data.get('documents', [])
            
            return self._parse_results(documents)
            
        except Exception as e:
            print(f"❌ 카카오 API 요청 실패: {e}")
            return []
    
    def _parse_results(self, documents: List[Dict]) -> List[Dict]:
        """
        API 응답 파싱
        """
        results = []
        
        for doc in documents:
            result = {
                'place_id': doc.get('id'),
                'name': doc.get('place_name'),
                'latitude': float(doc.get('y', 0)),
                'longitude': float(doc.get('x', 0)),
                'phone': doc.get('phone', ''),
                'address': doc.get('address_name', ''),
                'road_address': doc.get('road_address_name', ''),
                'place_url': doc.get('place_url', ''),
                'category': doc.get('category_name', ''),
                'platform': 'KAKAO'
            }
            results.append(result)
        
        return results
    
    def enrich_cafe(self, master_cafe: Dict) -> Optional[Dict]:
        """
        cafe_master 데이터를 카카오맵으로 풍부화
        
        Args:
            master_cafe: cafe_master 레코드
                - latitude, longitude, business_name
                
        Returns:
            cafe_detail 용 데이터 또는 None
        """
        # 카페 이름으로 주변 검색
        results = self.search_nearby(
            master_cafe['latitude'],
            master_cafe['longitude'],
            master_cafe['business_name'],
            radius=100
        )
        
        if not results:
            return None
        
        # 가장 가까운 첫 번째 결과 반환
        # (coordinate_matcher에서 이미 필터링됨)
        return results[0]
    
    def get_place_details(self, place_id: str) -> Optional[Dict]:
        """
        장소 ID로 상세 정보 조회
        
        Note: 카카오맵 API는 별점/리뷰수를 제공하지 않음
              크롤링이 필요한 경우 별도 구현 필요
        """
        # 카카오 API는 별점/리뷰 미제공
        # 필요시 Playwright로 크롤링 구현
        pass
