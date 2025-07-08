# Routes Directory

This directory contains all the route definitions for the Ecomora Server API, organized by
functional modules.

## Structure

The routes have been split into separate files for better organization and maintainability:

- **UserRoutes.kt** - User authentication and management routes
    - `/api/v1/auth/register` - User registration
    - `/api/v1/auth/login` - User login
    - `/api/v1/users/*` - User CRUD operations

- **CategoryRoutes.kt** - Product category management routes
    - `/v1/categories` - Category CRUD operations
    - Supports image uploads and category visibility settings

- **ProductRoutes.kt** - Product management routes
    - `/v1/products` - Product CRUD operations
    - Supports multiple products queries, image uploads, and product features

- **ServiceRoutes.kt** - Service management routes
    - `/v1/services` - Service CRUD operations
    - Supports service offerings and visibility settings

- **PromotionRoutes.kt** - Promotion and campaign management routes
    - `/v1/promotions` - Promotion CRUD operations
    - Supports date-based promotions and image uploads

- **PrintRoutes.kt** - Print service management routes
    - `/v1/prints` - Print CRUD operations
    - Supports copy quantities and pricing

- **CartRoutes.kt** - Shopping cart management routes
    - `/v1/cart` - Cart item management
    - Supports user-specific cart operations

- **OrderRoutes.kt** - Order management routes
    - `/v1/order` - Order CRUD operations
    - Supports order tracking and status updates

## Usage

Routes are automatically registered in
`server/src/main/kotlin/est/ecomora/server/plugins/Routing.kt` using extension functions:

```kotlin
routing {
    userRoutes(usersRepository)
    categoryRoutes(categoriesRepository)
    productRoutes(productsRepository)
    // ... other routes
}
```

## API Conventions

- All routes use standard HTTP methods (GET, POST, PUT, DELETE)
- File uploads are handled via multipart form data
- Error responses include appropriate HTTP status codes
- JSON responses are used for data exchange
- Path parameters are used for resource identification

## Dependencies

Each route file imports its respective repository implementation and required Ktor components:

- Repository implementations for data access
- HTTP status codes and content types
- Request/response handling utilities
- File upload handling capabilities