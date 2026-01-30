"""
Creama 하이브리드 데이터 파이프라인 - 메인 오케스트레이터

Phase 1: 공공 API → cafe_master
Phase 2: 좌표 매칭 → cafe_detail
Phase 3: 크리마 지수 계산 → crema_score
Phase 4: AI 리뷰 분석 → cafe_insights
"""
import sys
import os

# 현재 디렉토리를 import path에 추가
sys.path.append(os.path.dirname(__file__))

from config import (
    PUBLIC_API_KEY, PUBLIC_API_URL, KAKAO_REST_API_KEY,
    DB_CONFIG, COORDINATE_MATCH_RADIUS, NAME_SIMILARITY_THRESHOLD
)

from phase1_master_data.public_api_client import PublicAPIClient
from phase1_master_data.master_importer import MasterImporter
from phase2_enrichment.coordinate_matcher import CoordinateMatcher
from phase2_enrichment.kakao_enricher import KakaoEnricher
from phase3_scoring.crema_calculator import CremaCalculator

import mysql.connector
import argparse


class HybridPipeline:
    """
    하이브리드 데이터 파이프라인 메인 클래스
    """
    
    def __init__(self):
        self.db_config = DB_CONFIG
        self.public_client = PublicAPIClient(PUBLIC_API_KEY, PUBLIC_API_URL)
        self.master_importer = MasterImporter(self.db_config)
        self.matcher = CoordinateMatcher()
        self.kakao_enricher = KakaoEnricher(KAKAO_REST_API_KEY)
        self.crema_calculator = CremaCalculator()
    
    def run_phase1(self, max_pages: int = 10):
        """
        Phase 1: 공공 API 데이터 수집 및 cafe_master 저장
        """
        print("=" * 60)
        print("Phase 1: 공공 데이터 수집")
        print("=" * 60)
        
        # 공공 API에서 데이터 수집
        df = self.public_client.fetch_all_coffee_shops(max_pages=max_pages)
        
        if df.empty:
            print("❌ 수집된 데이터가 없습니다")
            return
        
        # DB에 저장
        self.master_importer.connect()
        result = self.master_importer.upsert_cafes(df)
        
        # 통계 출력
        stats = self.master_importer.get_statistics()
        print(f"\n📊 cafe_master 통계:")
        for status, count in stats.items():
            print(f"   - {status}: {count}개")
        
        self.master_importer.close()
        
        print("\n✅ Phase 1 완료\n")
    
    def run_phase2(self, limit: int = None):
        """
        Phase 2: 좌표 기반 매칭 및 cafe_detail 저장
        """
        print("=" * 60)
        print("Phase 2: 데이터 매칭 및 풍부화")
        print("=" * 60)
        
        # cafe_master에서 활성 카페 가져오기
        conn = mysql.connector.connect(**self.db_config)
        cursor = conn.cursor(dictionary=True)
        
        query = """
            SELECT id, business_number, business_name, 
                   latitude, longitude, jibun_address
            FROM cafe_master
            WHERE status = 'ACTIVE'
            ORDER BY id
        """
        
        if limit:
            query += f" LIMIT {limit}"
        
        cursor.execute(query)
        cafes = cursor.fetchall()
        cursor.close()
        
        print(f"📍 {len(cafes)}개 카페 매칭 시작...\n")
        
        matched = 0
        failed = 0
        
        for cafe in cafes:
            try:
                # 카카오맵 검색
                platform_results = self.kakao_enricher.search_nearby(
                    cafe['latitude'],
                    cafe['longitude'],
                    cafe['business_name'],
                    radius=100
                )
                
                if not platform_results:
                    failed += 1
                    continue
                
                # 최적 매칭 찾기
                best_match = self.matcher.find_best_match(
                    cafe,
                    platform_results,
                    max_distance=COORDINATE_MATCH_RADIUS,
                    min_similarity=NAME_SIMILARITY_THRESHOLD
                )
                
                if not best_match:
                    failed += 1
                    continue
                
                match_cafe, match_info = best_match
                
                # cafe_detail에 저장
                self._save_cafe_detail(conn, cafe['id'], match_cafe)
                
                matched += 1
                
                if matched % 10 == 0:
                    print(f"  진행: {matched + failed}/{len(cafes)}")
                
            except Exception as e:
                print(f"⚠️ {cafe['business_name']} 매칭 실패: {e}")
                failed += 1
        
        conn.close()
        
        print(f"\n✅ Phase 2 완료:")
        print(f"   - 매칭 성공: {matched}개")
        print(f"   - 매칭 실패: {failed}개")
        print(f"   - 성공률: {matched / len(cafes) * 100:.1f}%\n")
    
    def _save_cafe_detail(self, conn, master_id: int, platform_data: Dict):
        """
        cafe_detail 테이블에 저장
        """
        cursor = conn.cursor()
        
        query = """
            INSERT INTO cafe_detail
            (master_id, platform, place_id, place_url, rating, review_count, phone)
            VALUES (%s, %s, %s, %s, %s, %s, %s)
            ON DUPLICATE KEY UPDATE
                place_url = VALUES(place_url),
                rating = VALUES(rating),
                review_count = VALUES(review_count),
                phone = VALUES(phone),
                last_synced_at = CURRENT_TIMESTAMP
        """
        
        values = (
            master_id,
            platform_data['platform'],
            platform_data['place_id'],
            platform_data.get('place_url', ''),
            platform_data.get('rating'),
            platform_data.get('review_count', 0),
            platform_data.get('phone', '')
        )
        
        cursor.execute(query, values)
        conn.commit()
        cursor.close()
    
    def run_phase3(self):
        """
        Phase 3: 크리마 지수 계산
        """
        print("=" * 60)
        print("Phase 3: 크리마 지수 계산")
        print("=" * 60)
        
        # cafe_detail에서 평점 데이터 조회
        conn = mysql.connector.connect(**self.db_config)
        cursor = conn.cursor(dictionary=True)
        
        cursor.execute("""
            SELECT 
                cm.id as master_id,
                MAX(CASE WHEN cd.platform = 'KAKAO' THEN cd.rating END) as kakao_rating,
                MAX(CASE WHEN cd.platform = 'KAKAO' THEN cd.review_count END) as kakao_reviews,
                MAX(CASE WHEN cd.platform = 'NAVER' THEN cd.rating END) as naver_rating,
                MAX(CASE WHEN cd.platform = 'NAVER' THEN cd.review_count END) as naver_reviews
            FROM cafe_master cm
            LEFT JOIN cafe_detail cd ON cm.id = cd.master_id
            WHERE cm.status = 'ACTIVE'
            GROUP BY cm.id
        """)
        
        cafes = cursor.fetchall()
        cursor.close()
        conn.close()
        
        print(f"📊 {len(cafes)}개 카페 점수 계산 중...\n")
        
        calculated = 0
        
        for cafe in cafes:
            # 크리마 지수 계산
            score_data = self.crema_calculator.calculate_crema_score(
                kakao_rating=cafe['kakao_rating'],
                naver_rating=cafe['naver_rating'],
                kakao_reviews=cafe['kakao_reviews'] or 0,
                naver_reviews=cafe['naver_reviews'] or 0
            )
            
            # DB 저장
            self.crema_calculator.save_score(
                self.db_config,
                cafe['master_id'],
                score_data
            )
            
            calculated += 1
            
            if calculated % 100 == 0:
                print(f"  진행: {calculated}/{len(cafes)}")
        
        # 점수 분포 통계
        distribution = self.crema_calculator.get_score_distribution(self.db_config)
        
        print(f"\n✅ Phase 3 완료:")
        print(f"   - 계산 완료: {calculated}개")
        print(f"\n📊 크리마 지수 분포:")
        for grade, count in distribution.items():
            print(f"   - {grade}: {count}개")
        print()
    
    def run_all(self, max_pages: int = 10, match_limit: int = None):
        """
        전체 파이프라인 실행
        """
        print("\n" + "=" * 60)
        print("Creama 하이브리드 데이터 파이프라인 시작")
        print("=" * 60 + "\n")
        
        # Phase 1: 공공 데이터 수집
        self.run_phase1(max_pages=max_pages)
        
        # Phase 2: 데이터 매칭
        self.run_phase2(limit=match_limit)
        
        # Phase 3: 크리마 지수 계산
        self.run_phase3()
        
        print("\n" + "=" * 60)
        print("🎉 파이프라인 완료!")
        print("=" * 60 + "\n")


def main():
    parser = argparse.ArgumentParser(description="Creama 하이브리드 데이터 파이프라인")
    parser.add_argument('--phase', type=int, choices=[1, 2, 3], help='실행할 Phase (1, 2, 3)')
    parser.add_argument('--max-pages', type=int, default=10, help='Phase 1: 최대 페이지 수')
    parser.add_argument('--match-limit', type=int, help='Phase 2: 매칭할 카페 수 제한')
    parser.add_argument('--all', action='store_true', help='전체 파이프라인 실행')
    
    args = parser.parse_args()
    
    pipeline = HybridPipeline()
    
    if args.all:
        pipeline.run_all(max_pages=args.max_pages, match_limit=args.match_limit)
    elif args.phase == 1:
        pipeline.run_phase1(max_pages=args.max_pages)
    elif args.phase == 2:
        pipeline.run_phase2(limit=args.match_limit)
    elif args.phase == 3:
        pipeline.run_phase3()
    else:
        parser.print_help()


if __name__ == '__main__':
    main()
