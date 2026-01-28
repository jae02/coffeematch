# CoffeeMatch Docker 배포 가이드

## 🔄 워크플로우 요약 (개발자용)

이 프로젝트는 **로컬 개발 -> Docker Hub 빌드 -> VPS 배포** 순서로 진행됩니다.
Windows와 Mac(M1/M2 포함) 모두 동일한 프로세스를 따릅니다.

### 1️⃣ 로컬 개발 (Local Development)
기능 개발 및 테스트는 로컬 컴퓨터에서 진행합니다.
```bash
# 개발 서버 실행 (DB + Backend + Frontend)
# Docker Desktop(최신) 사용 시 'docker-compose' 대신 'docker compose' 권장
docker compose up -d

# 개발 종료
docker compose down
```
- 접속: `http://localhost:8080`

### 2️⃣ 배포 이미지 빌드 (Build & Push)
개발이 완료되면 로컬에서 이미지를 빌드하여 Docker Hub로 보냅니다.
(VPS가 아닌 로컬에서 빌드하므로 VPS 성능에 영향을 주지 않습니다.)

**Windows:**
```powershell
.\docker\deploy.ps1 -Username jay02
```

**Mac / Linux:**
```bash
# 실행 권한 부여 (최초 1회)
chmod +x ./docker/deploy.sh

# 스크립트 실행
./docker/deploy.sh jay02
```

### 3️⃣ VPS 배포 (Production Deploy)
서버에 접속하여 최신 이미지를 받고 컨테이너를 재시작합니다.

```bash
# 1. VPS 접속
ssh root@175.126.73.154

# 2. 이동 및 최신 버전 적용
cd /opt/coffeematch/docker
docker-compose -f docker-compose.prod.yml pull
docker-compose -f docker-compose.prod.yml up -d
```

---

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
