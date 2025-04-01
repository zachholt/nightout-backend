#!/bin/bash
set -e

echo "Initializing local PostgreSQL database for Nightout development..."

# Check if PostgreSQL is running
echo "Checking if PostgreSQL is running..."
if ! pg_isready -q; then
  echo "PostgreSQL is not running. Starting PostgreSQL..."
  brew services start postgresql@14
  sleep 3
fi

# Create the database if it doesn't exist
echo "Creating database if it doesn't exist..."
psql -U postgres -c "SELECT 1 FROM pg_database WHERE datname = 'nightoutdb'" | grep -q 1 || psql -U postgres -c "CREATE DATABASE nightoutdb"

# Check if test database exists
echo "Creating test database if it doesn't exist..."
psql -U postgres -c "SELECT 1 FROM pg_database WHERE datname = 'nightoutdb_test'" | grep -q 1 || psql -U postgres -c "CREATE DATABASE nightoutdb_test"

# Apply schema to the database
echo "Applying schema to database..."
psql -U postgres -d nightoutdb -f nightout-backend/src/main/resources/schema.sql

# Apply schema to the test database
echo "Applying schema to test database..."
psql -U postgres -d nightoutdb_test -f nightout-backend/src/main/resources/schema.sql

# Insert sample user data
echo "Inserting sample user data..."
psql -U postgres -d nightoutdb -c "
INSERT INTO users (name, email, password, created_at, profile_image, latitude, longitude)
VALUES 
  ('Test User', 'test@example.com', '\$2a\$10\$hzx.yyh8mW.V8xhzRZzQJ.oZcB1vK3zrt1QKxSxUXf3yVYSQZAXA.', NOW(), NULL, 40.7128, -74.0060),
  ('Jane Doe', 'jane@example.com', '\$2a\$10\$hzx.yyh8mW.V8xhzRZzQJ.oZcB1vK3zrt1QKxSxUXf3yVYSQZAXA.', NOW(), NULL, 34.0522, -118.2437)
ON CONFLICT (email) DO NOTHING;"

echo "Local database setup complete!"
echo ""
echo "You can now start your application with:"
echo "cd nightout-backend && ./mvnw spring-boot:run"
echo ""
echo "Database connection details:"
echo "URL: jdbc:postgresql://localhost:5432/nightoutdb"
echo "Username: postgres"
echo "Password: postgres" 