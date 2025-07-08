# Ecomora API Documentation

## Base URL

```
http://localhost:8080
```

## Authentication

Currently no authentication required. Add JWT tokens for production use.

## Endpoints

### Categories

#### Create Category

```http
POST /v1/categories
Content-Type: multipart/form-data

Parameters:
- name (required): Category name
- description (required): Category description  
- isVisible (required): "true" or "false"
- image (optional): Image file

Response: Category object with productCount initialized to 0
```

#### Get All Categories

```http
GET /v1/categories

Response: Array of category objects
```

#### Get Category by ID

```http
GET /v1/categories/{id}

Response: Single category object
```

#### Update Category

```http
PUT /v1/categories/{id}
Content-Type: multipart/form-data

Parameters: Same as create (all optional for partial updates)
```

#### Delete Category

```http
DELETE /v1/categories/{id}

Response: Success message
```

### Products

#### Create Product

```http
POST /v1/products
Content-Type: multipart/form-data

Required Parameters:
- name: Product name
- description: Product description
- price: Product price (number)
- categoryName: Category name
- categoryId: Category ID (number)

Optional Parameters (with defaults):
- brand: Default "Unknown"
- color: Default "N/A" 
- totalStock: Default 0
- discount: Default 0
- isFeatured: Default false
- isAvailable: Default true
- image: Image file (uses default if not provided)

Response: Product object with generated ID
```

#### Get All Products

```http
GET /v1/products

Response: Array of product objects
```

#### Get Product by ID

```http
GET /v1/products/{id}

Response: Single product object
```

#### Update Product

```http
PUT /v1/products/{id}
Content-Type: multipart/form-data

Parameters: All optional for partial updates
```

#### Delete Product

```http
DELETE /v1/products/{id}

Response: Success message
```

### Services

#### Create Service

```http
POST /v1/services
Content-Type: multipart/form-data

Required Parameters:
- name: Service name
- description: Service description
- price: Service price (number)
- category: Service category
- createdAt: Creation date
- updatedAt: Update date

Optional Parameters:
- offered: Number offered (default 0)
- isVisible: Visibility (default false)
- image: Service image

Response: Service object
```

#### Get All Services

```http
GET /v1/services

Response: Array of service objects
```

#### Update Service (Partial Updates Supported)

```http
PUT /v1/services/{id}
Content-Type: multipart/form-data

Parameters: All optional - only provided fields will be updated
```

### Cart

#### Add to Cart

```http
POST /v1/cart
Content-Type: application/x-www-form-urlencoded

Parameters:
- productId (required): Product ID (number)
- userId (required): User ID (number)  
- quantity (required): Quantity (number)

Response: Cart item object
```

#### Get All Cart Items

```http
GET /v1/cart

Response: Array of cart items
```

#### Get Cart by User ID

```http
GET /v1/cart/user/{userId}

Response: Array of cart items for specific user
```

#### Update Cart Item

```http
PUT /v1/cart/{userId}

Parameters:
- cartId: Cart item ID
- productId: Product ID
- quantity: New quantity
```

#### Delete Cart Items by User

```http
DELETE /v1/cart/{userId}

Response: Deletion confirmation
```

### Promotions

#### Create Promotion

```http
POST /v1/promotions
Content-Type: multipart/form-data

Required Parameters:
- title: Promotion title
- description: Promotion description
- startDate: Start date (MM/dd/yyyy)
- endDate: End date (MM/dd/yyyy) 
- enable: "true" or "false"

Optional Parameters:
- image: Promotion image (uses default if not provided)

Response: Promotion object
```

#### Update Promotion (Partial Updates Supported)

```http
PUT /v1/promotions/{id}

Parameters: All optional - uses existing values for missing fields
```

### Prints

#### Create Print

```http
POST /v1/prints
Content-Type: multipart/form-data

Required Parameters:
- name: Print name
- description: Print description
- price: Print price (decimal)
- copies: Number of copies (integer)

Optional Parameters:
- image: Print image (uses default if not provided)

Response: Print object
```

#### Update Print (Partial Updates Supported)

```http
PUT /v1/prints/{id}

Parameters: All optional - uses existing values for missing fields
```

### Health Check

```http
GET /health

Response: Server health status
```

## Error Responses

All endpoints return appropriate HTTP status codes:

- 200: Success
- 201: Created
- 400: Bad Request (missing/invalid parameters)
- 404: Not Found
- 500: Internal Server Error

Error responses include descriptive messages for debugging.

## Notes

- All image uploads are optional and use default placeholders if not provided
- PUT endpoints support partial updates - only provided fields are updated
- File uploads are stored in `/upload/` directory structure
- Database uses PostgreSQL with proper foreign key constraints