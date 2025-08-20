# Use OpenJDK 21 as base image
FROM openjdk:21-jdk-slim

# Set working directory
WORKDIR /app

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Copy Gradle wrapper and build files
COPY gradlew gradlew.bat ./
COPY gradle/ gradle/
COPY build.gradle.kts settings.gradle.kts ./
COPY server/build.gradle.kts ./server/

# Copy source code
COPY server/src/ ./server/src/

# Make gradlew executable and build the application
RUN chmod +x ./gradlew && ./gradlew :server:shadowJar --no-daemon

# Create user for running the application
RUN groupadd -r ecomora && useradd -r -g ecomora ecomora

# Create directories and set permissions
RUN mkdir -p /app/uploads /app/logs && \
    chown -R ecomora:ecomora /app

# Switch to non-root user
USER ecomora

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/health || exit 1

# Run the application
CMD ["java", "-jar", "server/build/libs/server-all.jar"]