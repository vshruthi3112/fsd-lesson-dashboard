# ============================================
# Dockerfile for Lesson Dashboard API (Spring Boot Backend)
# ============================================
#
# DOCKER BASICS - What is this file?
# -----------------------------------
# A Dockerfile is a recipe that tells Docker how to build an "image" —
# a lightweight, portable package containing your application and everything
# it needs to run (Java runtime, dependencies, your compiled code).
#
# Think of it like shipping a pre-configured computer to someone:
#   1. Start with a base OS that has Java installed
#   2. Copy your application code into it
#   3. Tell it how to start your application
#
# MULTI-STAGE BUILD:
# We use two stages to keep the final image small:
#   Stage 1 ("build"): Has Maven + JDK — compiles the code, produces a JAR
#   Stage 2 ("runtime"): Has only JRE — runs the JAR, no build tools
# This reduces the final image from ~500MB to ~200MB.
#
# BUILD & RUN:
#   docker build -t lesson-dashboard-api .
#   docker run -p 8080:8080 lesson-dashboard-api

# ── Stage 1: Build the application ──────────────────────────────
# Uses Maven with JDK 17 to compile source code and produce a JAR file
FROM maven:3.9-eclipse-temurin-17 AS build

# Set the working directory inside the container
WORKDIR /app

# Copy Maven config first (for Docker layer caching).
# Docker caches each step. If pom.xml hasn't changed, it skips re-downloading
# all dependencies — this makes rebuilds MUCH faster.
COPY pom.xml .

# Download all dependencies (cached if pom.xml unchanged)
RUN mvn dependency:go-offline -B

# Now copy the actual source code
COPY src ./src

# Build the application JAR (skip tests for faster builds in Docker)
# The -DskipTests flag skips running tests — they should be run in CI/CD, not here
RUN mvn clean package -DskipTests -B

# ── Stage 2: Create the runtime image ───────────────────────────
# Uses only the Java Runtime Environment (JRE) — smaller and more secure
# than the full JDK because it doesn't include compilers or build tools
FROM eclipse-temurin:17-jre

# Set the working directory
WORKDIR /app

# Create a non-root user for security.
# Running as root inside a container is a security risk — if an attacker
# breaks out of the app, they'd have root access to the container.
RUN groupadd --system appgroup && useradd --system --gid appgroup appuser

# Create the logs directory (the app writes logs here)
RUN mkdir -p /app/logs && chown -R appuser:appgroup /app/logs

# Copy the built JAR from the build stage.
# The --from=build flag references Stage 1 — we only take the final artifact.
COPY --from=build /app/target/*.jar app.jar

# Change ownership of the JAR to our non-root user
RUN chown appuser:appgroup app.jar

# Switch to the non-root user
USER appuser

# Tell Docker which port this container listens on.
# This is documentation — it doesn't actually publish the port.
# You still need -p 8080:8080 when running the container.
EXPOSE 8080

# Health check — Docker (and Azure) can verify the app is actually running.
# Every 30 seconds, it hits the /api/auth endpoint.
# If it fails 3 times in a row, Docker marks the container as "unhealthy".
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/api/auth/health || exit 1

# Start the application.
# ENTRYPOINT defines the command that runs when the container starts.
# Spring Boot reads environment variables automatically, so JWT_SECRET,
# SERVER_PORT, etc. can be injected at runtime without changing code.
ENTRYPOINT ["java", "-jar", "app.jar"]
