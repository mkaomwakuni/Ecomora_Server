# Ecomora Server Deployment Guide - Railway

## Overview

Railway is a deployment platform that automatically provisions infrastructure and deploys
applications from Git repositories.

## Prerequisites

1. **Railway Account**: Create a free account at [railway.app](https://railway.app)
2. **GitHub Repository**: Your code should be in a GitHub repository
3. **Git**: Ensure your code is committed and pushed to GitHub

## Deployment Steps

### Step 1: Create Railway Project

1. Go to [Railway Dashboard](https://railway.app/dashboard)
2. Click **New Project**
3. Select **Deploy from GitHub repo**
4. Choose your Ecomora Server repository
5. Railway will automatically detect it's a Java project

### Step 2: Add PostgreSQL Database

1. In your project dashboard, click **+ New**
2. Select **Database** → **PostgreSQL**
3. Railway will provision a PostgreSQL database
4. Wait for it to show "Active" status

### Step 3: Configure Environment Variables

1. Click on your **web service** (your application)
2. Go to **Variables** tab
3. Add these environment variables:

#### Required Environment Variables:

```env
ENV=production
PORT=8080
DATABASE_URL=${{Postgres.DATABASE_URL}}
DB_USERNAME=${{Postgres.POSTGRES_USER}}
DB_PASSWORD=${{Postgres.POSTGRES_PASSWORD}}
JWT_SECRET=your_64_character_secret_here
STATIC_FILE_ROOT=/app/uploads
UPLOAD_DIR=/app/uploads
```

#### How to Reference PostgreSQL Variables:

Railway automatically creates variables for connected services. Use these exact references:

- `${{Postgres.DATABASE_URL}}`
- `${{Postgres.POSTGRES_USER}}`
- `${{Postgres.POSTGRES_PASSWORD}}`
- `${{Postgres.POSTGRES_DB}}`

### Step 4: Generate JWT Secret

Generate a secure JWT secret:

```bash
# Option 1: OpenSSL
openssl rand -hex 32

# Option 2: Online generator
# Visit: https://www.allkeysgenerator.com/Random/Security-Encryption-Key-Generator.aspx
```

### Step 5: Configure Service Connections

1. In your web service, go to **Settings**
2. Scroll to **Service Variables**
3. Click **+ Variable reference**
4. Select your PostgreSQL service
5. This automatically connects the services

### Step 6: Deploy

1. Railway automatically deploys when you push to your connected branch
2. Check the **Deployments** tab for build progress
3. Look for any errors in the build logs

## Alternative: Manual Database Setup

If automatic connection doesn't work:

### 1. Get Database Connection Details

1. Click on your PostgreSQL service
2. Go to **Connect** tab
3. Copy the connection details:
    - Host
    - Port
    - Database
    - Username
    - Password

### 2. Set Variables Manually

```env
DATABASE_URL=postgresql://username:password@host:port/database
DB_USERNAME=your_username
DB_PASSWORD=your_password
ENV=production
PORT=8080
JWT_SECRET=your_secret
```

## Configuration Files

### railway.json (Optional)

Create this file in your project root for custom configuration:

```json
{
  "$schema": "https://railway.app/railway.schema.json",
  "build": {
    "builder": "nixpacks",
    "buildCommand": "./gradlew server:shadowJar"
  },
  "deploy": {
    "startCommand": "java -jar server/build/libs/server-all.jar",
    "healthcheckPath": "/health",
    "healthcheckTimeout": 100,
    "restartPolicyType": "on_failure"
  }
}
```

### Procfile (Alternative)

If you prefer using a Procfile:

```
web: java -jar server/build/libs/server-all.jar
```

## Environment Variables Reference

### Required Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `ENV` | Environment type | `production` |
| `PORT` | Server port | `8080` |
| `DATABASE_URL` | PostgreSQL connection string | Auto-generated |
| `DB_USERNAME` | Database username | Auto-generated |
| `DB_PASSWORD` | Database password | Auto-generated |
| `JWT_SECRET` | JWT signing secret | Generated 64-char string |

### Optional Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `STATIC_FILE_ROOT` | Static files directory | `/app/uploads` |
| `UPLOAD_DIR` | Upload directory | `/app/uploads` |

## Troubleshooting

### Common Issues

#### 1. Database Connection Refused

**Error**: `Connection to localhost:5432 refused`

**Solution**:

1. Check if PostgreSQL service is active
2. Verify environment variables are set correctly
3. Ensure service variables reference the database service

#### 2. Environment Variables Not Set

**Error**: Application using default localhost values

**Solution**:

1. Go to web service → Variables tab
2. Add all required environment variables
3. Use Railway's variable references: `${{Postgres.DATABASE_URL}}`

#### 3. Build Failures

**Error**: Build fails during compilation

**Solution**:

1. Check build logs in Deployments tab
2. Ensure `gradlew` has execute permissions
3. Verify Java version compatibility

#### 4. Application Won't Start

**Error**: Application crashes on startup

**Solution**:

1. Check deployment logs
2. Verify all environment variables are set
3. Test database connection

### Debug Commands

Check your deployment status:

```bash
# Check if services are running
railway status

# View logs
railway logs

# Connect to database
railway connect postgres
```

### Health Check

Test your deployed application:

```bash
curl https://your-app-url.railway.app/health
```

## Custom Domain

### Add Custom Domain

1. Go to your web service
2. Click **Settings** → **Domains**
3. Click **+ Custom Domain**
4. Enter your domain (e.g., `api.yourdomain.com`)
5. Update your DNS records as instructed
6. Railway automatically provisions SSL certificates

## Scaling

Railway automatically scales based on usage:

- **Hobby Plan**: Limited resources
- **Pro Plan**: Better performance and scaling
- **Team Plan**: Advanced features

### Resource Limits

- **Memory**: Configurable per service
- **CPU**: Shared/dedicated based on plan
- **Storage**: Persistent volumes available

## Database Management

### Backup and Restore

1. Railway automatically backs up databases
2. Access backups in PostgreSQL service → Backups tab
3. Restore from any backup point

### Database Access

```bash
# Connect via Railway CLI
railway connect postgres

# Or use connection string directly
psql $DATABASE_URL
```

### Database Migrations

Your application automatically creates tables on first run. For manual migrations:

```bash
# Connect to database
railway connect postgres

# Run your migration SQL
\i your-migration.sql
```

## Security Best Practices

1. **Environment Variables**: Never commit secrets to code
2. **JWT Secrets**: Use strong, randomly generated secrets
3. **Database Access**: Use Railway's private networking
4. **HTTPS**: Railway automatically provides SSL/TLS
5. **Monitoring**: Enable Railway's monitoring features

## Cost Optimization

- **Resource Management**: Monitor usage in dashboard
- **Database Optimization**: Use connection pooling
- **Caching**: Implement Redis for better performance
- **Scaling**: Start with smaller plans and scale up

## Support and Resources

- **Railway Documentation**: https://docs.railway.app
- **Railway Discord**: https://discord.gg/railway
- **Railway Status**: https://status.railway.app
- **GitHub Issues**: Report application-specific issues

## Migration from Other Platforms

### From Render

1. Export database data from Render
2. Create new Railway project
3. Import data to Railway PostgreSQL
4. Update environment variables
5. Deploy

### From Heroku

1. Use Railway's Heroku migration tool
2. Or manually recreate services
3. Import database and configure variables

## Deployment Checklist

- [ ] Repository connected to Railway
- [ ] PostgreSQL database created
- [ ] Environment variables configured
- [ ] Service variables linked
- [ ] JWT secret generated
- [ ] Health checks working
- [ ] Custom domain configured (optional)
- [ ] Monitoring enabled
- [ ] Backup strategy in place

Remember: Railway automatically handles most infrastructure concerns, so focus on your application
configuration and environment variables.