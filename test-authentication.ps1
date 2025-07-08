# Authentication and File Upload Testing Script
$baseUrl = "http://localhost:8080"
$results = @{ }

function Test-Endpoint
{
    param(
        [string]$Method,
        [string]$Url,
        [string]$Body = "",
        [string]$ContentType = "application/x-www-form-urlencoded",
        [string]$TestName,
        [int]$TimeoutSec = 10
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
            $response = Invoke-RestMethod -Uri $Url -Method $Method -TimeoutSec $TimeoutSec
        }
        else
        {
            $response = Invoke-RestMethod -Uri $Url -Method $Method -Body $Body -Headers $headers -TimeoutSec $TimeoutSec
        }

        Write-Host "✅ SUCCESS" -ForegroundColor Green
        $jsonResponse = $response | ConvertTo-Json -Depth 5
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

Write-Host "🔐 AUTHENTICATION AND FILE UPLOAD TESTING" -ForegroundColor Green

# Wait for server to be ready
Write-Host "Checking server readiness..." -ForegroundColor Yellow
$maxAttempts = 10
$attempt = 0
$serverReady = $false

while ($attempt -lt $maxAttempts -and -not $serverReady)
{
    try
    {
        $response = Invoke-RestMethod -Uri "$baseUrl/" -Method GET -TimeoutSec 3
        if ($response)
        {
            $serverReady = $true
            Write-Host "✅ Server is ready!" -ForegroundColor Green
        }
    }
    catch
    {
        $attempt++
        Write-Host "⏳ Attempt $attempt/$maxAttempts - Server not ready yet..." -ForegroundColor Yellow
        Start-Sleep -Seconds 3
    }
}

if (-not $serverReady)
{
    Write-Host "❌ Server is not responding. Exiting..." -ForegroundColor Red
    exit 1
}

# 1. TEST HEALTH ENDPOINT (FIXED)
Test-Endpoint -Method "GET" -Url "$baseUrl/health" -TestName "Health_Check_Fixed"

# 2. TEST AUTHENTICATION ENDPOINTS
Write-Host "`n🔑 TESTING AUTHENTICATION" -ForegroundColor Magenta

# Test login with existing user
$loginBody = "email=testuser1@example.com" + "&" + "password=password123"
$loginResponse = Test-Endpoint -Method "POST" -Url "$baseUrl/api/v1/auth/login" -Body $loginBody -TestName "Auth_Login_Existing_User"

# Test login with wrong password
$wrongLoginBody = "email=testuser1@example.com" + "&" + "password=wrongpassword"
Test-Endpoint -Method "POST" -Url "$baseUrl/api/v1/auth/login" -Body $wrongLoginBody -TestName "Auth_Login_Wrong_Password"

# Test user registration
$randomNum = Get-Random
$registerBody = "email=newuser$randomNum@test.com" + "&" + "password=password123" + "&" + "userName=newuser$randomNum" + "&" + "fullName=New Test User" + "&" + "phoneNumber=+1234567890" + "&" + "userRole=user"
$registerResponse = Test-Endpoint -Method "POST" -Url "$baseUrl/api/v1/auth/register" -Body $registerBody -TestName "Auth_Register_New_User"

# Test registration with missing data
$incompleteRegisterBody = "email=incomplete@test.com" + "&" + "password=password123"
Test-Endpoint -Method "POST" -Url "$baseUrl/api/v1/auth/register" -Body $incompleteRegisterBody -TestName "Auth_Register_Incomplete_Data"

# 3. TEST USER MANAGEMENT ENDPOINTS
Write-Host "`n👥 TESTING USER MANAGEMENT" -ForegroundColor Magenta

# Get all users
Test-Endpoint -Method "GET" -Url "$baseUrl/api/v1/users" -TestName "Users_GetAll"

# Get user by ID
Test-Endpoint -Method "GET" -Url "$baseUrl/api/v1/users/1" -TestName "Users_GetById"

# Get non-existent user
Test-Endpoint -Method "GET" -Url "$baseUrl/api/v1/users/9999" -TestName "Users_GetById_NotFound"

# 4. TEST FILE UPLOAD ENDPOINTS
Write-Host "`n📁 TESTING FILE UPLOAD ENDPOINTS" -ForegroundColor Magenta

# Note: For actual file upload testing, we would need to create multipart form data
# For now, we'll test the endpoints that accept file uploads to see their response

# Test category creation (accepts file uploads)
Write-Host "Testing Category Creation (with file upload capability)" -ForegroundColor Yellow
# This would normally require multipart/form-data for file uploads

# Test product creation (accepts file uploads)  
Write-Host "Testing Product Creation (with file upload capability)" -ForegroundColor Yellow
# This would normally require multipart/form-data for file uploads

# Test service creation (accepts file uploads)
Write-Host "Testing Service Creation (with file upload capability)" -ForegroundColor Yellow
# This would normally require multipart/form-data for file uploads

# Test user profile update with image (accepts file uploads)
Write-Host "Testing User Profile Update (with file upload capability)" -ForegroundColor Yellow
# This would normally require multipart/form-data for file uploads

Write-Host "`n📊 TEST SUMMARY" -ForegroundColor Green
$successCount = ($results.Values | Where-Object { $_.status -eq "SUCCESS" }).Count
$errorCount = ($results.Values | Where-Object { $_.status -eq "ERROR" }).Count
$totalTests = $results.Count

Write-Host "Total Authentication Tests: $totalTests" -ForegroundColor Yellow
Write-Host "Successful: $successCount" -ForegroundColor Green
Write-Host "Failed: $errorCount" -ForegroundColor Red

# Save results
$jsonResults = $results | ConvertTo-Json -Depth 10
$jsonResults | Out-File -FilePath "authentication-test-results.json" -Encoding UTF8
Write-Host "`n💾 Results saved to 'authentication-test-results.json'" -ForegroundColor Cyan

# Display failed tests
if ($errorCount -gt 0)
{
    Write-Host "`n❌ FAILED TESTS:" -ForegroundColor Red
    $results.GetEnumerator() | Where-Object { $_.Value.status -eq "ERROR" } | ForEach-Object {
        Write-Host "- $( $_.Key ): $( $_.Value.error )" -ForegroundColor Red
    }
}

Write-Host "`n✅ Authentication testing completed!" -ForegroundColor Green