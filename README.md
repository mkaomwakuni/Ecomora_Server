# Ecomora Server

This is a Kotlin Multiplatform project targeting the backend server, implemented with Ktor. The server provides a RESTful API for managing users, product categories, products, and services for an e-commerce platform.

## Features

- **User Management:** Register, update, delete, and fetch users with fields like username, email, password, full name, phone number, and user role.
- **Category Management:** Create, update, delete, and fetch product categories, each with a name, description, visibility, and image.
- **Product Management:** Add, update, delete, and fetch products, supporting fields such as name, description, price, image, category, stock, brand, availability, discount, promotion, and rating.
- **Service Management:** (Model present) Services can be managed with fields like name, description, price, category, status, and timestamps.
- **File Uploads:** Supports image uploads for products and categories.
- **Authentication & Authorization:** (JWT and session support present in dependencies, but not detailed in the routes above.)
- **Error Handling:** Returns appropriate HTTP status codes and error messages for invalid requests.

## Technologies Used

- **Kotlin (JVM)**
- **Ktor** (server framework)
- **Kotlinx Serialization** (for JSON serialization)
- **Exposed** (ORM for database access)
- **HikariCP** (JDBC connection pooling)
- **PostgreSQL** (primary database, with H2 for testing)
- **Logback** (logging)
- **Gradle** (build tool)
- **JWT & Session Authentication** (dependencies included)
- **Multipart Handling** (for file uploads)

## Project Structure

- `/server` — Ktor server application
  - `src/main/kotlin/est/ecomora/server/`
    - `Application.kt` — Main entry point and route registration
    - `plugins/Routes.kt` — All API endpoints for users, categories, products
    - `domain/model/` — Data models for Users, Products, Categories, Services
    - `domain/repository/` — Data access logic
    - `domain/service/` — Business logic

## How to Run

1. Ensure you have JDK 21+ and Gradle installed.
2. Configure your database connection in `application.conf`.
3. From the `/server` directory, run:
   ```
   ./gradlew run
   ```
   The server will start on port 8080 (or as configured).

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…