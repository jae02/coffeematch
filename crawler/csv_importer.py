"""
정리된 CSV 파일에서 데이터를 추출하여 cafe 테이블로 import
"""
import pandas as pd
import mysql.connector
from mysql.connector import Error
from pyproj import Transformer
from typing import Dict, List
import os
import sys

# 서버 MariaDB 설정
DB_CONFIG = {
    'host': '175.126.73.154',
    'port': 3306,
    'user': 'root',
    'password': '1234',
    'database': 'coffeematch',
    'charset': 'utf8mb4'
}


class CSVImporter:
    """
    정리된 CSV 파일을 cafe 테이블에 import
    """
    
    # EPSG:5174 -> WGS84 (EPSG:4326) 좌표 변환기
    transformer = Transformer.from_crs("EPSG:5174", "EPSG:4326", always_xy=True)
    
    # 카페 관련 업태구분명 필터
    CAFE_KEYWORDS = ['커피', '카페', '까페', '디저트', '베이커리', '제과', '빵']
    
    def __init__(self, db_config: Dict):
        self.db_config = db_config
        self.conn = None
    
    def connect(self):
        """DB 연결"""
        try:
            self.conn = mysql.connector.connect(**self.db_config)
            print("✅ DB 연결 성공")
        except Error as e:
            print(f"❌ DB 연결 실패: {e}")
            raise
    
    def close(self):
        """DB 연결 종료"""
        if self.conn and self.conn.is_connected():
            self.conn.close()
            print("DB 연결 종료")
    
    def transform_coordinates(self, x: float, y: float) -> tuple:
        """
        EPSG:5174 좌표를 WGS84로 변환
        """
        try:
            lon, lat = self.transformer.transform(x, y)
            # 유효한 좌표인지 확인 (한국 범위)
            if 33.0 <= lat <= 43.0 and 124.0 <= lon <= 132.0:
                return lat, lon
            return None, None
        except Exception:
            return None, None
    
    def is_cafe_related(self, industry_name: str) -> bool:
        """
        카페 관련 업태인지 확인
        """
        if not industry_name or pd.isna(industry_name):
            return False
        return any(keyword in str(industry_name) for keyword in self.CAFE_KEYWORDS)
    
    def load_csv(self, file_path: str, filter_cafe_only: bool = True) -> pd.DataFrame:
        """
        CSV 파일 로드 및 전처리
        """
        print(f"\n📂 CSV 로드 중: {file_path}")
        
        # CSV 로드
        df = pd.read_csv(file_path, encoding='utf-8-sig', low_memory=False)
        print(f"   원본 데이터: {len(df)}개")
        
        # 카페 관련 업태만 필터링
        if filter_cafe_only:
            df = df[df['업태구분명'].apply(self.is_cafe_related)]
            print(f"   카페 관련 필터링 후: {len(df)}개")
        
        # 좌표가 있는 데이터만 필터링
        df = df[df['좌표정보x(epsg5174)'].notna() & df['좌표정보y(epsg5174)'].notna()]
        print(f"   좌표 있는 데이터: {len(df)}개")
        
        return df
    
    def import_csv(self, file_path: str, filter_cafe_only: bool = True) -> Dict:
        """
        CSV 파일을 cafe 테이블에 import
        
        cafe 테이블 컬럼:
        - id, name, address, phone, description, image_url, business_type
        - source_platform, platform_id, latitude, longitude, status
        - last_synced_at, bookmark_count, review_count, internal_rating_avg
        """
        df = self.load_csv(file_path, filter_cafe_only)
        
        if df.empty:
            print("⚠️ import할 데이터가 없습니다")
            return {'inserted': 0, 'skipped': 0, 'errors': 0}
        
        if not self.conn or not self.conn.is_connected():
            self.connect()
        
        cursor = self.conn.cursor()
        
        inserted = 0
        skipped = 0
        errors = 0
        
        # cafe 테이블에 INSERT (중복 체크: 이름 + 좌표 근사값)
        # platform_id에 관리번호 저장하여 중복 방지
        insert_query = """
            INSERT INTO cafe 
            (name, address, latitude, longitude, business_type, source_platform, platform_id, status)
            VALUES (%s, %s, %s, %s, %s, 'PUBLIC_DATA', %s, 'ACTIVE')
        """
        
        # 중복 체크 쿼리 (platform_id로 확인)
        check_query = """
            SELECT id FROM cafe WHERE platform_id = %s AND source_platform = 'PUBLIC_DATA'
        """
        
        print(f"\n💾 {len(df)}개 데이터 저장 중...")
        
        for idx, row in df.iterrows():
            try:
                # 좌표 변환 (EPSG:5174 -> WGS84)
                x = row.get('좌표정보x(epsg5174)')
                y = row.get('좌표정보y(epsg5174)')
                
                lat, lon = self.transform_coordinates(float(x), float(y))
                
                if lat is None or lon is None:
                    errors += 1
                    continue
                
                # 데이터 추출
                platform_id = str(row.get('관리번호'))  # 관리번호를 platform_id로 사용
                name = row.get('사업장명')
                address = row.get('도로명전체주소') or row.get('소재지전체주소')
                business_type = row.get('업태구분명')
                
                # 필수값 검증
                if not platform_id or not name:
                    errors += 1
                    continue
                
                # NaN 처리
                if pd.isna(address):
                    address = None
                if pd.isna(business_type):
                    business_type = None
                
                # 중복 체크
                cursor.execute(check_query, (platform_id,))
                existing = cursor.fetchone()
                
                if existing:
                    skipped += 1
                    continue
                
                # INSERT
                values = (
                    str(name)[:255],
                    str(address)[:255] if address else None,
                    lat,
                    lon,
                    str(business_type)[:50] if business_type else None,
                    platform_id[:100]
                )
                
                cursor.execute(insert_query, values)
                inserted += 1
                
                # 500개마다 진행상황 출력 및 커밋
                if inserted % 500 == 0:
                    self.conn.commit()
                    print(f"   진행: {inserted + skipped}/{len(df)} (신규: {inserted}, 중복: {skipped})")
                
            except Exception as e:
                errors += 1
                if errors <= 5:  # 처음 5개 에러만 출력
                    print(f"⚠️ {row.get('사업장명', 'Unknown')} 저장 실패: {e}")
        
        self.conn.commit()
        cursor.close()
        
        print(f"\n✅ Import 완료:")
        print(f"   - 신규 추가: {inserted}개")
        print(f"   - 중복 스킵: {skipped}개")
        print(f"   - 에러: {errors}개")
        
        return {'inserted': inserted, 'skipped': skipped, 'errors': errors}
    
    def import_all_csv(self, csv_files: List[str], filter_cafe_only: bool = True) -> Dict:
        """
        여러 CSV 파일을 import
        """
        total_stats = {'inserted': 0, 'skipped': 0, 'errors': 0}
        
        for csv_file in csv_files:
            if not os.path.exists(csv_file):
                print(f"❌ 파일을 찾을 수 없음: {csv_file}")
                continue
            
            stats = self.import_csv(csv_file, filter_cafe_only)
            total_stats['inserted'] += stats['inserted']
            total_stats['skipped'] += stats['skipped']
            total_stats['errors'] += stats['errors']
        
        print(f"\n" + "=" * 50)
        print(f"📊 전체 Import 결과:")
        print(f"   - 총 신규 추가: {total_stats['inserted']}개")
        print(f"   - 총 중복 스킵: {total_stats['skipped']}개")
        print(f"   - 총 에러: {total_stats['errors']}개")
        print("=" * 50)
        
        return total_stats


def main():
    """
    메인 실행 함수
    """
    import argparse
    
    parser = argparse.ArgumentParser(description="정리된 CSV 파일을 cafe DB로 import")
    parser.add_argument('--files', nargs='+', help='import할 CSV 파일 경로들')
    parser.add_argument('--all', action='store_true', help='기본 정리됨 CSV 파일 모두 import')
    parser.add_argument('--no-filter', action='store_true', help='카페 관련 필터링 비활성화')
    
    args = parser.parse_args()
    
    importer = CSVImporter(DB_CONFIG)
    
    try:
        importer.connect()
        
        if args.all:
            # 기본 경로의 정리된 CSV 파일들
            base_dir = os.path.dirname(os.path.dirname(__file__))  # coffee 폴더
            csv_files = [
                os.path.join(base_dir, 'fulldata_07_24_05_P_휴게음식점_정리됨.csv'),
                os.path.join(base_dir, 'fulldata_07_24_04_P_일반음식점_정리됨.csv'),
            ]
            importer.import_all_csv(csv_files, filter_cafe_only=not args.no_filter)
        elif args.files:
            importer.import_all_csv(args.files, filter_cafe_only=not args.no_filter)
        else:
            parser.print_help()
    finally:
        importer.close()


if __name__ == '__main__':
    main()
