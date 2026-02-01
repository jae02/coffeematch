import os
import sys
from dotenv import load_dotenv
import json

# Add current directory to sys.path
sys.path.append(os.getcwd())

from phase1_master_data.seoul_api_client import SeoulOpenAPIClient

# Load env
load_dotenv()
API_KEY = os.getenv("PUBLIC_API_KEY")

if not API_KEY:
    print("❌ PUBLIC_API_KEY not found in .env")
    sys.exit(1)

print(f"🔑 API Key: {API_KEY[:5]}...")

client = SeoulOpenAPIClient(API_KEY)

print("📡 Fetching data (1~5)...")
df = client.fetch_data(1, 5)

if df.empty:
    print("❌ No data fetched")
else:
    print(f"✅ Fetched {len(df)} rows")
    # print(df.columns)
    
    cols = ['business_name', 'road_address', 'latitude', 'longitude', 'tm_x', 'tm_y']
    available_cols = [c for c in cols if c in df.columns]
    
    print(df[available_cols].head().to_string())
    
    # Check coordinates
    first_row = df.iloc[0]
    print(f"\n📍 Sample Coordinates:")
    print(f"  Name: {first_row['business_name']}")
    print(f"  TM X: {first_row.get('tm_x')}")
    print(f"  TM Y: {first_row.get('tm_y')}")
    print(f"  Converted Lat: {first_row.get('latitude')}")
    print(f"  Converted Lon: {first_row.get('longitude')}")
