# Ecomora Server Deployment Guide

## Prerequisites

1. **Google Cloud Account**: Set up a Google Cloud account and enable billing
2. **Docker**: Install Docker on your local machine
3. **Google Cloud SDK**: Install and configure `gcloud` CLI
4. **PostgreSQL**: Set up a PostgreSQL database (Cloud SQL recommended)

## Local Development Setup

### 1. Environment Setup

Copy the example environment file:

```bash
cp .env.example .env
```

Update the `.env` file with your local configuration:

- Database credentials
- JWT secret
- Upload directories

### 2. Database Setup

#### Option A: Using Docker Compose (Recommended)

```bash
docker-compose up -d postgres redis
```

#### Option B: Local PostgreSQL

1. Install PostgreSQL locally
2. Create database: `createdb ecomora_db`
3. Run initialization script: `psql -d ecomora_db -f init-db.sql`

### 3. Build and Run

```bash
# Build the application
./gradlew server:shadowJar

# Run with Docker Compose
docker-compose up

# Or run directly
java -jar server/build/libs/server-all.jar
```

## Production Deployment on Google Cloud

### 1. Google Cloud Setup

```bash
# Login to Google Cloud
gcloud auth login

# Set your project
gcloud config set project YOUR_PROJECT_ID

# Enable required APIs
gcloud services enable cloudsql.googleapis.com
gcloud services enable run.googleapis.com
gcloud services enable containerregistry.googleapis.com
```

### 2. Database Setup (Cloud SQL)

```bash
# Create Cloud SQL instance
gcloud sql instances create ecomora-db \
  --database-version=POSTGRES_15 \
  --tier=db-f1-micro \
  --region=us-central1 \
  --storage-type=SSD \
  --storage-size=10GB

# Create database
gcloud sql databases create ecomora_db --instance=ecomora-db

# Create user
gcloud sql users create ecomora_user \
  --instance=ecomora-db \
  --password=SECURE_PASSWORD_HERE

# Get connection name
gcloud sql instances describe ecomora-db --format="value(connectionName)"
```

### 3. Environment Variables for Production

Create a `production.env` file:

```env
ENV=production
DATABASE_URL=jdbc:postgresql://google/ecomora_db?cloudSqlInstance=PROJECT_ID:REGION:INSTANCE_NAME&socketFactory=com.google.cloud.sql.postgres.SocketFactory
DB_USERNAME=ecomora_user
DB_PASSWORD=YOUR_SECURE_PASSWORD
JWT_SECRET=YOUR_256_BIT_SECRET
STATIC_FILE_ROOT=/app/uploads
UPLOAD_DIR=/app/uploads
```

### 4. Build and Deploy with Cloud Run

```bash
# Build and push Docker image
docker build -t gcr.io/YOUR_PROJECT_ID/ecomora-server .
docker push gcr.io/YOUR_PROJECT_ID/ecomora-server

# Deploy to Cloud Run
gcloud run deploy ecomora-server \
  --image gcr.io/YOUR_PROJECT_ID/ecomora-server \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated \
  --add-cloudsql-instances PROJECT_ID:REGION:INSTANCE_NAME \
  --set-env-vars ENV=production \
  --set-env-vars DATABASE_URL="jdbc:postgresql://google/ecomora_db?cloudSqlInstance=PROJECT_ID:REGION:INSTANCE_NAME&socketFactory=com.google.cloud.sql.postgres.SocketFactory" \
  --set-env-vars DB_USERNAME=ecomora_user \
  --set-env-vars DB_PASSWORD=YOUR_SECURE_PASSWORD \
  --set-env-vars JWT_SECRET=YOUR_256_BIT_SECRET \
  --memory 1Gi \
  --cpu 1 \
  --port 8080
```

### 5. Custom Domain Setup

```bash
# Map custom domain
gcloud run domain-mappings create \
  --service ecomora-server \
  --domain api.yourdomain.com \
  --region us-central1
```

## Alternative Deployment Options

### 1. Google Kubernetes Engine (GKE)

```bash
# Create cluster
gcloud container clusters create ecomora-cluster \
  --zone us-central1-a \
  --num-nodes 3 \
  --machine-type e2-medium

# Deploy
kubectl apply -f k8s/
```

### 2. Compute Engine

```bash
# Create instance
gcloud compute instances create ecomora-server \
  --image-family=ubuntu-2004-lts \
  --image-project=ubuntu-os-cloud \
  --machine-type=e2-medium \
  --zone=us-central1-a

# SSH and setup
gcloud compute ssh ecomora-server --zone=us-central1-a
```

## Security Considerations

### 1. Environment Variables

- Never commit secrets to version control
- Use Google Secret Manager for production secrets
- Rotate JWT secrets regularly

### 2. Database Security

- Use strong passwords
- Enable SSL connections
- Restrict database access by IP

### 3. Network Security

- Use HTTPS in production
- Configure proper CORS settings
- Implement rate limiting

### 4. Container Security

- Use non-root user in containers
- Regularly update base images
- Scan images for vulnerabilities

## Monitoring and Logging

### 1. Cloud Logging

```bash
# View logs
gcloud logging read "resource.type=cloud_run_revision AND resource.labels.service_name=ecomora-server"
```

### 2. Cloud Monitoring

- Set up alerts for errors
- Monitor CPU and memory usage
- Track response times

### 3. Health Checks

- `/health` - Complete health status
- `/ready` - Readiness probe
- `/live` - Liveness probe

## Scaling

### 1. Cloud Run Scaling

```bash
# Update service with scaling config
gcloud run services update ecomora-server \
  --region us-central1 \
  --min-instances 1 \
  --max-instances 10 \
  --concurrency 100
```

### 2. Database Scaling

```bash
# Scale up database
gcloud sql instances patch ecomora-db \
  --tier=db-n1-standard-1 \
  --storage-size=20GB
```

## Backup and Recovery

### 1. Database Backups

```bash
# Create backup
gcloud sql backups create --instance=ecomora-db

# Restore from backup
gcloud sql backups restore BACKUP_ID --restore-instance=ecomora-db
```

### 2. File Storage Backups

- Use Google Cloud Storage for static files
- Enable versioning and lifecycle policies

## CI/CD Pipeline

### 1. Cloud Build

Create `cloudbuild.yaml`:

```yaml
steps:
  - name: 'gcr.io/cloud-builders/docker'
    args: ['build', '-t', 'gcr.io/$PROJECT_ID/ecomora-server', '.']
  - name: 'gcr.io/cloud-builders/docker'
    args: ['push', 'gcr.io/$PROJECT_ID/ecomora-server']
  - name: 'gcr.io/cloud-builders/gcloud'
    args: ['run', 'deploy', 'ecomora-server', '--image', 'gcr.io/$PROJECT_ID/ecomora-server', '--region', 'us-central1', '--platform', 'managed']
```

### 2. GitHub Actions

Set up automated deployments with GitHub Actions workflow.

## Cost Optimization

1. **Cloud Run**: Pay only for requests
2. **Cloud SQL**: Use smallest instance that meets performance needs
3. **Storage**: Use lifecycle policies for old files
4. **Networking**: Use CDN for static assets

## Troubleshooting

### Common Issues

1. **Database Connection**: Check Cloud SQL proxy configuration
2. **Memory Issues**: Increase container memory limits
3. **Slow Performance**: Enable connection pooling
4. **File Upload Issues**: Check upload directory permissions

### Debugging

```bash
# Check logs
gcloud run services logs tail ecomora-server --region us-central1

# Connect to database
gcloud sql connect ecomora-db --user=ecomora_user

# Test health endpoint
curl https://your-service-url/health
```

## Production Checklist

- [ ] Database is secured with strong passwords
- [ ] JWT secrets are properly generated and stored
- [ ] HTTPS is enabled
- [ ] CORS is configured for production domains
- [ ] Health checks are working
- [ ] Monitoring and alerting are set up
- [ ] Backups are configured
- [ ] CI/CD pipeline is working
- [ ] Performance testing is completed
- [ ] Security scan is passed