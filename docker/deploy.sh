#!/bin/bash
# Docker Hub 배포 스크립트 (Linux/Mac)
# 사용법: ./deploy.sh YOUR_DOCKER_USERNAME

set -e

if [ -z "$1" ]; then
    echo "Usage: ./deploy.sh YOUR_DOCKER_USERNAME"
    exit 1
fi

USERNAME=$1

echo "====================================="
echo "CoffeeMatch Docker Build & Push"
echo "====================================="
echo ""

# 프로젝트 루트로 이동
cd "$(dirname "$0")/.."

# Backend 빌드 & 푸시
echo "[1/2] Backend 이미지 빌드 중..."
docker build --platform linux/amd64 -t "${USERNAME}/coffeematch-backend:latest" ./backend

echo "[1/2] Backend 이미지 푸시 중..."
docker push "${USERNAME}/coffeematch-backend:latest"

# Frontend 빌드 & 푸시
echo "[2/2] Frontend 이미지 빌드 중..."
docker build --platform linux/amd64 -t "${USERNAME}/coffeematch-frontend:latest" ./frontend

echo "[2/2] Frontend 이미지 푸시 중..."
docker push "${USERNAME}/coffeematch-frontend:latest"

echo ""
echo "====================================="
echo "빌드 & 푸시 완료!"
echo "====================================="
echo ""
echo "VPS 배포 명령어:"
echo "  ssh root@175.126.73.154"
echo "  cd /path/to/coffeematch/docker"
echo "  export DOCKER_USERNAME=$USERNAME"
echo "  docker compose -f docker-compose.prod.yml pull"
echo "  docker compose -f docker-compose.prod.yml up -d"
