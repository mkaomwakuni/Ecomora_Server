# PowerShell Deployment script for Ecomora Server on Google Cloud Run

# Set your project ID
$PROJECT_ID = "ecomora-server-project"
$SERVICE_NAME = "ecomora-server"
$REGION = "us-central1"

# Replace these with your actual values
$DB_CONNECTION_NAME = "ecomora-server-project:us-central1:ecomora-db"
$DB_PASSWORD = "your-secure-password-here"
$JWT_SECRET = "your-256-bit-jwt-secret-here"

Write-Host "🚀 Deploying Ecomora Server to Google Cloud Run..." -ForegroundColor Green

# Build and push Docker image
Write-Host "📦 Building Docker image..." -ForegroundColor Yellow
docker build -t gcr.io/$PROJECT_ID/$SERVICE_NAME .

Write-Host "📤 Pushing to Google Container Registry..." -ForegroundColor Yellow
docker push gcr.io/$PROJECT_ID/$SERVICE_NAME

# Deploy to Cloud Run
Write-Host "🌐 Deploying to Cloud Run..." -ForegroundColor Yellow
gcloud run deploy $SERVICE_NAME `
    --image gcr.io/$PROJECT_ID/$SERVICE_NAME `
    --platform managed `
    --region $REGION `
    --allow-unauthenticated `
    --port 8080 `
    --memory 1Gi `
    --cpu 1 `
    --min-instances 0 `
    --max-instances 10 `
    --timeout 300 `
    --add-cloudsql-instances $DB_CONNECTION_NAME `
    --set-env-vars ENV=production `
    --set-env-vars APP_VERSION=1.0.0 `
    --set-env-vars PORT=8080 `
    --set-env-vars HOST=0.0.0.0 `
    --set-env-vars DATABASE_URL="jdbc:postgresql://google/ecomora_db?cloudSqlInstance=$DB_CONNECTION_NAME&socketFactory=com.google.cloud.sql.postgres.SocketFactory&useSSL=false" `
    --set-env-vars DB_USERNAME=ecomora_user `
    --set-env-vars DB_PASSWORD=$DB_PASSWORD `
    --set-env-vars STATIC_FILE_ROOT=/app/uploads `
    --set-env-vars UPLOAD_DIR=/app/uploads `
    --set-env-vars JWT_SECRET=$JWT_SECRET `
    --set-env-vars JWT_AUDIENCE=ecomora-api `
    --set-env-vars JWT_DOMAIN=https://ecomora-server-project.run.app/ `
    --set-env-vars JWT_REALM="ecomora server" `
    --set-env-vars JWT_EXPIRATION_HOURS=24

Write-Host "✅ Deployment completed!" -ForegroundColor Green
Write-Host "📝 Your service URL:" -ForegroundColor Cyan
gcloud run services describe $SERVICE_NAME --region $REGION --format="value(status.url)"

Write-Host "🔍 Health check:" -ForegroundColor Cyan
$SERVICE_URL = gcloud run services describe $SERVICE_NAME --region $REGION --format="value(status.url)"
Write-Host "Test: curl $SERVICE_URL/health" -ForegroundColor White