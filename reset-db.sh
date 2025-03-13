#!/bin/bash
set -e

echo "Resetting database for testing..."

# Wait for PostgreSQL to be ready
echo "Waiting for PostgreSQL to be ready..."
until docker exec nightout-postgres pg_isready -U postgres; do
  echo "PostgreSQL is not ready yet... waiting"
  sleep 2
done
echo "PostgreSQL is ready!"

# Drop the database if it exists
echo "Dropping database if it exists..."
docker exec nightout-postgres psql -U postgres -c "DROP DATABASE IF EXISTS nightoutdb;"

# Create a fresh database
echo "Creating a fresh database..."
docker exec nightout-postgres psql -U postgres -c "CREATE DATABASE nightoutdb;"

# Initialize the database schema
echo "Initializing database schema..."
docker exec nightout-postgres psql -U postgres -d nightoutdb -c "
-- Create sequence for user IDs
CREATE SEQUENCE IF NOT EXISTS user_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;
    
-- Create users table with latitude and longitude
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
);"

echo "Database reset complete!" 