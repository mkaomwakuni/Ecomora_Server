package est.ecomora.server

// Environment-based configuration
val SERVER_PORT = System.getenv("PORT")?.toIntOrNull() ?: 8080
val SERVER_HOST = System.getenv("HOST") ?: "0.0.0.0"

// Application environment - must be defined before DB_URL
val IS_PRODUCTION = System.getenv("ENV") == "production" || 
                   System.getenv("DATABASE_URL") != null ||
                   System.getenv("RENDER") != null

// Database configuration - flexible for cloud platforms
val DB_URL = run {
    // Try to get DATABASE_URL directly first
    val databaseUrl = System.getenv("DATABASE_URL")
    
    when {
        databaseUrl != null -> {
            println("Found DATABASE_URL in environment variable")
            // Handle Render's postgres:// format by converting to jdbc:postgresql://
            if (databaseUrl.startsWith("postgres://")) {
                databaseUrl.replace("postgres://", "jdbc:postgresql://")
            } else {
                databaseUrl
            }
        }
        // For production, use DATABASE_URL from environment or secret files
        IS_PRODUCTION -> {
            // Use external database URL (required for Render web service to database connection)
            val fallbackUrl = "jdbc:postgresql://dpg-d2iqj3je5dus73ba5bd0-a.oregon-postgres.render.com:5432/ecomora_db"
            println("Using external database URL for Render: $fallbackUrl")
            fallbackUrl
        }
        // Development fallback
        !IS_PRODUCTION -> "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
        else -> {
            println("ERROR: DATABASE_URL not found in production environment")
            println("Available environment variables:")
            System.getenv().entries.forEach { (key, value) ->
                println("$key = $value")
            }
            throw IllegalStateException("DATABASE_URL not found in production environment")
        }
    }
}

val DB_USERNAME = System.getenv("DB_USERNAME") ?: System.getenv("POSTGRES_USER") ?: "ecomora_user"
val DB_PASSWORD = System.getenv("DB_PASSWORD") ?: System.getenv("POSTGRES_PASSWORD") ?: "q8e40J52nfQyv5cWwqO6aB1Q31mjAK1Q"

// Static files configuration
val STATIC_FILE_ROOT = System.getenv("STATIC_FILE_ROOT") ?: "/app/uploads"
val UPLOAD_DIR = System.getenv("UPLOAD_DIR") ?: "${System.getProperty("user.home")}/uploads"

val APP_VERSION = System.getenv("APP_VERSION") ?: "1.0.0"