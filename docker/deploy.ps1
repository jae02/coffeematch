# Docker Hub 배포 스크립트 (Windows PowerShell)
# 사용법: .\deploy.ps1 -Username YOUR_DOCKER_USERNAME

param(
    [Parameter(Mandatory=$true)]
    [string]$Username
)

$ErrorActionPreference = "Stop"

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "CoffeeMatch Docker Build & Push" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

# 프로젝트 루트로 이동
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location (Join-Path $ScriptDir "..")

# Backend 빌드 & 푸시
Write-Host "[1/2] Backend 이미지 빌드 중..." -ForegroundColor Yellow
docker build --platform linux/amd64 -t "${Username}/coffeematch-backend:latest" ./backend
if ($LASTEXITCODE -ne 0) { throw "Backend 빌드 실패" }

Write-Host "[1/2] Backend 이미지 푸시 중..." -ForegroundColor Yellow
docker push "${Username}/coffeematch-backend:latest"
if ($LASTEXITCODE -ne 0) { throw "Backend 푸시 실패" }

# Frontend 빌드 & 푸시
Write-Host "[2/2] Frontend 이미지 빌드 중..." -ForegroundColor Yellow
docker build --platform linux/amd64 -t "${Username}/coffeematch-frontend:latest" ./frontend
if ($LASTEXITCODE -ne 0) { throw "Frontend 빌드 실패" }

Write-Host "[2/2] Frontend 이미지 푸시 중..." -ForegroundColor Yellow
docker push "${Username}/coffeematch-frontend:latest"
if ($LASTEXITCODE -ne 0) { throw "Frontend 푸시 실패" }

Write-Host ""
Write-Host "=====================================" -ForegroundColor Green
Write-Host "빌드 & 푸시 완료!" -ForegroundColor Green
Write-Host "=====================================" -ForegroundColor Green
Write-Host ""
Write-Host "VPS 배포 명령어:" -ForegroundColor Cyan
Write-Host "  ssh root@175.126.73.154"
Write-Host "  cd /path/to/coffeematch/docker"
Write-Host "  export DOCKER_USERNAME=$Username"
Write-Host "  docker compose -f docker-compose.prod.yml pull"
Write-Host "  docker compose -f docker-compose.prod.yml up -d"
