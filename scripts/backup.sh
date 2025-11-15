#!/bin/bash

# Database Backup Script
BACKUP_DIR="/backups"
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="${BACKUP_DIR}/backup_${DATE}.sql.gz"

echo "🗄️ Starting database backup at $(date)"

# Create backup
pg_dump -h ${PGHOST} -U ${PGUSER} -d ${PGDATABASE} | gzip > ${BACKUP_FILE}

if [ $? -eq 0 ]; then
    echo "✅ Backup created successfully: ${BACKUP_FILE}"

    # Keep only last 7 days of backups
    find ${BACKUP_DIR} -name "backup_*.sql.gz" -mtime +7 -delete
    echo "🧹 Cleaned up old backups (older than 7 days)"
else
    echo "❌ Backup failed!"
    exit 1
fi

echo "📊 Current backup files:"
ls -lh ${BACKUP_DIR}/backup_*.sql.gz

