# CoffeeMatch Docker 배포 가이드

## 1. 빌드 & 푸시 명령어 (모든 OS 공통)

```bash
# Docker Hub 로그인
docker login

# Backend 빌드 & 푸시
docker build --platform linux/amd64 -t YOUR_USERNAME/coffeematch-backend:latest ./backend
docker push YOUR_USERNAME/coffeematch-backend:latest

# Frontend 빌드 & 푸시
docker build --platform linux/amd64 -t YOUR_USERNAME/coffeematch-frontend:latest ./frontend
docker push YOUR_USERNAME/coffeematch-frontend:latest
```

또는 스크립트 사용:
```powershell
# Windows
.\docker\deploy.ps1 -Username YOUR_USERNAME

# Linux/Mac
./docker/deploy.sh YOUR_USERNAME
```

---

## 2. VPS 배포 (175.126.73.154)

```bash
ssh root@175.126.73.154
cd /path/to/coffeematch/docker

# 환경변수 설정
export DOCKER_USERNAME=YOUR_USERNAME
export DB_PASSWORD=your_secure_password  # 선택

# 이미지 pull & 실행
docker compose -f docker-compose.prod.yml pull
docker compose -f docker-compose.prod.yml up -d
```

---

## 3. 재배포 절차 (package.json 또는 build.gradle 변경 시)

```bash
# === 로컬에서 ===
# 1. 해당 이미지만 다시 빌드 & 푸시

# Frontend (package.json 변경)
docker build --platform linux/amd64 -t YOUR_USERNAME/coffeematch-frontend:latest ./frontend
docker push YOUR_USERNAME/coffeematch-frontend:latest

# Backend (build.gradle 변경)
docker build --platform linux/amd64 -t YOUR_USERNAME/coffeematch-backend:latest ./backend
docker push YOUR_USERNAME/coffeematch-backend:latest

# === VPS에서 ===
# 2. 이미지 pull & 재시작
cd /path/to/coffeematch/docker
docker compose -f docker-compose.prod.yml pull
docker compose -f docker-compose.prod.yml up -d
```

---

## 접속 URL

- **Frontend**: http://175.126.73.154
- **Backend API**: http://175.126.73.154:8080
