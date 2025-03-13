#!/bin/bash
set -e

# Wait for PostgreSQL to be ready
echo "Waiting for PostgreSQL to be ready..."
until docker exec nightout-postgres pg_isready -U postgres; do
  echo "PostgreSQL is not ready yet... waiting"
  sleep 2
done
echo "PostgreSQL is ready!"

# Migrate the database schema
echo "Migrating database schema..."
docker exec nightout-postgres psql -U postgres -d nightoutdb -c "
-- Add latitude and longitude columns if they don't exist
ALTER TABLE users ADD COLUMN IF NOT EXISTS latitude DOUBLE PRECISION DEFAULT 0.0;
ALTER TABLE users ADD COLUMN IF NOT EXISTS longitude DOUBLE PRECISION DEFAULT 0.0;

-- Migrate data from coordinates column to latitude and longitude columns
DO \$\$
BEGIN
    -- Check if coordinates column exists
    IF EXISTS (SELECT FROM information_schema.columns WHERE table_name = 'users' AND column_name = 'coordinates') THEN
        -- Update latitude and longitude from coordinates
        UPDATE users
        SET 
            latitude = CASE 
                WHEN coordinates IS NULL OR coordinates = '' OR coordinates NOT LIKE '%,%' THEN 0.0
                ELSE CAST(SPLIT_PART(coordinates, ',', 1) AS DOUBLE PRECISION)
            END,
            longitude = CASE 
                WHEN coordinates IS NULL OR coordinates = '' OR coordinates NOT LIKE '%,%' THEN 0.0
                ELSE CAST(SPLIT_PART(coordinates, ',', 2) AS DOUBLE PRECISION)
            END
        WHERE 
            coordinates IS NOT NULL;
            
        -- Drop the coordinates column
        ALTER TABLE users DROP COLUMN IF EXISTS coordinates;
    END IF;
END
\$\$;
"

echo "Database migration complete!"

# Make the script executable
chmod +x migrate-db.sh 