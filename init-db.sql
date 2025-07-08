-- Initialize Ecomora Database
CREATE DATABASE IF NOT EXISTS ecomora_db;

-- Create user if not exists
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_user WHERE usename = 'ecomora_user') THEN
        CREATE USER ecomora_user WITH PASSWORD 'ecomora_password';
    END IF;
END
$$;

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE ecomora_db TO ecomora_user;

-- Connect to the database
\c ecomora_db;

-- Create extensions if needed
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Set proper permissions
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO ecomora_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO ecomora_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON FUNCTIONS TO ecomora_user;

-- Create logs table for application logging (optional)
CREATE TABLE IF NOT EXISTS application_logs (
    id SERIAL PRIMARY KEY,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    level VARCHAR(10) NOT NULL,
    logger VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    exception TEXT,
    thread_name VARCHAR(100),
    user_id BIGINT,
    request_id VARCHAR(100)
);

-- Create index for performance
CREATE INDEX IF NOT EXISTS idx_logs_timestamp ON application_logs(timestamp);
CREATE INDEX IF NOT EXISTS idx_logs_level ON application_logs(level);
CREATE INDEX IF NOT EXISTS idx_logs_logger ON application_logs(logger);

-- Application will create the other tables automatically through Exposed ORM