# Ecomora Server API Endpoint Testing Script
# This script tests all the API endpoints with various HTTP methods

$baseUrl = "http://localhost:8080"
$headers = @{ "Content-Type" = "application/x-www-form-urlencoded" }

Write-Host "=== Ecomora Server API Endpoint Testing ===" -ForegroundColor Green
Write-Host "Base URL: $baseUrl" -ForegroundColor Yellow

# Function to make HTTP requests with error handling
function Test-Endpoint
{
    param(
        [string]$Method,
        [string]$Url,
        [hashtable]$Body = @{ },
        [string]$Description
    )

    Write-Host "`n--- Testing: $Description ---" -ForegroundColor Cyan
    Write-Host "$Method $Url" -ForegroundColor White

    try
    {
        if ($Method -eq "GET")
        {
            $response = Invoke-RestMethod -Uri $Url -Method $Method -ErrorAction Stop
        }
        else
        {
            $bodyString = ($Body.GetEnumerator() | ForEach-Object { "$( $_.Key )=$( $_.Value )" }) -join '&'
            $response = Invoke-RestMethod -Uri $Url -Method $Method -Body $bodyString -Headers $headers -ErrorAction Stop
        }

        Write-Host "✅ SUCCESS:" -ForegroundColor Green
        Write-Host ($response | ConvertTo-Json -Depth 3) -ForegroundColor Gray
        return $response
    }
    catch
    {
        Write-Host "❌ ERROR:" -ForegroundColor Red
        Write-Host $_.Exception.Message -ForegroundColor Red
        return $null
    }
}

# Wait for server to be ready
Write-Host "`nWaiting for server to start..." -ForegroundColor Yellow
Start-Sleep -Seconds 5

# Test Health Check
Test-Endpoint -Method "GET" -Url "$baseUrl/health" -Description "Health Check"

# Test Home Page
Test-Endpoint -Method "GET" -Url "$baseUrl/" -Description "Home Page"

# Test Documentation
Test-Endpoint -Method "GET" -Url "$baseUrl/test/endpoints" -Description "Test Endpoints Documentation"

# Test Data Counts
Test-Endpoint -Method "GET" -Url "$baseUrl/test/data" -Description "Test Data Counts"

Write-Host "`n=== USER ENDPOINTS ===" -ForegroundColor Magenta

# Test User Registration
$registerData = @{
    email = "newuser@test.com"
    password = "password123"
    userName = "newuser"
    fullName = "New Test User"
    phoneNumber = "+1234567899"
    userRole = "user"
}
$newUser = Test-Endpoint -Method "POST" -Url "$baseUrl/api/v1/auth/register" -Body $registerData -Description "User Registration"

# Test User Login
$loginData = @{
    email = "testuser1@example.com"
    password = "password123"
}
$loginResponse = Test-Endpoint -Method "POST" -Url "$baseUrl/api/v1/auth/login" -Body $loginData -Description "User Login"

# Test Get All Users
Test-Endpoint -Method "GET" -Url "$baseUrl/api/v1/users" -Description "Get All Users"

# Test Get User by ID
Test-Endpoint -Method "GET" -Url "$baseUrl/api/v1/users/1" -Description "Get User by ID"

Write-Host "`n=== CATEGORY ENDPOINTS ===" -ForegroundColor Magenta

# Test Get All Categories
Test-Endpoint -Method "GET" -Url "$baseUrl/v1/categories" -Description "Get All Categories"

# Test Get Category by ID
Test-Endpoint -Method "GET" -Url "$baseUrl/v1/categories/1" -Description "Get Category by ID"

Write-Host "`n=== PRODUCT ENDPOINTS ===" -ForegroundColor Magenta

# Test Get All Products
Test-Endpoint -Method "GET" -Url "$baseUrl/v1/products" -Description "Get All Products"

# Test Get Product by ID
Test-Endpoint -Method "GET" -Url "$baseUrl/v1/products/1" -Description "Get Product by ID"

# Test Get Multiple Products
Test-Endpoint -Method "GET" -Url "$baseUrl/v1/products/multiple?ids=1,2" -Description "Get Multiple Products"

Write-Host "`n=== SERVICE ENDPOINTS ===" -ForegroundColor Magenta

# Test Get All Services
Test-Endpoint -Method "GET" -Url "$baseUrl/v1/services" -Description "Get All Services"

# Test Get Service by ID
Test-Endpoint -Method "GET" -Url "$baseUrl/v1/services/1" -Description "Get Service by ID"

Write-Host "`n=== PROMOTION ENDPOINTS ===" -ForegroundColor Magenta

# Test Get All Promotions
Test-Endpoint -Method "GET" -Url "$baseUrl/v1/promotions" -Description "Get All Promotions"

# Test Get Promotion by ID
Test-Endpoint -Method "GET" -Url "$baseUrl/v1/promotions/1" -Description "Get Promotion by ID"

Write-Host "`n=== PRINT ENDPOINTS ===" -ForegroundColor Magenta

# Test Get All Prints
Test-Endpoint -Method "GET" -Url "$baseUrl/v1/prints" -Description "Get All Prints"

# Test Get Print by ID
Test-Endpoint -Method "GET" -Url "$baseUrl/v1/prints/1" -Description "Get Print by ID"

Write-Host "`n=== CART ENDPOINTS ===" -ForegroundColor Magenta

# Test Add to Cart
$cartData = @{
    productId = "1"
    userId = "1"
    quantity = "2"
}
Test-Endpoint -Method "POST" -Url "$baseUrl/v1/cart" -Body $cartData -Description "Add Item to Cart"

# Test Get All Cart Items
Test-Endpoint -Method "GET" -Url "$baseUrl/v1/cart" -Description "Get All Cart Items"

# Test Get Cart Items by User ID
Test-Endpoint -Method "GET" -Url "$baseUrl/v1/cart/1" -Description "Get Cart Items by User ID"

Write-Host "`n=== ORDER ENDPOINTS ===" -ForegroundColor Magenta

# Test Create Order
$orderData = @{
    userId = "1"
    indicatorColor = "blue"
    productIds = "1"
    totalQuantity = "2"
    totalSum = "1798"
    paymentType = "Credit Card"
}
$newOrder = Test-Endpoint -Method "POST" -Url "$baseUrl/v1/order" -Body $orderData -Description "Create Order"

# Test Get All Orders
Test-Endpoint -Method "GET" -Url "$baseUrl/v1/order" -Description "Get All Orders"

# Test Get Order by ID
Test-Endpoint -Method "GET" -Url "$baseUrl/v1/order/1" -Description "Get Order by ID"

# Test Get Orders by User ID
Test-Endpoint -Method "GET" -Url "$baseUrl/v1/order/userId/1" -Description "Get Orders by User ID"

# Test Update Order Status
if ($newOrder -and $newOrder.id)
{
    $updateOrderData = @{
        orderProgress = "Shipped"
    }
    Test-Endpoint -Method "PUT" -Url "$baseUrl/v1/order/$( $newOrder.id )" -Body $updateOrderData -Description "Update Order Status"
}

Write-Host "`n=== TESTING SUMMARY ===" -ForegroundColor Green
Write-Host "All endpoint tests completed!" -ForegroundColor Yellow
Write-Host "Check the results above for any failures." -ForegroundColor Yellow

Write-Host "`n=== SAMPLE CURL COMMANDS ===" -ForegroundColor Magenta
Write-Host @"
# User Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "email=testuser1@example.com&password=password123"

# Get All Products
curl -X GET http://localhost:8080/v1/products

# Add to Cart
curl -X POST http://localhost:8080/v1/cart \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "productId=1&userId=1&quantity=2"

# Create Order
curl -X POST http://localhost:8080/v1/order \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "userId=1&indicatorColor=blue&productIds=1&totalQuantity=2&totalSum=1798&paymentType=Credit Card"
"@ -ForegroundColor Gray