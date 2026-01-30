"""
좌표 기반 매칭 알고리즘
- Haversine formula로 두 좌표 간 거리 계산
- 이름 유사도 보조 매칭
"""
import math
from difflib import SequenceMatcher
from typing import Dict, Optional, Tuple
import re

class CoordinateMatcher:
    """
    공공 데이터와 지도 플랫폼 데이터를 좌표 기반으로 매칭
    """
    
    @staticmethod
    def haversine_distance(lat1: float, lon1: float, lat2: float, lon2: float) -> float:
        """
        두 좌표 간 거리 계산 (미터 단위)
        
        Haversine Formula:
        a = sin²(Δφ/2) + cos φ1 ⋅ cos φ2 ⋅ sin²(Δλ/2)
        c = 2 ⋅ atan2( √a, √(1−a) )
        d = R ⋅ c
        
        Args:
            lat1, lon1: 첫 번째 좌표
            lat2, lon2: 두 번째 좌표
            
        Returns:
            거리 (미터)
        """
        # 지구 반지름 (미터)
        R = 6371000
        
        # 라디안 변환
        phi1 = math.radians(lat1)
        phi2 = math.radians(lat2)
        delta_phi = math.radians(lat2 - lat1)
        delta_lambda = math.radians(lon2 - lon1)
        
        # Haversine formula
        a = (math.sin(delta_phi / 2) ** 2 +
             math.cos(phi1) * math.cos(phi2) * math.sin(delta_lambda / 2) ** 2)
        c = 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a))
        
        distance = R * c
        
        return distance
    
    @staticmethod
    def clean_name(name: str) -> str:
        """
        카페 이름 정규화
        
        - 공백 제거
        - 특수문자 제거
        - 소문자 변환
        - '점', '지점' 제거
        """
        if not name:
            return ""
        
        # 특수문자 제거
        cleaned = re.sub(r'[^\w\s가-힣]', '', name)
        
        # 공백 제거
        cleaned = cleaned.replace(' ', '').replace('\t', '')
        
        # 지점/점 표기 제거
        cleaned = cleaned.replace('점', '').replace('지점', '')
        
        # 소문자 변환 (영문)
        cleaned = cleaned.lower()
        
        return cleaned
    
    @classmethod
    def name_similarity(cls, name1: str, name2: str) -> float:
        """
        이름 유사도 계산 (0.0 ~ 1.0)
        
        Sequence Matcher를 사용하여 문자열 유사도 계산
        
        Args:
            name1, name2: 비교할 이름
            
        Returns:
            유사도 (0.0 = 완전 다름, 1.0 = 동일)
        """
        clean1 = cls.clean_name(name1)
        clean2 = cls.clean_name(name2)
        
        if not clean1 or not clean2:
            return 0.0
        
        # SequenceMatcher로 유사도 계산
        similarity = SequenceMatcher(None, clean1, clean2).ratio()
        
        return similarity
    
    @classmethod
    def is_match(cls, 
                 master_cafe: Dict, 
                 platform_cafe: Dict,
                 max_distance: float = 50.0,
                 min_similarity: float = 0.6) -> Tuple[bool, Dict]:
        """
        두 카페가 같은 곳인지 판별
        
        매칭 조건:
        1. 좌표 거리 <= max_distance (기본 50m)
        2. 이름 유사도 >= min_similarity (기본 0.6)
        
        Args:
            master_cafe: 공공 데이터 카페
                - latitude, longitude, business_name
            platform_cafe: 플랫폼 카페
                - latitude, longitude, name
            max_distance: 최대 허용 거리 (미터)
            min_similarity: 최소 이름 유사도
            
        Returns:
            (is_matched, match_info)
        """
        # 좌표 거리 계산
        distance = cls.haversine_distance(
            master_cafe['latitude'],
            master_cafe['longitude'],
            platform_cafe['latitude'],
            platform_cafe['longitude']
        )
        
        # 이름 유사도 계산
        similarity = cls.name_similarity(
            master_cafe.get('business_name', ''),
            platform_cafe.get('name', '')
        )
        
        # 매칭 여부 판별
        is_matched = (distance <= max_distance and similarity >= min_similarity)
        
        match_info = {
            'distance': round(distance, 2),
            'similarity': round(similarity, 3),
            'is_matched': is_matched
        }
        
        return is_matched, match_info
    
    @classmethod
    def find_best_match(cls,
                       master_cafe: Dict,
                       platform_cafes: list,
                       max_distance: float = 50.0,
                       min_similarity: float = 0.6) -> Optional[Tuple[Dict, Dict]]:
        """
        여러 플랫폼 후보 중 가장 잘 매칭되는 카페 찾기
        
        Args:
            master_cafe: 공공 데이터 카페
            platform_cafes: 플랫폼 후보 카페 리스트
            max_distance: 최대 허용 거리
            min_similarity: 최소 이름 유사도
            
        Returns:
            (best_match_cafe, match_info) or None
        """
        best_match = None
        best_score = 0.0
        best_info = None
        
        for cafe in platform_cafes:
            is_matched, info = cls.is_match(
                master_cafe, cafe, max_distance, min_similarity
            )
            
            if is_matched:
                # 거리와 유사도를 종합한 점수 (유사도에 더 큰 가중치)
                score = info['similarity'] * 0.7 + (1 - info['distance'] / max_distance) * 0.3
                
                if score > best_score:
                    best_score = score
                    best_match = cafe
                    best_info = info
        
        if best_match:
            return best_match, best_info
        
        return None
