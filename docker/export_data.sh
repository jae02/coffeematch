#!/bin/bash
# Data Migration Script for CoffeeMatch Production Deployment
# This script exports cafe data from local Docker DB and prepares it for production

set -e  # Exit on error

echo "🔄 Starting data migration process..."

# Configuration
LOCAL_CONTAINER="creama-db"
DB_USER="root"
DB_PASSWORD="1234"
DB_NAME="coffeematch"
OUTPUT_FILE="cafe_migration_data.sql"

# Check if Docker container is running
if ! docker ps | grep -q "$LOCAL_CONTAINER"; then
    echo "❌ Error: Docker container '$LOCAL_CONTAINER' is not running"
    echo "   Please start it with: cd docker && docker compose up -d db"
    exit 1
fi

echo "📊 Exporting data from local database..."

# Export all relevant tables
docker exec $LOCAL_CONTAINER mysqldump \
    -u$DB_USER \
    -p$DB_PASSWORD \
    --single-transaction \
    --no-create-info \
    --skip-add-locks \
    --skip-comments \
    $DB_NAME \
    cafe \
    menu \
    review \
    platform_data \
    cafe_keyword_stat \
    user_keyword_vote \
    > $OUTPUT_FILE

if [ $? -eq 0 ]; then
    echo "✅ Data exported successfully to: $OUTPUT_FILE"
    
    # Show statistics
    CAFE_COUNT=$(docker exec $LOCAL_CONTAINER mysql -u$DB_USER -p$DB_PASSWORD -N -e "SELECT COUNT(*) FROM $DB_NAME.cafe" 2>/dev/null)
    MENU_COUNT=$(docker exec $LOCAL_CONTAINER mysql -u$DB_USER -p$DB_PASSWORD -N -e "SELECT COUNT(*) FROM $DB_NAME.menu" 2>/dev/null)
    REVIEW_COUNT=$(docker exec $LOCAL_CONTAINER mysql -u$DB_USER -p$DB_PASSWORD -N -e "SELECT COUNT(*) FROM $DB_NAME.review" 2>/dev/null)
    
    echo ""
    echo "📈 Export Summary:"
    echo "   - Cafes: $CAFE_COUNT"
    echo "   - Menus: $MENU_COUNT"
    echo "   - Reviews: $REVIEW_COUNT"
    echo ""
    echo "📦 Next steps:"
    echo "   1. Transfer file to VPS: scp $OUTPUT_FILE root@175.126.73.154:/opt/coffeematch/docker/"
    echo "   2. Import on VPS: ssh root@175.126.73.154 'cd /opt/coffeematch/docker && docker exec -i creama-db mysql -uroot -p1234 coffeematch < $OUTPUT_FILE'"
else
    echo "❌ Export failed"
    exit 1
fi
