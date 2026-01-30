"""
cafe_master 테이블 데이터 Importer (Upsert 로직)
"""
import mysql.connector
from mysql.connector import Error
import pandas as pd
from typing import Dict
from datetime import datetime

class MasterImporter:
    """
    cafe_master 테이블에 공공 데이터 저장/갱신
    """
    
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
    
    def upsert_cafes(self, df: pd.DataFrame) -> Dict[str, int]:
        """
        cafe_master 테이블에 Upsert
        
        Args:
            df: 공공 API에서 가져온 DataFrame
            
        Returns:
            {'inserted': N, 'updated': M} 통계
        """
        if df.empty:
            print("⚠️ 저장할 데이터가 없습니다")
            return {'inserted': 0, 'updated': 0}
        
        if not self.conn or not self.conn.is_connected():
            self.connect()
        
        cursor = self.conn.cursor()
        
        inserted = 0
        updated = 0
        errors = 0
        
        query = """
            INSERT INTO cafe_master 
            (business_number, business_name, jibun_address, road_address,
             latitude, longitude, industry_code, industry_name, opened_at, status)
            VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, 'ACTIVE')
            ON DUPLICATE KEY UPDATE
                business_name = VALUES(business_name),
                jibun_address = VALUES(jibun_address),
                road_address = VALUES(road_address),
                latitude = VALUES(latitude),
                longitude = VALUES(longitude),
                industry_code = VALUES(industry_code),
                industry_name = VALUES(industry_name),
                status = 'ACTIVE',
                updated_at = CURRENT_TIMESTAMP
        """
        
        print(f"💾 {len(df)}개 카페 저장 중...")
        
        for idx, row in df.iterrows():
            try:
                # 날짜 변환
                opened_at = self._parse_date(row.get('opened_at'))
                
                values = (
                    row.get('business_number'),
                    row.get('business_name'),
                    row.get('jibun_address'),
                    row.get('road_address'),
                    float(row.get('latitude')),
                    float(row.get('longitude')),
                    row.get('industry_code'),
                    row.get('industry_name'),
                    opened_at
                )
                
                cursor.execute(query, values)
                
                # rowcount: 1=INSERT, 2=UPDATE
                if cursor.rowcount == 1:
                    inserted += 1
                elif cursor.rowcount == 2:
                    updated += 1
                
                # 100개마다 진행상황 출력
                if (idx + 1) % 100 == 0:
                    print(f"  진행: {idx + 1}/{len(df)}")
                    
            except Exception as e:
                errors += 1
                if errors <= 5:  # 처음 5개 에러만 출력
                    print(f"⚠️ {row.get('business_name', 'Unknown')} 저장 실패: {e}")
        
        self.conn.commit()
        cursor.close()
        
        print(f"\n✅ 저장 완료:")
        print(f"   - 신규 추가: {inserted}개")
        print(f"   - 기존 갱신: {updated}개")
        print(f"   - 실패: {errors}개")
        
        return {'inserted': inserted, 'updated': updated, 'errors': errors}
    
    def _parse_date(self, date_str) -> str:
        """
        날짜 문자열 파싱
        
        예: '20240115' -> '2024-01-15'
        """
        if pd.isna(date_str) or not date_str:
            return None
        
        try:
            date_str = str(date_str)
            if len(date_str) == 8:
                return f"{date_str[:4]}-{date_str[4:6]}-{date_str[6:8]}"
            return None
        except:
            return None
    
    def mark_closed_cafes(self, active_business_numbers: list) -> int:
        """
        공공 데이터에 없는 카페를 CLOSED로 표시
        
        Args:
            active_business_numbers: 현재 활성 인허가번호 목록
            
        Returns:
            CLOSED로 변경된 카페 수
        """
        if not self.conn or not self.conn.is_connected():
            self.connect()
        
        cursor = self.conn.cursor()
        
        # 공공 데이터에 없지만 DB에는 있는 카페 찾기
        placeholders = ','.join(['%s'] * len(active_business_numbers))
        query = f"""
            UPDATE cafe_master
            SET status = 'CLOSED', updated_at = CURRENT_TIMESTAMP
            WHERE business_number NOT IN ({placeholders})
              AND status = 'ACTIVE'
        """
        
        cursor.execute(query, active_business_numbers)
        closed_count = cursor.rowcount
        
        self.conn.commit()
        cursor.close()
        
        if closed_count > 0:
            print(f"⚠️ {closed_count}개 카페를 CLOSED로 표시")
        
        return closed_count
    
    def get_statistics(self) -> Dict:
        """
        cafe_master 테이블 통계
        """
        if not self.conn or not self.conn.is_connected():
            self.connect()
        
        cursor = self.conn.cursor(dictionary=True)
        
        cursor.execute("""
            SELECT 
                status,
                COUNT(*) as count
            FROM cafe_master
            GROUP BY status
        """)
        
        stats = cursor.fetchall()
        cursor.close()
        
        return {row['status']: row['count'] for row in stats}
