-- Create sequence for user IDs if it doesn't exist
CREATE SEQUENCE IF NOT EXISTS user_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

-- Create sequence for favorite IDs if it doesn't exist
CREATE SEQUENCE IF NOT EXISTS favorite_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

-- Create users table if it doesn't exist
CREATE TABLE IF NOT EXISTS users (
    id BIGINT NOT NULL DEFAULT nextval('user_id_seq'),
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    profile_image VARCHAR(255),
    latitude DOUBLE PRECISION DEFAULT 0.0,
    longitude DOUBLE PRECISION DEFAULT 0.0,
    CONSTRAINT users_pkey PRIMARY KEY (id)
);

-- Create favorites table if it doesn't exist
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
);