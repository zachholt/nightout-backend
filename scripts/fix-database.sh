#!/bin/bash

echo "Fixing database schema for NightOut application..."

# Connect to PostgreSQL container and run SQL commands
sudo docker exec -i nightout-postgres psql -U postgres -d nightoutdb << EOF
-- Drop sequence if exists
DROP SEQUENCE IF EXISTS user_id_seq;

-- Create sequence for user IDs
CREATE SEQUENCE IF NOT EXISTS user_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

-- Drop table if exists
DROP TABLE IF EXISTS users CASCADE;

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT NOT NULL DEFAULT nextval('user_id_seq'),
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    profile_image VARCHAR(255),
    coordinates VARCHAR(255),
    CONSTRAINT users_pkey PRIMARY KEY (id)
);
EOF

echo "Database schema fixed. Restarting application..."

# Restart the application container
sudo docker restart nightout-backend

echo "Done! Check the logs with: sudo docker logs nightout-backend --tail 50" 