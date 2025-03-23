#!/bin/bash
set -e

echo "Starting database backup process..."

# Wait for PostgreSQL to be ready
echo "Waiting for PostgreSQL to be ready..."
until docker exec nightout-postgres pg_isready -U postgres; do
  echo "PostgreSQL is not ready yet... waiting"
  sleep 2
done
echo "PostgreSQL is ready!"

# Create a backup directory if it doesn't exist
mkdir -p db_backup

# Export current data from tables
echo "Exporting current data..."
docker exec nightout-postgres pg_dump -U postgres -d nightoutdb -t users --data-only --column-inserts > db_backup/users_data.sql
docker exec nightout-postgres pg_dump -U postgres -d nightoutdb -t favorites --data-only --column-inserts > db_backup/favorites_data.sql

# Backup sequences current values
echo "Backing up sequence values..."
docker exec nightout-postgres psql -U postgres -d nightoutdb -t -c "SELECT last_value FROM user_id_seq;" > db_backup/user_id_seq_value.txt
docker exec nightout-postgres psql -U postgres -d nightoutdb -t -c "SELECT last_value FROM favorite_id_seq;" > db_backup/favorite_id_seq_value.txt

# Drop existing tables and sequences
echo "Dropping existing tables and sequences..."
docker exec nightout-postgres psql -U postgres -d nightoutdb -c "
DROP TABLE IF EXISTS favorites CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP SEQUENCE IF EXISTS user_id_seq CASCADE;
DROP SEQUENCE IF EXISTS favorite_id_seq CASCADE;
"

# Create new tables and sequences
echo "Creating new tables and sequences..."
docker exec nightout-postgres psql -U postgres -d nightoutdb -c "
-- Create sequences
CREATE SEQUENCE IF NOT EXISTS user_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

CREATE SEQUENCE IF NOT EXISTS favorite_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT NOT NULL DEFAULT nextval('user_id_seq'),
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    profile_image VARCHAR(255),
    latitude DOUBLE PRECISION DEFAULT 0.0,
    longitude DOUBLE PRECISION DEFAULT 0.0,
    CONSTRAINT users_pkey PRIMARY KEY (id)
);

-- Create favorites table
CREATE TABLE IF NOT EXISTS favorites (
    id BIGINT NOT NULL DEFAULT nextval('favorite_id_seq'),
    user_id BIGINT NOT NULL,
    location_id VARCHAR(255) NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT favorites_pkey PRIMARY KEY (id),
    CONSTRAINT favorites_user_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT favorites_unique_user_location UNIQUE (user_id, location_id)
);"

# Restore sequence values
echo "Restoring sequence values..."
USER_SEQ_VAL=$(cat db_backup/user_id_seq_value.txt)
FAVORITE_SEQ_VAL=$(cat db_backup/favorite_id_seq_value.txt)

docker exec nightout-postgres psql -U postgres -d nightoutdb -c "
SELECT setval('user_id_seq', ${USER_SEQ_VAL});
SELECT setval('favorite_id_seq', ${FAVORITE_SEQ_VAL});"

# Import the data back
echo "Importing data back into tables..."
cat db_backup/users_data.sql | docker exec -i nightout-postgres psql -U postgres -d nightoutdb
cat db_backup/favorites_data.sql | docker exec -i nightout-postgres psql -U postgres -d nightoutdb

echo "Database backup and restore process complete!" 