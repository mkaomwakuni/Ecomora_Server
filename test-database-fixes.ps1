# Test script for database fixes
$baseUrl = "http://localhost:8080"

Write-Host "Testing Database Fixes..." -ForegroundColor Green

# Test 1: Create Category (should now work without productCount issues)
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
}

# Test 2: Create Product (should now work with all required fields)
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
}

# Test 3: Get Categories (should show productCount field)
Write-Host "`n3. Testing Get Categories..." -ForegroundColor Yellow
try
{
    $response = Invoke-RestMethod -Uri "$baseUrl/v1/categories" -Method GET
    Write-Host "✓ Categories retrieved successfully. Count: $( $response.Count )" -ForegroundColor Green
    if ($response.Count -gt 0)
    {
        Write-Host "  First category: $( $response[0].name ) (productCount: $( $response[0].productCount ))" -ForegroundColor Cyan
    }
}
catch
{
    Write-Host "✗ Get categories failed: $( $_.Exception.Message )" -ForegroundColor Red
}

# Test 4: Get Products (should show all fields including sold)
Write-Host "`n4. Testing Get Products..." -ForegroundColor Yellow
try
{
    $response = Invoke-RestMethod -Uri "$baseUrl/v1/products" -Method GET
    Write-Host "✓ Products retrieved successfully. Count: $( $response.Count )" -ForegroundColor Green
    if ($response.Count -gt 0)
    {
        Write-Host "  First product: $( $response[0].name ) (sold: $( $response[0].sold ))" -ForegroundColor Cyan
    }
}
catch
{
    Write-Host "✗ Get products failed: $( $_.Exception.Message )" -ForegroundColor Red
}

Write-Host "`nDatabase fixes test completed!" -ForegroundColor Green