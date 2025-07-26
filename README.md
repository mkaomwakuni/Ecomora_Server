# Ecomora Server

Ecomora Server is a modern backend solution for ecommerce platforms, built with Kotlin and Ktor. It
provides robust RESTful APIs to support scalable online store applications, with a secure, modular,
and production-ready codebase.

---

## Table of Contents

- [Introduction](#introduction)
- [Technologies Used](#technologies-used)
- [Features](#features)
- [Installation](#installation)
- [Setup](#setup)
- [Usage](#usage)
- [API Endpoints](#api-endpoints)
- [Contributing](#contributing)
- [License](#license)
- [About](#about)

---

## Introduction

Ecomora Server is designed to streamline backend ecommerce development. It leverages industry
best-practices for security, validation, and database management using Ktor, Exposed ORM, HikariCP,
and JWT authentication—all written in idiomatic Kotlin.

## Technologies Used

- [Kotlin (JVM)](https://kotlinlang.org/) — Main programming language
- [Ktor](https://ktor.io/) — Modern Kotlin framework for server APIs
- [Gradle (Kotlin DSL)](https://docs.gradle.org/current/userguide/kotlin_dsl.html) — Build and
  dependency management
- [Exposed ORM](https://github.com/JetBrains/Exposed) — ORM for SQL databases in Kotlin
- [HikariCP](https://github.com/brettwooldridge/HikariCP) — JDBC connection pool
- [PostgreSQL](https://www.postgresql.org/) _(for production)_
- [H2 Database](https://www.h2database.com/) _(for development)_
- [SLF4J](http://www.slf4j.org/) — Logging API
- [auth0/java-jwt (JWT)](https://github.com/auth0/java-jwt) — Secure JWT-based authentication
- [Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization) — JSON
  serialization/deserialization

## Features

- User authentication and robust JWT authorization
- CRUD endpoints for users, categories, products, orders, carts, promotions, services, and more
- Strong validation and error handling
- Secure, production-ready JWT security with environment configuration
- Modular structure and clean code separation (logging, validation, serialization, etc.)
- PostgreSQL for production, H2 for development (auto-configured)
- Highly configurable via environment variables
- Development-ready API collection (Postman)
- Extensible for new features and plugins


---

## Installation

Clone the repository:

```bash
git clone https://github.com/your-username/ecomora-server.git
cd ecomora-server
```

Ensure you have JDK 17+ installed. Use the included Gradle wrapper:

```bash
./gradlew build
```

_For Windows:_

```bash
gradlew.bat build
```

---

## Setup

1. **Set Environment Variables**
    - Create a `.env` or add to your environment; required variables:
        - `JWT_SECRET`, `JWT_AUDIENCE`, `JWT_DOMAIN`, `JWT_REALM`
        - `IS_PRODUCTION` (`true` uses PostgreSQL, otherwise H2 embedded)
        - If using PostgreSQL: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
        - (Optional) `H2_PATH` for custom H2 DB path
2. **Set up PostgreSQL for production:**
    - Install PGAdmin4 or another Postgres client.
    - Create a new database.
    - Set JDBC connection variables as above in your run configuration.

3. **Start the server**
   ```bash
   ./gradlew run
   # or: gradlew.bat run
   ```

---

## Usage

- Once running, the server listens on `localhost:8080` by default.
- Explore endpoints and test requests with the included Postman collection:  
  `EcomoraServer.postman_collection.json`

_Update your applications’ base URL as needed._

---

## API Endpoints

Core endpoints (replace `{id}` as needed):

### Users

- **Get all:** `GET /v1/users`
- **Get single:** `GET /v1/users/{id}`
- **Create:** `POST /v1/users`
- **Login:** `GET /v1/login`
- **Delete:** `DELETE /v1/users/{id}`
- **Update:** `PUT /v1/users/{id}`

### Categories

- **Get all:** `GET /v1/categories`
- **Get by ID:** `GET /v1/categories/{id}`
- **Create:** `POST /v1/categories`
- **Delete:** `DELETE /v1/categories/{id}`
- **Update:** `PUT /v1/categories/{id}`

### Products

- **Get all:** `GET /v1/products`
- **Get by ID:** `GET /v1/products/{id}`
- **Create:** `POST /v1/products`
- **Delete:** `DELETE /v1/products/{id}`
- **Update:** `PUT /v1/products/{id}`

### Promotions

- **Get all:** `GET /v1/promotions`
- **Get single:** `GET /v1/promotions/{id}`
- **Create:** `POST /v1/promotions`
- **Delete:** `DELETE /v1/promotions/{id}`
- **Update:** `PUT /v1/promotions/{id}`
## 

---

## Contributing

Contributions are welcome!

- Please follow Kotlin idiomatic style.
- Open issues or pull requests for bugs/features.

--
## License

This project is licensed under the MIT License.

---
## About

Ecomora is a feature-complete ecommerce server compatible with multiple frontend clients (including
Kotlin Multiplatform).
