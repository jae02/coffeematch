"""
크리마 지수 계산 엔진
가중치: 카카오 50% + 네이버 30% + 볼륨 20%
"""
from typing import Dict, Optional
import mysql.connector

class CremaCalculator:
    """
    크리마 지수 계산
    
    공식: (카카오 점수 * 50% + 네이버 점수 * 30% + 볼륨 점수 * 20%) * 20
    결과: 0-100 점수
    """
    
    # 가중치
    KAKAO_WEIGHT = 0.5
    NAVER_WEIGHT = 0.3
    VOLUME_WEIGHT = 0.2
    
    @staticmethod
    def normalize_rating(rating: Optional[float], max_rating: float = 5.0) -> float:
        """
        별점을 0-5 범위로 정규화
        
        Args:
            rating: 원본 별점
            max_rating: 최대 별점 (기본 5.0)
            
        Returns:
            정규화된 별점 (0.0 ~ 5.0)
        """
        if rating is None:
            return 0.0
        
        normalized = min(max_rating, max(0.0, float(rating)))
        return normalized
    
    @staticmethod
    def calculate_volume_score(kakao_reviews: int, naver_reviews: int) -> int:
        """
        리뷰 볼륨 점수 계산 (0-100)
        
        로직:
        - 총 리뷰 수에 따라 구간별 점수 부여
        - 200개 이상 = 100점
        - 100-199 = 80점
        - 50-99 = 60점
        - 20-49 = 40점
        - 10-19 = 20점
        - 10 미만 = 10점
        
        Args:
            kakao_reviews: 카카오 리뷰 수
            naver_reviews: 네이버 리뷰 수
            
        Returns:
            볼륨 점수 (0-100)
        """
        total_reviews = (kakao_reviews or 0) + (naver_reviews or 0)
        
        if total_reviews >= 200:
            return 100
        elif total_reviews >= 100:
            return 80
        elif total_reviews >= 50:
            return 60
        elif total_reviews >= 20:
            return 40
        elif total_reviews >= 10:
            return 20
        else:
            return 10
    
    @classmethod
    def calculate_crema_score(cls,
                             kakao_rating: Optional[float] = None,
                             naver_rating: Optional[float] = None,
                             kakao_reviews: int = 0,
                             naver_reviews: int = 0) -> Dict:
        """
        최종 크리마 지수 계산
        
        Args:
            kakao_rating: 카카오 별점 (0-5)
            naver_rating: 네이버 별점 (0-5)
            kakao_reviews: 카카오 리뷰 수
            naver_reviews: 네이버 리뷰 수
            
        Returns:
            {
                'kakao_score': float,
                'naver_score': float,
                'volume_score': int,
                'crema_score': int
            }
        """
        # 별점 정규화
        kakao_norm = cls.normalize_rating(kakao_rating)
        naver_norm = cls.normalize_rating(naver_rating)
        
        # 볼륨 점수 계산
        volume_score = cls.calculate_volume_score(kakao_reviews, naver_reviews)
        
        # 가중 평균 계산
        # 별점은 0-5 범위, 볼륨은 0-100 범위이므로 볼륨을 0-5로 변환
        volume_normalized = volume_score / 100 * 5
        
        weighted_score = (
            kakao_norm * cls.KAKAO_WEIGHT +
            naver_norm * cls.NAVER_WEIGHT +
            volume_normalized * cls.VOLUME_WEIGHT
        )
        
        # 0-100 스케일로 변환
        crema_score = int(weighted_score * 20)
        
        # 0-100 범위 보장
        crema_score = max(0, min(100, crema_score))
        
        return {
            'kakao_score': round(kakao_norm, 1),
            'naver_score': round(naver_norm, 1),
            'volume_score': volume_score,
            'crema_score': crema_score
        }
    
    @classmethod
    def save_score(cls, db_config: Dict, master_id: int, score_data: Dict):
        """
        크리마 지수를 DB에 저장
        
        Args:
            db_config: DB 설정
            master_id: cafe_master.id
            score_data: calculate_crema_score() 결과
        """
        conn = mysql.connector.connect(**db_config)
        cursor = conn.cursor()
        
        query = """
            INSERT INTO crema_score
            (master_id, kakao_score, naver_score, review_volume_score, crema_score)
            VALUES (%s, %s, %s, %s, %s)
            ON DUPLICATE KEY UPDATE
                kakao_score = VALUES(kakao_score),
                naver_score = VALUES(naver_score),
                review_volume_score = VALUES(review_volume_score),
                crema_score = VALUES(crema_score),
                calculated_at = CURRENT_TIMESTAMP
        """
        
        values = (
            master_id,
            score_data['kakao_score'],
            score_data['naver_score'],
            score_data['volume_score'],
            score_data['crema_score']
        )
        
        cursor.execute(query, values)
        conn.commit()
        
        cursor.close()
        conn.close()
    
    @classmethod
    def get_score_distribution(cls, db_config: Dict) -> Dict:
        """
        크리마 지수 분포 통계
        
        Returns:
            {'우수': N, '양호': M, '보통': K, '미흡': L}
        """
        conn = mysql.connector.connect(**db_config)
        cursor = conn.cursor(dictionary=True)
        
        cursor.execute("""
            SELECT 
                CASE 
                    WHEN crema_score >= 80 THEN '우수 (80-100)'
                    WHEN crema_score >= 60 THEN '양호 (60-79)'
                    WHEN crema_score >= 40 THEN '보통 (40-59)'
                    ELSE '미흡 (0-39)'
                END as grade,
                COUNT(*) as count
            FROM crema_score
            GROUP BY grade
            ORDER BY MIN(crema_score) DESC
        """)
        
        results = cursor.fetchall()
        
        cursor.close()
        conn.close()
        
        return {row['grade']: row['count'] for row in results}
