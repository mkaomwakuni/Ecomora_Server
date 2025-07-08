# Ecomora Server

A secure, scalable e-commerce backend API built with Ktor and Kotlin.

## 🔒 Security Features

### Authentication & Authorization

- **JWT-based Authentication**: Secure token-based authentication system
- **Environment Variable Configuration**: JWT secrets and configuration loaded from environment
  variables
- **Role-based Access Control**: Admin, Moderator, and User roles with proper permission handling
- **Secure Password Hashing**: PBKDF2 with salt for password storage (100,000 iterations)
- **Session Management**: Secure HTTP-only cookies with SameSite protection

### API Security

- **Input Validation**: Comprehensive validation for all user inputs
- **Error Handling**: Structured error responses without information leakage
- **Request/Response Logging**: Security event logging for audit trails
- **CORS Protection**: Configurable CORS settings for production deployment

## 🏗️ Architecture

### Modular Design

- **Separated Route Files**: Clean separation of concerns with dedicated route modules
- **Service Layer**: Business logic separated from controllers
- **Repository Pattern**: Data access layer abstraction
- **Plugin System**: Modular configuration using Ktor plugins

### Core Components

```
src/main/kotlin/est/ecomora/server/
├── routes/           # API route definitions
│   └── UserRoutes.kt
├── plugins/          # Ktor plugins and middleware
│   ├── Security.kt   # Authentication & authorization
│   ├── Validation.kt # Input validation middleware
│   ├── Authorization.kt # Role-based access control
│   └── Logging.kt    # Comprehensive logging
├── domain/
│   ├── service/      # Business logic services
│   │   ├── JwtService.kt
│   │   └── PasswordService.kt
│   └── repository/   # Data access layer
└── data/local/table/ # Database schema definitions
```

## 🚀 Getting Started

### Prerequisites

- JDK 21+
- PostgreSQL 12+
- Gradle 8+

### Environment Setup

1. Copy `.env.example` to `.env`
2. Configure your environment variables:

```bash
# Required for production
JWT_SECRET=your-super-secret-jwt-key-min-256-bits
JDBC_DATABASE_URL=jdbc:postgresql://localhost:5432/ecomora
DB_USERNAME=your_db_username
DB_PASSWORD=your_db_password

# Optional (have defaults)
JWT_AUDIENCE=ecomora-api
JWT_DOMAIN=https://yourserver.com/
SERVER_PORT=8080
```

### Running the Server

```bash
# Development
./gradlew run

# Production build
./gradlew shadowJar
java -jar build/libs/ecomora-server-1.0.0-all.jar
```

### Running Tests

```bash
./gradlew test
```

## 📋 API Documentation

### Authentication Endpoints

#### Register User

```http
POST /api/v1/auth/register
Content-Type: application/x-www-form-urlencoded

email=user@example.com&password=SecurePass123&userName=johndoe&fullName=John Doe&phoneNumber=+1234567890&userRole=user
```

#### Login

```http
POST /api/v1/auth/login
Content-Type: application/x-www-form-urlencoded

email=user@example.com&password=SecurePass123
```

**Response:**

```json
{
  "message": "Login successful",
  "user": {
    "id": 1,
    "username": "johndoe",
    "email": "user@example.com",
    "userRole": "user",
    "fullName": "John Doe",
    "phoneNumber": "+1234567890"
  },
  "token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9..."
}
```

### User Management Endpoints

#### Get All Users

```http
GET /api/v1/users
Authorization: Bearer {jwt_token}
```

#### Get User by ID

```http
GET /api/v1/users/{id}
Authorization: Bearer {jwt_token}
```

#### Delete User

```http
DELETE /api/v1/users/{id}
Authorization: Bearer {jwt_token}
```

### Health Check

```http
GET /health
```

## 🔧 Configuration

### Security Configuration

All security settings are controlled via environment variables:

- `JWT_SECRET`: Secret key for JWT signing (minimum 256 bits)
- `JWT_EXPIRATION_HOURS`: Token expiration time (default: 24 hours)
- `JWT_AUDIENCE`: Token audience claim
- `JWT_DOMAIN`: Token issuer claim

### Database Configuration

- `JDBC_DATABASE_URL`: PostgreSQL connection string
- `DB_USERNAME`: Database username
- `DB_PASSWORD`: Database password

## 🛡️ Security Best Practices Implemented

1. **Password Security**
    - PBKDF2 hashing with 100,000 iterations
    - Unique salt per password
    - Passwords never returned in API responses

2. **JWT Security**
    - Configurable secret keys via environment variables
    - Proper token expiration
    - Audience and issuer validation

3. **Input Validation**
    - Email format validation
    - Password strength requirements
    - Username format validation
    - Phone number validation

4. **Logging & Monitoring**
    - Security event logging
    - Failed authentication attempts tracking
    - API request/response logging
    - Database operation logging

5. **Error Handling**
    - Structured error responses
    - No sensitive information leakage
    - Proper HTTP status codes

## 🚦 API Versioning

All APIs are versioned using URL path versioning:

- Current version: `v1`
- Base path: `/api/v1/`

## 📊 Logging

The application includes comprehensive logging:

- **Security Logger**: Authentication, authorization events
- **Database Logger**: Database operations and errors
- **API Logger**: Request/response logging with timing
- **Application Logger**: General application events

Log levels can be configured via environment variables.

## 🧪 Testing

The project includes:

- Unit tests for core functionality
- Integration tests for API endpoints
- Security tests for authentication

Run tests with:

```bash
./gradlew test
```

## 🔄 Development vs Production

### Development Mode

- Uses default JWT secret with warning
- Enables detailed error messages
- Allows HTTP cookies (non-secure)

### Production Mode

- Requires all environment variables
- Secure cookie settings
- Minimal error information exposure

## 🚀 Deployment

### Docker Deployment (Recommended)

```dockerfile
FROM openjdk:21-jdk
COPY build/libs/ecomora-server-1.0.0-all.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
```

### Environment Variables for Production

Ensure all security-related environment variables are properly set:

- Never use default JWT secrets
- Use strong database passwords
- Configure proper CORS settings
- Set up SSL/TLS termination

## 📈 Future Enhancements

- [ ] Rate limiting middleware
- [ ] API key authentication for external integrations
- [ ] OAuth2 integration
- [ ] Advanced audit logging
- [ ] Database connection pooling optimization
- [ ] Caching layer implementation
- [ ] Comprehensive API documentation with OpenAPI/Swagger

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Implement changes with tests
4. Ensure all security checks pass
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

For support and questions:

- Create an issue in the GitHub repository
- Check the documentation in the `/docs` directory
- Review the API collection in `Ecomora_API_Collection.json`