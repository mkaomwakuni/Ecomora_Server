# Render Deployment Script for Ecomora Server (PowerShell)
# This script helps you deploy your Ecomora server to Render

$ErrorActionPreference = "Stop"

Write-Host "🚀 Ecomora Server - Render Deployment" -ForegroundColor Green
Write-Host "======================================" -ForegroundColor Green

# Check if we're in the right directory
if (!(Test-Path "render.yaml"))
{
    Write-Host "❌ Error: render.yaml not found. Please run this script from the project root." -ForegroundColor Red
    exit 1
}

# Check if we have a git repository
if (!(Test-Path ".git"))
{
    Write-Host "❌ Error: This is not a git repository. Please initialize git first:" -ForegroundColor Red
    Write-Host "   git init" -ForegroundColor Yellow
    Write-Host "   git add ." -ForegroundColor Yellow
    Write-Host "   git commit -m 'Initial commit'" -ForegroundColor Yellow
    exit 1
}

# Check if we have uncommitted changes
$gitStatus = git status --porcelain
if ($gitStatus)
{
    Write-Host "⚠️  Warning: You have uncommitted changes. Please commit them first:" -ForegroundColor Yellow
    Write-Host "   git add ." -ForegroundColor Yellow
    Write-Host "   git commit -m 'Update for Render deployment'" -ForegroundColor Yellow
    Write-Host ""
    $response = Read-Host "Do you want to continue anyway? (y/n)"
    if ($response -notmatch "^[Yy]$")
    {
        exit 1
    }
}

# Check if we have a remote repository
try
{
    git remote get-url origin > $null 2>&1
}
catch
{
    Write-Host "❌ Error: No remote repository found. Please add a GitHub remote:" -ForegroundColor Red
    Write-Host "   git remote add origin https://github.com/yourusername/your-repo.git" -ForegroundColor Yellow
    Write-Host "   git push -u origin main" -ForegroundColor Yellow
    exit 1
}

Write-Host "✅ Pre-deployment checks passed!" -ForegroundColor Green
Write-Host ""

# Display deployment options
Write-Host "Deployment Options:" -ForegroundColor Cyan
Write-Host "1. Deploy using Blueprint (Recommended)" -ForegroundColor White
Write-Host "2. Manual deployment instructions" -ForegroundColor White
Write-Host "3. Generate JWT secret" -ForegroundColor White
Write-Host ""

$choice = Read-Host "Choose an option (1-3)"

switch ($choice)
{
    1 {
        Write-Host ""
        Write-Host "🔧 Blueprint Deployment" -ForegroundColor Cyan
        Write-Host "======================" -ForegroundColor Cyan
        Write-Host ""
        Write-Host "1. Push your code to GitHub:" -ForegroundColor White
        Write-Host "   git push origin main" -ForegroundColor Yellow
        Write-Host ""
        Write-Host "2. Go to https://dashboard.render.com" -ForegroundColor White
        Write-Host "3. Click 'New +' → 'Blueprint'" -ForegroundColor White
        Write-Host "4. Connect your GitHub repository" -ForegroundColor White
        Write-Host "5. Select this repository" -ForegroundColor White
        Write-Host "6. Click 'Apply' to deploy" -ForegroundColor White
        Write-Host ""
        Write-Host "Render will automatically:" -ForegroundColor Green
        Write-Host "• Create PostgreSQL database" -ForegroundColor Green
        Write-Host "• Set up Redis instance" -ForegroundColor Green
        Write-Host "• Deploy your web service" -ForegroundColor Green
        Write-Host "• Configure environment variables" -ForegroundColor Green
        Write-Host ""
    }
    2 {
        Write-Host ""
        Write-Host "📝 Manual Deployment Instructions" -ForegroundColor Cyan
        Write-Host "=================================" -ForegroundColor Cyan
        Write-Host ""
        Write-Host "1. Create PostgreSQL Database:" -ForegroundColor White
        Write-Host "   - Go to Render dashboard" -ForegroundColor Yellow
        Write-Host "   - Click 'New +' → 'PostgreSQL'" -ForegroundColor Yellow
        Write-Host "   - Name: ecomora-db" -ForegroundColor Yellow
        Write-Host "   - Database: ecomora_db" -ForegroundColor Yellow
        Write-Host "   - User: ecomora_user" -ForegroundColor Yellow
        Write-Host ""
        Write-Host "2. Create Redis Instance:" -ForegroundColor White
        Write-Host "   - Click 'New +' → 'Redis'" -ForegroundColor Yellow
        Write-Host "   - Name: ecomora-redis" -ForegroundColor Yellow
        Write-Host ""
        Write-Host "3. Create Web Service:" -ForegroundColor White
        Write-Host "   - Click 'New +' → 'Web Service'" -ForegroundColor Yellow
        Write-Host "   - Connect GitHub repository" -ForegroundColor Yellow
        Write-Host "   - Build Command: ./gradlew server:shadowJar" -ForegroundColor Yellow
        Write-Host "   - Start Command: java -jar server/build/libs/server-all.jar" -ForegroundColor Yellow
        Write-Host ""
        Write-Host "4. Add Environment Variables:" -ForegroundColor White
        Write-Host "   - ENV=production" -ForegroundColor Yellow
        Write-Host "   - PORT=8080" -ForegroundColor Yellow
        Write-Host "   - DATABASE_URL=[from database]" -ForegroundColor Yellow
        Write-Host "   - DB_USERNAME=[from database]" -ForegroundColor Yellow
        Write-Host "   - DB_PASSWORD=[from database]" -ForegroundColor Yellow
        Write-Host "   - JWT_SECRET=[generate 256-bit secret]" -ForegroundColor Yellow
        Write-Host ""
    }
    3 {
        Write-Host ""
        Write-Host "🔐 JWT Secret Generator" -ForegroundColor Cyan
        Write-Host "======================" -ForegroundColor Cyan
        Write-Host ""
        # Generate a secure JWT secret
        $bytes = New-Object byte[] 32
        $rng = [System.Security.Cryptography.RNGCryptoServiceProvider]::Create()
        $rng.GetBytes($bytes)
        $jwtSecret = [Convert]::ToHexString($bytes)
        Write-Host "Generated JWT Secret:" -ForegroundColor Green
        Write-Host $jwtSecret -ForegroundColor Yellow
        Write-Host ""
        Write-Host "Copy this secret and add it as JWT_SECRET environment variable in Render." -ForegroundColor White
        Write-Host ""
    }
    default {
        Write-Host "Invalid option. Please choose 1, 2, or 3." -ForegroundColor Red
        exit 1
    }
}

Write-Host "📚 For detailed instructions, see: RENDER_DEPLOYMENT_GUIDE.md" -ForegroundColor Cyan
Write-Host ""
Write-Host "🎉 Happy deploying!" -ForegroundColor Green