package est.ecomora.server

// Environment-based configuration
val SERVER_PORT = System.getenv("PORT")?.toIntOrNull() ?: 8080
val SERVER_HOST = System.getenv("HOST") ?: "0.0.0.0"

// Database configuration
val DB_URL = System.getenv("DATABASE_URL") ?: "jdbc:postgresql://localhost:5432/ecomora_db"
val DB_USERNAME = System.getenv("DB_USERNAME") ?: "postgres"
val DB_PASSWORD = System.getenv("DB_PASSWORD") ?: "password"

// Static files configuration
val STATIC_FILE_ROOT = System.getenv("STATIC_FILE_ROOT") ?: "/app/uploads"
val UPLOAD_DIR = System.getenv("UPLOAD_DIR") ?: "${System.getProperty("user.home")}/uploads"

// Application environment
val IS_PRODUCTION = System.getenv("ENV") == "production"
val APP_VERSION = System.getenv("APP_VERSION") ?: "1.0.0"