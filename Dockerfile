# Build stage
FROM gradle:8-jdk21 AS builder

WORKDIR /app

# Copy dependency files first for better caching
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY gradle/ ./gradle/
COPY server/build.gradle.kts ./server/

# Download dependencies
RUN gradle dependencies --no-daemon

# Copy source code
COPY . .

# Build the application
RUN gradle server:shadowJar --no-daemon

# Production stage
FROM openjdk:21-jdk-slim

# Install required packages
RUN apt-get update && apt-get install -y \
    curl \
    wget \
    && rm -rf /var/lib/apt/lists/*

# Create app user
RUN useradd -m -u 1000 appuser

# Set working directory
WORKDIR /app

# Create necessary directories
RUN mkdir -p /app/logs /app/uploads/products /app/uploads/services /app/uploads/static \
    && chown -R appuser:appuser /app

# Copy the built JAR file
COPY --from=builder /app/server/build/libs/server-all.jar /app/ecomora-server.jar

# Copy any additional resources
COPY --from=builder /app/server/src/main/resources/ /app/resources/

# Change ownership
RUN chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/health || exit 1

# Set JVM options for production
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseStringDeduplication"

# Run the application
CMD ["sh", "-c", "java $JAVA_OPTS -jar ecomora-server.jar"]