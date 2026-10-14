# Multi-stage Dockerfile for Public Transport Management System (PTMS)

# Stage 1: Build the Maven artifact
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy pom.xml and source code
COPY pom.xml .
COPY src ./src

# Compile and package executable JAR
RUN mvn clean package -DskipTests

# Stage 2: Lightweight runtime environment
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy compiled JAR from build stage
COPY --from=builder /app/target/public-transport-management-system-1.0.0.jar app.jar

# Change ownership
RUN chown appuser:appgroup app.jar
USER appuser

# Expose web dashboard port
EXPOSE 8085

# Healthcheck
HEALTHCHECK --interval=30s --timeout=5s --start-period=5s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8085/api/state || exit 1

# Launch standalone PTMS application in server mode
ENTRYPOINT ["java", "-jar", "app.jar", "--server"]
