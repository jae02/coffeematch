import mysql.connector
import os
import sys
from dotenv import load_dotenv

# Add current directory to sys.path
sys.path.append(os.getcwd())
from config import DB_CONFIG

def verify():
    print("Connecting to DB...")
    # Ensure charset is set
    config = DB_CONFIG.copy()
    config['charset'] = 'utf8mb4'
    
    conn = mysql.connector.connect(**config)
    cursor = conn.cursor(dictionary=True)
    
    cursor.execute("SELECT business_name, road_address FROM cafe_master ORDER BY id DESC LIMIT 5")
    rows = cursor.fetchall()
    
    print(f"Fetched {len(rows)} rows:")
    for row in rows:
        print(f"Name: {row['business_name']}, Addr: {row['road_address']}")

    conn.close()

if __name__ == "__main__":
    verify()
