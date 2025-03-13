#!/bin/bash
set -e

echo "Setting up the database..."

# Run the initialization script
./init-db.sh

# Run the migration script
./migrate-db.sh

echo "Database setup complete!" 