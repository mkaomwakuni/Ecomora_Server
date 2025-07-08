# Test script for all recent fixes
$baseUrl = "http://localhost:8080"

Write-Host "Testing All Fixes..." -ForegroundColor Green

# Test 1: Create Category (should now work without productCount constraint error)
Write-Host "`n1. Testing Category Creation..." -ForegroundColor Yellow
$categoryForm = @{
    name = "Test Category $( Get-Date -Format 'HHmmss' )"
    description = "Test Description for category"
    isVisible = "true"
}

try
{
    $response = Invoke-RestMethod -Uri "$baseUrl/v1/categories" -Method POST -Form $categoryForm
    Write-Host "✓ Category created successfully: $response" -ForegroundColor Green
}
catch
{
    Write-Host "✗ Category creation failed: $( $_.Exception.Message )" -ForegroundColor Red
    Write-Host "Response: $( $_.ErrorDetails.Message )" -ForegroundColor Red
}

# Test 2: Create Product (should work with all required fields)
Write-Host "`n2. Testing Product Creation..." -ForegroundColor Yellow
$productForm = @{
    name = "Test Product $( Get-Date -Format 'HHmmss' )"
    description = "Test Product Description"
    price = "199"
    categoryName = "Electronics"
    categoryId = "1"
    brand = "TestBrand"
    color = "Blue"
    isFeatured = "false"
}

try
{
    $response = Invoke-RestMethod -Uri "$baseUrl/v1/products" -Method POST -Form $productForm
    Write-Host "✓ Product created successfully: $response" -ForegroundColor Green
}
catch
{
    Write-Host "✗ Product creation failed: $( $_.Exception.Message )" -ForegroundColor Red
    Write-Host "Response: $( $_.ErrorDetails.Message )" -ForegroundColor Red
}

# Test 3: Update Service with partial data (should now work without requiring all fields)
Write-Host "`n3. Testing Service Partial Update..." -ForegroundColor Yellow
$serviceUpdateForm = @{
    name = "Updated Service Name $( Get-Date -Format 'HHmmss' )"
    # Only providing name, not description or other fields
}

try
{
    $response = Invoke-RestMethod -Uri "$baseUrl/v1/services/1" -Method PUT -Form $serviceUpdateForm
    Write-Host "✓ Service updated successfully: $response" -ForegroundColor Green
}
catch
{
    Write-Host "✗ Service update failed: $( $_.Exception.Message )" -ForegroundColor Red
    Write-Host "Response: $( $_.ErrorDetails.Message )" -ForegroundColor Red
}

# Test 4: Add item to cart (should work with proper form data)
Write-Host "`n4. Testing Add to Cart..." -ForegroundColor Yellow
$cartForm = @{
    productId = "1"
    userId = "1"
    quantity = "2"
}

try
{
    $response = Invoke-RestMethod -Uri "$baseUrl/v1/cart" -Method POST -Form $cartForm
    Write-Host "✓ Cart item added successfully: $response" -ForegroundColor Green
}
catch
{
    Write-Host "✗ Add to cart failed: $( $_.Exception.Message )" -ForegroundColor Red
    Write-Host "Response: $( $_.ErrorDetails.Message )" -ForegroundColor Red
}

# Test 5: Get Categories (should show productCount field)
Write-Host "`n5. Testing Get Categories..." -ForegroundColor Yellow
try
{
    $response = Invoke-RestMethod -Uri "$baseUrl/v1/categories" -Method GET
    Write-Host "✓ Categories retrieved successfully. Count: $( $response.Count )" -ForegroundColor Green
    if ($response.Count -gt 0)
    {
        $firstCategory = $response[0]
        Write-Host "  First category: $( $firstCategory.name )" -ForegroundColor Cyan
        Write-Host "  ProductCount: $( $firstCategory.productCount )" -ForegroundColor Cyan
    }
}
catch
{
    Write-Host "✗ Get categories failed: $( $_.Exception.Message )" -ForegroundColor Red
}

# Test 6: Get Services
Write-Host "`n6. Testing Get Services..." -ForegroundColor Yellow
try
{
    $response = Invoke-RestMethod -Uri "$baseUrl/v1/services" -Method GET
    Write-Host "✓ Services retrieved successfully. Count: $( $response.Count )" -ForegroundColor Green
}
catch
{
    Write-Host "✗ Get services failed: $( $_.Exception.Message )" -ForegroundColor Red
}

# Test 7: Get Cart Items
Write-Host "`n7. Testing Get Cart Items..." -ForegroundColor Yellow
try
{
    $response = Invoke-RestMethod -Uri "$baseUrl/v1/cart" -Method GET
    Write-Host "✓ Cart items retrieved successfully. Count: $( $response.Count )" -ForegroundColor Green
}
catch
{
    Write-Host "✗ Get cart items failed: $( $_.Exception.Message )" -ForegroundColor Red
}

Write-Host "`nAll tests completed!" -ForegroundColor Green
Write-Host "If any tests failed, check the server logs for more details." -ForegroundColor Yellow