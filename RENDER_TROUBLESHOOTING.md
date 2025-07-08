# Render Deployment Troubleshooting Guide

## Common Issues and Solutions

### 1. Database Connection Refused Error

**Error**: `Connection to localhost:5432 refused`

**Problem**: The application is trying to connect to a local database instead of the Render
PostgreSQL instance.

**Solutions**:

#### Option A: Manual Environment Variable Setup

1. Go to your web service in Render dashboard
2. Navigate to **Environment** tab
3. Add these environment variables:
    - `DATABASE_URL`: Copy from your PostgreSQL database connection string
    - `DB_USERNAME`: Copy from your PostgreSQL database user
    - `DB_PASSWORD`: Copy from your PostgreSQL database password
    - `ENV`: Set to `production`
    - `PORT`: Set to `8080`
    - `JWT_SECRET`: Generate a 64-character random string

#### Option B: Redeploy with Blueprint

1. Delete your current services (web service, database, and redis)
2. Go to **Dashboard** → **New +** → **Blueprint**
3. Select your GitHub repository
4. Ensure the `render.yaml` file is in the repository root
5. Click **Apply**

#### Option C: Check Database Status

1. Go to your PostgreSQL database in Render dashboard
2. Ensure the database is **Available** (not Creating or Failed)
3. Check the **Connect** tab for connection details
4. Verify the database name is `ecomora_db`

### 2. Environment Variables Not Found

**Error**: Application using default localhost values

**Solution**:

1. In your web service, go to **Environment** tab
2. Check that all required variables are set:
   ```
   DATABASE_URL=postgres://username:password@hostname:port/database
   DB_USERNAME=ecomora_user
   DB_PASSWORD=your_generated_password
   ENV=production
   PORT=8080
   JWT_SECRET=your_generated_secret
   STATIC_FILE_ROOT=/opt/render/project/src/uploads
   UPLOAD_DIR=/opt/render/project/src/uploads
   ```

### 3. Database Not Created

**Error**: Database connection fails during Blueprint deployment

**Solution**:

1. Check if PostgreSQL database was created successfully
2. Go to **Dashboard** → **PostgreSQL** → **ecomora-db**
3. If it shows "Failed" or "Creating", delete and recreate:
    - Delete the database instance
    - Delete the web service
    - Redeploy using Blueprint

### 4. Build Failures

**Error**: Build fails during Docker image creation

**Solutions**:

1. **Java Version Issues**:
    - Ensure your `build.gradle.kts` specifies Java 21
    - Check that Dockerfile uses correct Java version

2. **Dependency Issues**:
    - Clear Gradle cache: `./gradlew clean`
    - Update dependencies in `build.gradle.kts`
    - Check for version conflicts

3. **Memory Issues**:
    - Upgrade to a higher plan temporarily for build
    - Optimize build process

### 5. Application Won't Start

**Error**: Application starts but crashes immediately

**Common Causes**:

1. **Database Connection**: Most common issue
2. **Missing Environment Variables**: Check all required vars are set
3. **Port Issues**: Ensure PORT is set to 8080
4. **File Permissions**: Check upload directory permissions

**Debug Steps**:

1. Check logs in Render dashboard
2. Verify all environment variables are set correctly
3. Test database connection manually
4. Check if required directories exist

### 6. Static Files Not Serving

**Error**: Image uploads or static files not accessible

**Solution**:

1. Verify upload directory environment variables:
   ```
   STATIC_FILE_ROOT=/opt/render/project/src/uploads
   UPLOAD_DIR=/opt/render/project/src/uploads
   ```
2. Check file permissions in your application
3. Ensure upload directory is created at startup

## Manual Deployment Steps

If Blueprint deployment fails, try manual deployment:

### 1. Create PostgreSQL Database

```bash
Name: ecomora-db
Database Name: ecomora_db
User: ecomora_user
Region: Oregon (or your preferred region)
Plan: Free (for testing) or Starter
```

### 2. Create Redis Instance

```bash
Name: ecomora-redis
Region: Same as database
Plan: Free (for testing) or Starter
```

### 3. Create Web Service

```bash
Name: ecomora-server
Build Command: ./gradlew server:shadowJar
Start Command: java -jar server/build/libs/server-all.jar
```

### 4. Set Environment Variables

Copy the connection strings from your database and Redis instances:

```bash
DATABASE_URL=postgres://username:password@hostname:port/database
DB_USERNAME=ecomora_user
DB_PASSWORD=your_password
ENV=production
PORT=8080
JWT_SECRET=your_64_char_secret
STATIC_FILE_ROOT=/opt/render/project/src/uploads
UPLOAD_DIR=/opt/render/project/src/uploads
REDIS_URL=redis://username:password@hostname:port
```

## Health Check Commands

Test your deployment with these commands:

### 1. Check Database Connection

```bash
psql $DATABASE_URL -c "SELECT 1;"
```

### 2. Check Application Health

```bash
curl https://your-app-url/health
```

### 3. Check Environment Variables

In your web service logs, look for:

```
Starting Ecomora Server v1.0.0 on 0.0.0.0:8080
Running in PRODUCTION mode
```

## Common Environment Variable Values

### Production Environment Variables

```env
ENV=production
PORT=8080
DATABASE_URL=postgres://ecomora_user:PASSWORD@HOST:5432/ecomora_db
DB_USERNAME=ecomora_user
DB_PASSWORD=YOUR_PASSWORD
JWT_SECRET=YOUR_64_CHAR_SECRET
STATIC_FILE_ROOT=/opt/render/project/src/uploads
UPLOAD_DIR=/opt/render/project/src/uploads
REDIS_URL=redis://default:PASSWORD@HOST:6379
```

### How to Generate JWT Secret

```bash
# Option 1: Using OpenSSL
openssl rand -hex 32

# Option 2: Using PowerShell
[System.Web.Security.Membership]::GeneratePassword(64, 0)

# Option 3: Online generator
# Visit: https://www.allkeysgenerator.com/Random/Security-Encryption-Key-Generator.aspx
```

## Debugging Steps

### 1. Check Service Status

1. Go to Render dashboard
2. Check all services are "Live" (not "Build Failed" or "Deploy Failed")
3. Look for error indicators (red dots)

### 2. Check Logs

1. Go to your web service
2. Click **Logs** tab
3. Look for error messages, especially database connection errors

### 3. Check Database Connection

1. Go to PostgreSQL database
2. Click **Connect** tab
3. Copy connection string
4. Test connection manually

### 4. Verify Environment Variables

1. In web service, go to **Environment** tab
2. Check all required variables are present
3. Verify database connection details match your database

### 5. Test Database Initialization

1. Connect to your database using psql
2. Run: `\dt` to see if tables were created
3. If no tables, check application logs for database initialization errors

## Getting Help

If you're still having issues:

1. **Check Render Status**: https://status.render.com
2. **Render Community**: https://community.render.com
3. **Render Documentation**: https://render.com/docs
4. **GitHub Issues**: Check your repository issues
5. **Application Logs**: Always check the full logs in Render dashboard

## Quick Fix Script

Create a script to quickly redeploy:

```bash
#!/bin/bash
# quick-redeploy.sh

echo "🔄 Redeploying Ecomora Server..."

# Force a new deployment
git commit --allow-empty -m "Trigger Render deployment"
git push origin main

echo "✅ Deployment triggered. Check Render dashboard for progress."
```

## Prevention Tips

1. **Test Locally First**: Always test your changes locally before deploying
2. **Use Environment Variables**: Never hardcode database connections
3. **Check Logs Regularly**: Monitor application logs for issues
4. **Backup Database**: Regular backups of your PostgreSQL database
5. **Monitor Resources**: Keep an eye on CPU and memory usage
6. **Use Staging**: Consider a staging environment for testing

Remember: Most deployment issues are related to environment variables and database connections.
Always verify these first!