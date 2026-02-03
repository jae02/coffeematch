# Data Migration Script for CoffeeMatch Production Deployment (Windows PowerShell)
# This script exports cafe data from local Docker DB and prepares it for production

Write-Host "🔄 Starting data migration process..." -ForegroundColor Cyan

# Configuration
$LOCAL_CONTAINER = "creama-db"
$DB_USER = "root"
$DB_PASSWORD = "1234"
$DB_NAME = "coffeematch"
$OUTPUT_FILE = "cafe_migration_data.sql"

# Check if Docker container is running
$containerRunning = docker ps --filter "name=$LOCAL_CONTAINER" --format "{{.Names}}"
if (-not $containerRunning) {
    Write-Host "❌ Error: Docker container '$LOCAL_CONTAINER' is not running" -ForegroundColor Red
    Write-Host "   Please start it with: cd docker; docker compose up -d db" -ForegroundColor Yellow
    exit 1
}

Write-Host "📊 Exporting data from local database..." -ForegroundColor Cyan

# Export all relevant tables
docker exec $LOCAL_CONTAINER mysqldump `
    -u$DB_USER `
    -p$DB_PASSWORD `
    --single-transaction `
    --no-create-info `
    --skip-add-locks `
    --skip-comments `
    $DB_NAME `
    cafe `
    menu `
    review `
    platform_data `
    cafe_keyword_stat `
    user_keyword_vote `
    > $OUTPUT_FILE

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Data exported successfully to: $OUTPUT_FILE" -ForegroundColor Green
    
    # Show statistics
    $CAFE_COUNT = docker exec $LOCAL_CONTAINER mysql -u$DB_USER -p$DB_PASSWORD -N -e "SELECT COUNT(*) FROM $DB_NAME.cafe" 2>$null
    $MENU_COUNT = docker exec $LOCAL_CONTAINER mysql -u$DB_USER -p$DB_PASSWORD -N -e "SELECT COUNT(*) FROM $DB_NAME.menu" 2>$null
    $REVIEW_COUNT = docker exec $LOCAL_CONTAINER mysql -u$DB_USER -p$DB_PASSWORD -N -e "SELECT COUNT(*) FROM $DB_NAME.review" 2>$null
    
    Write-Host ""
    Write-Host "📈 Export Summary:" -ForegroundColor Cyan
    Write-Host "   - Cafes: $CAFE_COUNT"
    Write-Host "   - Menus: $MENU_COUNT"
    Write-Host "   - Reviews: $REVIEW_COUNT"
    Write-Host ""
    Write-Host "📦 Next steps:" -ForegroundColor Yellow
    Write-Host "   1. Transfer file to VPS: scp $OUTPUT_FILE root@175.126.73.154:/opt/coffeematch/docker/"
    Write-Host "   2. Import on VPS: ssh root@175.126.73.154 'cd /opt/coffeematch/docker && docker exec -i creama-db mysql -uroot -p1234 coffeematch < $OUTPUT_FILE'"
}
else {
    Write-Host "❌ Export failed" -ForegroundColor Red
    exit 1
}
