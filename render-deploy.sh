#!/bin/bash

# Render Deployment Script for Ecomora Server
# This script helps you deploy your Ecomora server to Render

set -e

echo "🚀 Ecomora Server - Render Deployment"
echo "======================================"

# Check if we're in the right directory
if [ ! -f "render.yaml" ]; then
    echo "❌ Error: render.yaml not found. Please run this script from the project root."
    exit 1
fi

# Check if we have a git repository
if [ ! -d ".git" ]; then
    echo "❌ Error: This is not a git repository. Please initialize git first:"
    echo "   git init"
    echo "   git add ."
    echo "   git commit -m 'Initial commit'"
    exit 1
fi

# Check if we have uncommitted changes
if [ -n "$(git status --porcelain)" ]; then
    echo "⚠️  Warning: You have uncommitted changes. Please commit them first:"
    echo "   git add ."
    echo "   git commit -m 'Update for Render deployment'"
    echo ""
    read -p "Do you want to continue anyway? (y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

# Check if we have a remote repository
if ! git remote get-url origin > /dev/null 2>&1; then
    echo "❌ Error: No remote repository found. Please add a GitHub remote:"
    echo "   git remote add origin https://github.com/yourusername/your-repo.git"
    echo "   git push -u origin main"
    exit 1
fi

echo "✅ Pre-deployment checks passed!"
echo ""

# Display deployment options
echo "Deployment Options:"
echo "1. Deploy using Blueprint (Recommended)"
echo "2. Manual deployment instructions"
echo "3. Generate JWT secret"
echo ""

read -p "Choose an option (1-3): " -n 1 -r
echo

case $REPLY in
    1)
        echo ""
        echo "🔧 Blueprint Deployment"
        echo "======================"
        echo ""
        echo "1. Push your code to GitHub:"
        echo "   git push origin main"
        echo ""
        echo "2. Go to https://dashboard.render.com"
        echo "3. Click 'New +' → 'Blueprint'"
        echo "4. Connect your GitHub repository"
        echo "5. Select this repository"
        echo "6. Click 'Apply' to deploy"
        echo ""
        echo "Render will automatically:"
        echo "• Create PostgreSQL database"
        echo "• Set up Redis instance"
        echo "• Deploy your web service"
        echo "• Configure environment variables"
        echo ""
        ;;
    2)
        echo ""
        echo "📝 Manual Deployment Instructions"
        echo "================================="
        echo ""
        echo "1. Create PostgreSQL Database:"
        echo "   - Go to Render dashboard"
        echo "   - Click 'New +' → 'PostgreSQL'"
        echo "   - Name: ecomora-db"
        echo "   - Database: ecomora_db"
        echo "   - User: ecomora_user"
        echo ""
        echo "2. Create Redis Instance:"
        echo "   - Click 'New +' → 'Redis'"
        echo "   - Name: ecomora-redis"
        echo ""
        echo "3. Create Web Service:"
        echo "   - Click 'New +' → 'Web Service'"
        echo "   - Connect GitHub repository"
        echo "   - Build Command: ./gradlew server:shadowJar"
        echo "   - Start Command: java -jar server/build/libs/server-all.jar"
        echo ""
        echo "4. Add Environment Variables:"
        echo "   - ENV=production"
        echo "   - PORT=8080"
        echo "   - DATABASE_URL=[from database]"
        echo "   - DB_USERNAME=[from database]"
        echo "   - DB_PASSWORD=[from database]"
        echo "   - JWT_SECRET=[generate 256-bit secret]"
        echo ""
        ;;
    3)
        echo ""
        echo "🔐 JWT Secret Generator"
        echo "======================"
        echo ""
        # Generate a secure JWT secret
        JWT_SECRET=$(openssl rand -hex 32)
        echo "Generated JWT Secret:"
        echo "$JWT_SECRET"
        echo ""
        echo "Copy this secret and add it as JWT_SECRET environment variable in Render."
        echo ""
        ;;
    *)
        echo "Invalid option. Please choose 1, 2, or 3."
        exit 1
        ;;
esac

echo "📚 For detailed instructions, see: RENDER_DEPLOYMENT_GUIDE.md"
echo ""
echo "🎉 Happy deploying!"