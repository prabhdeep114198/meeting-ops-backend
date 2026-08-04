# ============================================================
# Multi-stage Docker build for MeetingOps Microservices
# ============================================================

# Stage 1: Build
FROM eclipse-temurin:21-jdk-alpine AS builder
ARG MODULE
WORKDIR /app

# Copy Maven wrapper and root POM
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Copy module POMs and sources
COPY common ./common
COPY api-gateway ./api-gateway
COPY meeting-service ./meeting-service
COPY ai-pipeline-service ./ai-pipeline-service
COPY review-service ./review-service
COPY mcp-service ./mcp-service

# Build specified module
RUN chmod +x mvnw && ./mvnw clean package -pl ${MODULE} -am -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
ARG MODULE
WORKDIR /app

RUN apk add --no-cache curl

# Copy built JAR for target module
COPY --from=builder /app/${MODULE}/target/*.jar app.jar

# Create non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

EXPOSE 8080 8081 8082 8083 8084

# Start application
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-XX:+HeapDumpOnOutOfMemoryError", \
    "-XX:HeapDumpPath=/tmp", \
    "-jar", "app.jar"]
