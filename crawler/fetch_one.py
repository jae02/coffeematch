"""
공공데이터에서 카페 1개만 가져와서 DB에 저장하는 스크립트
"""
import requests
import json

API_KEY = '64674241757332313737587943504a'
url = f'http://openapi.seoul.go.kr:8088/{API_KEY}/json/LOCALDATA_072405/1/500/'

print("Fetching data from Seoul Open Data...")
response = requests.get(url, timeout=30)
data = response.json()

rows = data.get('LOCALDATA_072405', {}).get('row', [])
print(f"Total rows fetched: {len(rows)}")

# Find first cafe with status 01 (operating) and starts with Seoul address
for row in rows:
    status = row.get('TRDSTATEGBN', '')
    business_type = row.get('UPTAENM', '')
    address = row.get('RDNWHLADDR', '') or row.get('SITEWHLADDR', '')
    
    # Filter: operating, Seoul address, cafe-related business
    if status == '01' and address.startswith('서울') and '커피' in business_type:
        cafe_data = {
            'name': row.get('BPLCNM', ''),
            'address': address,
            'phone': row.get('SITETEL', ''),
            'category': business_type,
            'sourcePlatform': 'PUBLIC_DATA',
            'platformId': f"public_{row.get('MGTNO', '')}",
            'businessType': business_type,
            'status': 'NEW'
        }
        
        print(f"\nFound cafe: {cafe_data['name']}")
        print(f"Address: {cafe_data['address']}")
        print(f"Business Type: {cafe_data['businessType']}")
        print(f"Phone: {cafe_data['phone']}")
        
        # Save to backend
        print("\nSaving to database...")
        try:
            resp = requests.post(
                'http://localhost:9090/api/admin/cafes/crawl',
                json=cafe_data,
                headers={'Content-Type': 'application/json'},
                timeout=30
            )
            
            if resp.status_code == 200:
                result = resp.json()
                print(f"SUCCESS! Saved with ID: {result.get('id')}")
                print(f"Full response: {json.dumps(result, ensure_ascii=False, indent=2)}")
            else:
                print(f"Failed: {resp.status_code} - {resp.text}")
        except Exception as e:
            print(f"Error: {e}")
        break
else:
    print("No matching cafe found!")
