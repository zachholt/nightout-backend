-- Create sequence for users if it doesn't exist
CREATE SEQUENCE IF NOT EXISTS user_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- Create users table if it doesn't exist
CREATE TABLE IF NOT EXISTS users (
    id BIGINT DEFAULT nextval('user_id_seq') PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create sequence for favorites
CREATE SEQUENCE IF NOT EXISTS favorite_id_seq;

-- Create favorites table
CREATE TABLE IF NOT EXISTS favorites (
    id BIGINT DEFAULT nextval('favorite_id_seq') PRIMARY KEY,
    user_id BIGINT NOT NULL,
    location_id VARCHAR(255) NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT favorites_user_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT favorites_unique_user_location UNIQUE (user_id, location_id)
);

-- Create sequence for coordinates
CREATE SEQUENCE IF NOT EXISTS coordinate_id_seq;

-- Create coordinates table
CREATE TABLE IF NOT EXISTS coordinates (
    id BIGINT DEFAULT nextval('coordinate_id_seq') PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_user_coordinate UNIQUE (user_id)
); 

