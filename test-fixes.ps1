# Test script for API fixes
$baseUrl = "http://localhost:8080"

Write-Host "Testing API Endpoints..." -ForegroundColor Green

# Test 1: Create Category without image
Write-Host "`n1. Testing Category Creation without image..." -ForegroundColor Yellow
$categoryBody = @{
    name = "Test Category"
    description = "Test Description"
    isVisible = "true"
}

try
{
    $response = Invoke-RestMethod -Uri "$baseUrl/v1/categories" -Method POST -Form $categoryBody
    Write-Host "✓ Category created successfully" -ForegroundColor Green
}
catch
{
    Write-Host "✗ Category creation failed: $( $_.Exception.Message )" -ForegroundColor Red
}

# Test 2: Create Product with minimal fields
Write-Host "`n2. Testing Product Creation with minimal fields..." -ForegroundColor Yellow
$productBody = @{
    name = "Test Product"
    description = "Test Product Description"
    price = "100"
    categoryName = "Electronics"
    categoryId = "1"
}

try
{
    $response = Invoke-RestMethod -Uri "$baseUrl/v1/products" -Method POST -Form $productBody
    Write-Host "✓ Product created successfully" -ForegroundColor Green
}
catch
{
    Write-Host "✗ Product creation failed: $( $_.Exception.Message )" -ForegroundColor Red
}

# Test 3: Create Service without image
Write-Host "`n3. Testing Service Creation without image..." -ForegroundColor Yellow
$serviceBody = @{
    name = "Test Service"
    description = "Test Service Description"
    price = "50"
    category = "Consulting"
    createdAt = "2024-01-01"
    updatedAt = "2024-01-01"
}

try
{
    $response = Invoke-RestMethod -Uri "$baseUrl/v1/services" -Method POST -Form $serviceBody
    Write-Host "✓ Service created successfully" -ForegroundColor Green
}
catch
{
    Write-Host "✗ Service creation failed: $( $_.Exception.Message )" -ForegroundColor Red
}

# Test 4: Create Promotion without image
Write-Host "`n4. Testing Promotion Creation without image..." -ForegroundColor Yellow
$promotionBody = @{
    title = "Test Promotion"
    description = "Test Promotion Description"
    startDate = "01/01/2024"
    endDate = "12/31/2024"
    enable = "true"
}

try
{
    $response = Invoke-RestMethod -Uri "$baseUrl/v1/promotions" -Method POST -Form $promotionBody
    Write-Host "✓ Promotion created successfully" -ForegroundColor Green
}
catch
{
    Write-Host "✗ Promotion creation failed: $( $_.Exception.Message )" -ForegroundColor Red
}

# Test 5: Create Print without image
Write-Host "`n5. Testing Print Creation without image..." -ForegroundColor Yellow
$printBody = @{
    name = "Test Print"
    description = "Test Print Description"
    price = "25.50"
    copies = "10"
}

try
{
    $response = Invoke-RestMethod -Uri "$baseUrl/v1/prints" -Method POST -Form $printBody
    Write-Host "✓ Print created successfully" -ForegroundColor Green
}
catch
{
    Write-Host "✗ Print creation failed: $( $_.Exception.Message )" -ForegroundColor Red
}

# Test 6: Health Check
Write-Host "`n6. Testing Health Check..." -ForegroundColor Yellow
try
{
    $response = Invoke-RestMethod -Uri "$baseUrl/health" -Method GET
    Write-Host "✓ Health check successful: $( $response.status )" -ForegroundColor Green
}
catch
{
    Write-Host "✗ Health check failed: $( $_.Exception.Message )" -ForegroundColor Red
}

Write-Host "`nTest completed!" -ForegroundColor Green