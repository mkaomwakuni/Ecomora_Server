# Comprehensive Ecomora API Test Script
# Tests all endpoints with GET, POST, PUT, DELETE operations

$baseUrl = "http://localhost:8080"
$results = @{ }

function Test-Endpoint
{
    param(
        [string]$Method,
        [string]$Url,
        [string]$Body = "",
        [string]$ContentType = "application/x-www-form-urlencoded",
        [string]$TestName
    )

    Write-Host "`n=== $TestName ===" -ForegroundColor Cyan
    Write-Host "$Method $Url" -ForegroundColor Yellow

    try
    {
        $headers = @{ }
        if ($ContentType)
        {
            $headers["Content-Type"] = $ContentType
        }

        if ($Method -eq "GET")
        {
            $response = Invoke-RestMethod -Uri $Url -Method $Method -TimeoutSec 30
        }
        else
        {
            $response = Invoke-RestMethod -Uri $Url -Method $Method -Body $Body -Headers $headers -TimeoutSec 30
        }

        Write-Host "✅ SUCCESS" -ForegroundColor Green
        $jsonResponse = $response | ConvertTo-Json -Depth 10
        Write-Host $jsonResponse -ForegroundColor Gray

        $results[$TestName] = @{
            "status" = "SUCCESS"
            "method" = $Method
            "url" = $Url
            "response" = $response
        }

        return $response
    }
    catch
    {
        Write-Host "❌ ERROR: $( $_.Exception.Message )" -ForegroundColor Red
        $results[$TestName] = @{
            "status" = "ERROR"
            "method" = $Method
            "url" = $Url
            "error" = $_.Exception.Message
        }
        return $null
    }
}

Write-Host "🚀 Starting Comprehensive API Testing..." -ForegroundColor Green

# 1. HEALTH & BASIC ENDPOINTS
Test-Endpoint -Method "GET" -Url "$baseUrl/health" -TestName "Health_Check"
Test-Endpoint -Method "GET" -Url "$baseUrl/" -TestName "Home_Page"
Test-Endpoint -Method "GET" -Url "$baseUrl/test/data" -TestName "Test_Data_Counts"

# 2. USER AUTHENTICATION & MANAGEMENT
Write-Host "`n🔐 TESTING USER ENDPOINTS" -ForegroundColor Magenta

# Get all users (GET)
Test-Endpoint -Method "GET" -Url "$baseUrl/api/v1/users" -TestName "Users_GetAll"

# Get user by ID (GET)
Test-Endpoint -Method "GET" -Url "$baseUrl/api/v1/users/1" -TestName "Users_GetById"

# User Registration (POST)
$registerBody = "email=newuser$( Get-Random )@test.com" + "&" + "password=password123" + "&" + "userName=newuser$( Get-Random )" + "&" + "fullName=New Test User" + "&" + "phoneNumber=+1234567899" + "&" + "userRole=user"
$newUser = Test-Endpoint -Method "POST" -Url "$baseUrl/api/v1/auth/register" -Body $registerBody -TestName "Users_Register"

# User Login (POST)
$loginBody = "email=testuser1@example.com" + "&" + "password=password123"
Test-Endpoint -Method "POST" -Url "$baseUrl/api/v1/auth/login" -Body $loginBody -TestName "Users_Login"

# 3. CATEGORY MANAGEMENT
Write-Host "`n📂 TESTING CATEGORY ENDPOINTS" -ForegroundColor Magenta

# Get all categories (GET)
Test-Endpoint -Method "GET" -Url "$baseUrl/v1/categories" -TestName "Categories_GetAll"

# Get category by ID (GET)
Test-Endpoint -Method "GET" -Url "$baseUrl/v1/categories/1" -TestName "Categories_GetById"

# 4. PRODUCT MANAGEMENT
Write-Host "`n🛍️ TESTING PRODUCT ENDPOINTS" -ForegroundColor Magenta

# Get all products (GET)
Test-Endpoint -Method "GET" -Url "$baseUrl/v1/products" -TestName "Products_GetAll"

# Get product by ID (GET)
Test-Endpoint -Method "GET" -Url "$baseUrl/v1/products/1" -TestName "Products_GetById"

# Get multiple products (GET)
Test-Endpoint -Method "GET" -Url "$baseUrl/v1/products/multiple?ids=1,2,3" -TestName "Products_GetMultiple"

# Get products by user ID (GET)
Test-Endpoint -Method "GET" -Url "$baseUrl/v1/products/userId/1,2" -TestName "Products_GetByUserIds"

# 5. SERVICE MANAGEMENT
Write-Host "`n⚙️ TESTING SERVICE ENDPOINTS" -ForegroundColor Magenta

# Get all services (GET)
Test-Endpoint -Method "GET" -Url "$baseUrl/v1/services" -TestName "Services_GetAll"

# Get service by ID (GET)
Test-Endpoint -Method "GET" -Url "$baseUrl/v1/services/1" -TestName "Services_GetById"

# 6. PROMOTION MANAGEMENT
Write-Host "`n🎯 TESTING PROMOTION ENDPOINTS" -ForegroundColor Magenta

# Get all promotions (GET)
Test-Endpoint -Method "GET" -Url "$baseUrl/v1/promotions" -TestName "Promotions_GetAll"

# Get promotion by ID (GET)
Test-Endpoint -Method "GET" -Url "$baseUrl/v1/promotions/1" -TestName "Promotions_GetById"

# 7. PRINT SERVICES
Write-Host "`n🖨️ TESTING PRINT ENDPOINTS" -ForegroundColor Magenta

# Get all prints (GET)
Test-Endpoint -Method "GET" -Url "$baseUrl/v1/prints" -TestName "Prints_GetAll"

# Get print by ID (GET)
Test-Endpoint -Method "GET" -Url "$baseUrl/v1/prints/1" -TestName "Prints_GetById"

# 8. CART MANAGEMENT
Write-Host "`n🛒 TESTING CART ENDPOINTS" -ForegroundColor Magenta

# Get all cart items (GET)
Test-Endpoint -Method "GET" -Url "$baseUrl/v1/cart" -TestName "Cart_GetAll"

# Add item to cart (POST)
$cartBody = "productId=1" + "&" + "userId=1" + "&" + "quantity=2"
Test-Endpoint -Method "POST" -Url "$baseUrl/v1/cart" -Body $cartBody -TestName "Cart_AddItem"

# Get cart by user ID (GET)
Test-Endpoint -Method "GET" -Url "$baseUrl/v1/cart/1" -TestName "Cart_GetByUserId"

# Get cart items by user (GET)
Test-Endpoint -Method "GET" -Url "$baseUrl/v1/cart/user/1" -TestName "Cart_GetItemsByUser"

# Update cart item (PUT)
$updateCartBody = "cartId=1" + "&" + "productId=1" + "&" + "quantity=3"
Test-Endpoint -Method "PUT" -Url "$baseUrl/v1/cart/1" -Body $updateCartBody -TestName "Cart_UpdateItem"

# 9. ORDER MANAGEMENT
Write-Host "`n📦 TESTING ORDER ENDPOINTS" -ForegroundColor Magenta

# Get all orders (GET)
Test-Endpoint -Method "GET" -Url "$baseUrl/v1/order" -TestName "Orders_GetAll"

# Create new order (POST)
$orderBody = "userId=1" + "&" + "indicatorColor=blue" + "&" + "productIds=1" + "&" + "totalQuantity=2" + "&" + "totalSum=1798" + "&" + "paymentType=Credit Card"
$newOrder = Test-Endpoint -Method "POST" -Url "$baseUrl/v1/order" -Body $orderBody -TestName "Orders_Create"

# Get orders by user ID (GET)
Test-Endpoint -Method "GET" -Url "$baseUrl/v1/order/userId/1" -TestName "Orders_GetByUserId"

# Get order by ID (GET)
Test-Endpoint -Method "GET" -Url "$baseUrl/v1/order/1" -TestName "Orders_GetById"

# Update order status (PUT)
if ($newOrder -and $newOrder.id)
{
    $updateOrderBody = "orderProgress=Shipped"
    Test-Endpoint -Method "PUT" -Url "$baseUrl/v1/order/$( $newOrder.id )" -Body $updateOrderBody -TestName "Orders_UpdateStatus"
}

# 10. DELETE OPERATIONS (Testing Delete endpoints)
Write-Host "`n🗑️ TESTING DELETE ENDPOINTS" -ForegroundColor Magenta

# Delete cart item (DELETE)
Test-Endpoint -Method "DELETE" -Url "$baseUrl/v1/cart/item/1" -TestName "Cart_DeleteItem"

# Delete cart by user (DELETE)
Test-Endpoint -Method "DELETE" -Url "$baseUrl/v1/cart/2" -TestName "Cart_DeleteByUser"

# SUMMARY
Write-Host "`n📊 TEST SUMMARY" -ForegroundColor Green
$successCount = ($results.Values | Where-Object { $_.status -eq "SUCCESS" }).Count
$errorCount = ($results.Values | Where-Object { $_.status -eq "ERROR" }).Count
$totalTests = $results.Count

Write-Host "Total Tests: $totalTests" -ForegroundColor Yellow
Write-Host "Successful: $successCount" -ForegroundColor Green
Write-Host "Failed: $errorCount" -ForegroundColor Red

# Save detailed results to JSON file
$jsonResults = $results | ConvertTo-Json -Depth 10
$jsonResults | Out-File -FilePath "api-test-results.json" -Encoding UTF8
Write-Host "`n💾 Detailed results saved to 'api-test-results.json'" -ForegroundColor Cyan

# Display failed tests
if ($errorCount -gt 0)
{
    Write-Host "`n❌ FAILED TESTS:" -ForegroundColor Red
    $results.GetEnumerator() | Where-Object { $_.Value.status -eq "ERROR" } | ForEach-Object {
        Write-Host "- $( $_.Key ): $( $_.Value.error )" -ForegroundColor Red
    }
}

Write-Host "`n✅ Testing completed!" -ForegroundColor Green