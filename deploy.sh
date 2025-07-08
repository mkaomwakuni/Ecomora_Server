#!/bin/bash

# Deployment script for Ecomora Server on Google Cloud Run

# Set your project ID
PROJECT_ID="ecomora-server-project"
SERVICE_NAME="ecomora-server"
REGION="us-central1"

# Replace these with your actual values
DB_CONNECTION_NAME="ecomora-server-project:us-central1:ecomora-db"
DB_PASSWORD="your-secure-password-here"
JWT_SECRET="your-256-bit-jwt-secret-here"

echo "🚀 Deploying Ecomora Server to Google Cloud Run..."

# Build and push Docker image
echo "📦 Building Docker image..."
docker build -t gcr.io/${PROJECT_ID}/${SERVICE_NAME} .

echo "📤 Pushing to Google Container Registry..."
docker push gcr.io/${PROJECT_ID}/${SERVICE_NAME}

# Deploy to Cloud Run
echo "🌐 Deploying to Cloud Run..."
gcloud run deploy ${SERVICE_NAME} \
    --image gcr.io/${PROJECT_ID}/${SERVICE_NAME} \
    --platform managed \
    --region ${REGION} \
    --allow-unauthenticated \
    --port 8080 \
    --memory 1Gi \
    --cpu 1 \
    --min-instances 0 \
    --max-instances 10 \
    --timeout 300 \
    --add-cloudsql-instances ${DB_CONNECTION_NAME} \
    --set-env-vars ENV=production \
    --set-env-vars APP_VERSION=1.0.0 \
    --set-env-vars PORT=8080 \
    --set-env-vars HOST=0.0.0.0 \
    --set-env-vars DATABASE_URL="jdbc:postgresql://google/ecomora_db?cloudSqlInstance=${DB_CONNECTION_NAME}&socketFactory=com.google.cloud.sql.postgres.SocketFactory&useSSL=false" \
    --set-env-vars DB_USERNAME=ecomora_user \
    --set-env-vars DB_PASSWORD=${DB_PASSWORD} \
    --set-env-vars STATIC_FILE_ROOT=/app/uploads \
    --set-env-vars UPLOAD_DIR=/app/uploads \
    --set-env-vars JWT_SECRET=${JWT_SECRET} \
    --set-env-vars JWT_AUDIENCE=ecomora-api \
    --set-env-vars JWT_DOMAIN=https://ecomora-server-project.run.app/ \
    --set-env-vars JWT_REALM="ecomora server" \
    --set-env-vars JWT_EXPIRATION_HOURS=24

echo "✅ Deployment completed!"
echo "📝 Your service URL:"
gcloud run services describe ${SERVICE_NAME} --region ${REGION} --format="value(status.url)"

echo "🔍 Health check:"
SERVICE_URL=$(gcloud run services describe ${SERVICE_NAME} --region ${REGION} --format="value(status.url)")
echo "Test: curl ${SERVICE_URL}/health"