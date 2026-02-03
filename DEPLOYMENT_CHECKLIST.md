# 프로덕션 배포 체크리스트

## ✅ 배포 전 확인사항

### 로컬 환경
- [ ] Docker Desktop 실행 중
- [ ] 로컬 DB에 카페 데이터 존재 확인
  ```bash
  docker exec creama-db mysql -uroot -p1234 -e "SELECT COUNT(*) FROM coffeematch.cafe"
  ```
- [ ] 백엔드 정상 작동 확인 (`http://localhost:9090/api/cafes`)
- [ ] 프론트엔드 정상 작동 확인 (`http://localhost:5173`)

### 코드 준비
- [x] `frontend/.env.production` 파일 존재 확인
- [x] `frontend/Dockerfile`에 환경 변수 복사 코드 추가됨
- [ ] 최신 코드 커밋 및 푸시 완료

---

## 📦 배포 단계

### 1단계: 이미지 빌드 및 푸시

**Windows:**
```powershell
cd docker
.\deploy.ps1 -Username jay02
```

**Mac/Linux:**
```bash
cd docker
chmod +x deploy.sh
./deploy.sh jay02
```

**예상 결과:**
- ✅ Frontend 이미지 빌드 성공
- ✅ Backend 이미지 빌드 성공
- ✅ Docker Hub 푸시 완료

---

### 2단계: 데이터 마이그레이션 (최초 배포 시만)

**Windows:**
```powershell
cd docker
.\export_data.ps1
```

**Mac/Linux:**
```bash
cd docker
chmod +x export_data.sh
./export_data.sh
```

**예상 결과:**
- ✅ `cafe_migration_data.sql` 파일 생성
- ✅ 카페 개수 표시 (예: 243개)

**VPS로 전송:**
```bash
scp docker/cafe_migration_data.sql root@175.126.73.154:/opt/coffeematch/docker/
```

---

### 3단계: VPS 배포

```bash
# VPS 접속
ssh root@175.126.73.154

# 프로젝트 디렉토리로 이동
cd /opt/coffeematch/docker

# 환경 변수 설정
export DOCKER_USERNAME=jay02
export DB_PASSWORD=1234

# 최신 이미지 다운로드
docker compose -f docker-compose.prod.yml pull

# 컨테이너 시작
docker compose -f docker-compose.prod.yml up -d

# 데이터 임포트 (최초 배포 시만)
docker exec -i creama-db mysql -uroot -p1234 coffeematch < cafe_migration_data.sql
```

---

## 🔍 배포 후 검증

### 1. 컨테이너 상태 확인
```bash
docker ps
```

**예상 결과:**
```
CONTAINER ID   IMAGE                              STATUS
xxxxx          jay02/coffeematch-frontend:latest  Up X minutes
xxxxx          jay02/coffeematch-backend:latest   Up X minutes
xxxxx          mariadb:10.11                      Up X minutes
```

### 2. 백엔드 API 테스트
```bash
curl http://175.126.73.154:8080/api/cafes
```

**예상 결과:**
- HTTP 200 응답
- JSON 배열 반환
- 카페 데이터 포함

### 3. 프론트엔드 접속
브라우저에서 `http://175.126.73.154` 접속

**확인 사항:**
- [ ] 페이지 로드 성공
- [ ] 카페 목록 표시
- [ ] 카페 클릭 시 상세 페이지 표시
- [ ] 검색 기능 작동
- [ ] 이미지 로드 확인

### 4. 데이터베이스 확인
```bash
docker exec creama-db mysql -uroot -p1234 -e "SELECT COUNT(*) as total FROM coffeematch.cafe"
```

**예상 결과:**
- 카페 개수가 로컬과 동일 (예: 243개)

### 5. 로그 확인
```bash
# 백엔드 로그
docker logs creama-backend --tail 50

# 프론트엔드 로그
docker logs creama-frontend --tail 50

# DB 로그
docker logs creama-db --tail 50
```

**확인 사항:**
- [ ] 에러 메시지 없음
- [ ] "Started BackendApplication" 메시지 확인
- [ ] DB 연결 성공 메시지 확인

---

## 🚨 문제 해결

### 프론트엔드가 API에 연결되지 않는 경우

**증상:** 브라우저 콘솔에 "Failed to fetch" 또는 CORS 에러

**해결:**
1. `.env.production` 파일 확인
   ```bash
   cat frontend/.env.production
   # VITE_API_URL=http://175.126.73.154:8080
   ```

2. 프론트엔드 이미지 재빌드
   ```bash
   docker build --platform linux/amd64 -t jay02/coffeematch-frontend:latest ./frontend
   docker push jay02/coffeematch-frontend:latest
   ```

3. VPS에서 컨테이너 재시작
   ```bash
   ssh root@175.126.73.154
   cd /opt/coffeematch/docker
   docker compose -f docker-compose.prod.yml pull frontend
   docker compose -f docker-compose.prod.yml up -d frontend
   ```

### 카페 데이터가 없는 경우

**증상:** 프론트엔드에 "검색 결과가 없습니다" 표시

**해결:**
1. 데이터 마이그레이션 재실행
   ```bash
   # 로컬에서
   cd docker
   ./export_data.ps1  # Windows
   # 또는
   ./export_data.sh   # Mac/Linux
   
   # VPS로 전송 및 임포트
   scp cafe_migration_data.sql root@175.126.73.154:/opt/coffeematch/docker/
   ssh root@175.126.73.154 "cd /opt/coffeematch/docker && docker exec -i creama-db mysql -uroot -p1234 coffeematch < cafe_migration_data.sql"
   ```

### 백엔드가 시작되지 않는 경우

**증상:** `docker ps`에 backend 컨테이너 없음

**해결:**
1. 로그 확인
   ```bash
   docker logs creama-backend
   ```

2. DB 연결 확인
   ```bash
   docker exec creama-backend env | grep SPRING_DATASOURCE
   ```

3. 컨테이너 재시작
   ```bash
   docker compose -f docker-compose.prod.yml restart backend
   ```

---

## 📊 성공 기준

- ✅ 모든 컨테이너 실행 중 (frontend, backend, db)
- ✅ 백엔드 API 응답 정상 (HTTP 200)
- ✅ 프론트엔드 페이지 로드 성공
- ✅ 카페 목록 표시 (243개)
- ✅ 카페 상세 페이지 작동
- ✅ 검색 기능 작동
- ✅ 에러 로그 없음

---

## 🔄 재배포 (코드 수정 후)

코드를 수정한 경우:

```bash
# 1. 로컬에서 이미지 재빌드 및 푸시
cd docker
.\deploy.ps1 -Username jay02  # Windows
# 또는
./deploy.sh jay02             # Mac/Linux

# 2. VPS에서 업데이트
ssh root@175.126.73.154
cd /opt/coffeematch/docker
docker compose -f docker-compose.prod.yml pull
docker compose -f docker-compose.prod.yml up -d
```

**데이터는 유지됩니다** (Docker volume 사용)
