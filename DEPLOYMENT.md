# Deployment Guide — Lesson Dashboard Application

This guide covers deploying the Lesson Dashboard (React frontend + Spring Boot backend) using **Docker** and **Azure App Service**, covering all four core concepts:

1. **Docker Basics** — Containerizing the application
2. **Environment Variables** — Externalizing configuration
3. **Azure App Service** — Cloud hosting
4. **Deploy the Application** — Step-by-step deployment

---

## Architecture Overview

```
┌──────────────────────────────────────────────────────┐
│                    Azure App Service                 │
│                                                      │
│  ┌──────────────────┐    ┌─────────────────────────┐ │
│  │  Frontend (Nginx) │    │  Backend (Spring Boot) │ │
│  │   Port 80         │───▶│   Port 8080            │ │
│  │                   │/api│                        │ │
│  │  React SPA        │    │  REST API + JWT Auth   │ │
│  │  Static Files     │    │  H2 Database           │ │
│  └──────────────────┘    └─────────────────────────┘ │
│         ▲                                            │
│         │ HTTPS                                      │
└─────────┼────────────────────────────────────────────┘
          │
      End Users
```

---

## 1. Docker Basics

### What is Docker?

Docker packages your application into **containers** — lightweight, portable units that include everything needed to run: code, runtime, libraries, and configuration.

**Analogy**: Imagine shipping a fully set-up computer to someone. Docker is like shipping a computer that:
- Has the OS already installed
- Has your app configured and ready to go
- Works the same way no matter where you run it (your laptop, a server, the cloud)

### Key Docker Concepts

| Concept | What It Is | Analogy |
|---------|-----------|---------|
| **Image** | A read-only template with your app + dependencies | A recipe/blueprint |
| **Container** | A running instance of an image | A dish made from the recipe |
| **Dockerfile** | Instructions to build an image | The recipe steps |
| **Docker Compose** | Define and run multi-container apps | A meal plan (multiple recipes) |
| **Registry** | Storage for Docker images | A cookbook library |
| **Volume** | Persistent storage that survives container restarts | An external hard drive |

### Project Docker Files

```
FSD Lesson Manager Application/
├── Dockerfile              # Frontend: React → Nginx
├── .dockerignore           # Files to exclude from Docker builds
├── nginx.conf              # Nginx config for frontend (proxy + SPA routing)
├── docker-compose.yml      # Orchestrates frontend + backend containers
├── .env.example            # Template for environment variables
│
└── backend/
    ├── Dockerfile          # Backend: Maven build → JRE runtime
    └── .dockerignore       # Backend-specific excludes
```

### Multi-Stage Builds

Both Dockerfiles use **multi-stage builds** to keep images small:

```
Backend (2 stages):
  Stage 1 (maven:3.9-eclipse-temurin-17):  Compile Java → JAR    ~500MB
  Stage 2 (eclipse-temurin:17-jre):         Run the JAR only      ~200MB
                                                          Savings: ~300MB

Frontend (2 stages):
  Stage 1 (node:18-alpine):   npm install + vite build            ~300MB
  Stage 2 (nginx:alpine):     Serve static files only              ~40MB
                                                          Savings: ~260MB
```

### Run Locally with Docker Compose

```bash
# 1. Copy environment file
cp .env.example .env

# 2. Edit .env with your values (especially JWT_SECRET for production)
#    For local testing, the defaults work fine.

# 3. Build and start both containers
docker-compose up --build

# 4. Access the application
#    Frontend: http://localhost:3000
#    Backend:  http://localhost:8080
#    Health:   http://localhost:8080/api/auth/health
```

### Useful Docker Commands

```bash
# Build images without starting
docker-compose build

# Start in background (detached mode)
docker-compose up -d

# View running containers
docker-compose ps

# View logs
docker-compose logs -f              # All services
docker-compose logs -f backend      # Backend only

# Stop everything
docker-compose down

# Stop and remove volumes (clean reset)
docker-compose down -v

# Rebuild a specific service
docker-compose up --build backend
```

---

## 2. Environment Variables

### Why Environment Variables?

Environment variables separate **configuration from code**. This means:

1. **Security**: Secrets (JWT keys, DB passwords) never appear in source code or Git
2. **Flexibility**: Same Docker image works in dev, staging, and production
3. **12-Factor App**: Industry best practice for cloud-native applications

### How They Work in This Project

```
┌─────────────────────┐     ┌──────────────────────┐     ┌─────────────────┐
│   .env file         │────▶│   docker-compose.yml │────▶│   Container     │
│   (local secrets)   │     │   (reads .env)       │     │   (app reads    │
│                     │     │                      │     │    env vars)    │
│ JWT_SECRET=abc...   │     │ - JWT_SECRET=${...}  │     │                 │
│ JWT_EXPIRATION=...  │     │ - SERVER_PORT=8080   │     │ Spring Boot     │
└─────────────────────┘     └──────────────────────┘     │ auto-maps:     │
                                                         │ jwt.secret ←   │
                                                         │   JWT_SECRET   │
                                                         └─────────────────┘
```

### Backend Environment Variables

| Variable | Description | Default | Required in Production |
|----------|------------|---------|----------------------|
| `JWT_SECRET` | Secret key for signing JWT tokens (min 32 chars) | Dev default | **Yes — change this!** |
| `JWT_EXPIRATION` | Token validity in milliseconds | `86400000` (24h) | No |
| `SERVER_PORT` | HTTP port | `8080` | No |
| `SPRING_PROFILES_ACTIVE` | Spring profile (default/production) | `default` | Recommended |
| `LOG_PATH` | Directory for log files | `logs` | No |

### Frontend Environment Variables

| Variable | Description | Default | Set At |
|----------|------------|---------|--------|
| `VITE_API_URL` | Backend API base URL | `/api` | Build time |

### Spring Boot Property Resolution

Spring Boot automatically maps environment variables to properties:

```
Environment Variable          →  application.properties
─────────────────────────────────────────────────────────
JWT_SECRET                    →  jwt.secret
JWT_EXPIRATION                →  jwt.expiration
SERVER_PORT                   →  server.port
SPRING_PROFILES_ACTIVE        →  spring.profiles.active
LOG_PATH                      →  logging.file.name path
```

The `${}` syntax in application.properties enables this:
```properties
jwt.secret=${JWT_SECRET:defaultValue}
#                       ↑ colon = fallback if env var not set
```

---

## 3. Azure App Service

### What is Azure App Service?

Azure App Service is a **Platform as a Service (PaaS)** that hosts web applications. You provide a Docker container, and Azure handles everything else:

- **No server management**: No VMs to patch, no OS updates
- **Auto-scaling**: Handles traffic spikes automatically
- **Built-in HTTPS**: Free SSL certificates
- **Custom domains**: Point your domain to the app
- **Deployment slots**: Test new versions before going live
- **Monitoring**: Built-in metrics and alerts

### Azure Resources We Create

```
lesson-dashboard-rg (Resource Group)
├── lessondashboardacr (Container Registry)    — Stores Docker images
├── lesson-dashboard-plan (App Service Plan)   — Compute resources (CPU/RAM)
├── lesson-dashboard-api (Web App)             — Backend container
└── lesson-dashboard-web (Web App)             — Frontend container
```

| Resource | What It Is | Analogy |
|----------|-----------|---------|
| **Resource Group** | Logical container for related resources | A project folder |
| **Container Registry (ACR)** | Private Docker image storage | Private Docker Hub |
| **App Service Plan** | The "hardware" tier (CPU, RAM, price) | Choosing a hosting plan |
| **Web App** | The actual running application | A website on the server |

### Pricing

| Plan | CPU | RAM | Cost (approx.) | Supports Containers |
|------|-----|-----|-----------------|-------------------|
| Free (F1) | Shared | 1 GB | $0/month | ❌ No |
| Basic (B1) | 1 core | 1.75 GB | ~$13/month | ✅ Yes |
| Standard (S1) | 1 core | 1.75 GB | ~$70/month | ✅ Yes + Scaling |

For learning, **B1** is the minimum that supports custom Docker containers.

---

## 4. Deploy the Application

### Prerequisites

1. **Docker Desktop** installed and running
2. **Azure CLI** installed: https://learn.microsoft.com/en-us/cli/azure/install-azure-cli
3. **Azure account** (free tier: https://azure.microsoft.com/free)

### Option A: Automated Deployment (Script)

```bash
# Make the script executable
chmod +x azure-deploy.sh

# Run the deployment
./azure-deploy.sh
```

The script handles all 10 steps automatically. Read the script comments for explanations.

### Option B: Manual Step-by-Step

#### Step 1: Login to Azure

```bash
az login
```

This opens your browser for authentication.

#### Step 2: Create a Resource Group

```bash
az group create \
  --name lesson-dashboard-rg \
  --location eastus
```

#### Step 3: Create Azure Container Registry

```bash
az acr create \
  --resource-group lesson-dashboard-rg \
  --name lessondashboardacr \
  --sku Basic \
  --admin-enabled true
```

#### Step 4: Build and Push Docker Images

```bash
# Login to ACR
az acr login --name lessondashboardacr

# Get the ACR server address
ACR_SERVER=$(az acr show --name lessondashboardacr --query loginServer -o tsv)

# Build and push backend
docker build -t $ACR_SERVER/lesson-dashboard-api:latest ./backend
docker push $ACR_SERVER/lesson-dashboard-api:latest

# Build and push frontend
docker build -t $ACR_SERVER/lesson-dashboard-web:latest .
docker push $ACR_SERVER/lesson-dashboard-web:latest
```

#### Step 5: Create App Service Plan

```bash
az appservice plan create \
  --name lesson-dashboard-plan \
  --resource-group lesson-dashboard-rg \
  --sku B1 \
  --is-linux
```

#### Step 6: Create Backend Web App

```bash
ACR_USER=$(az acr credential show --name lessondashboardacr --query username -o tsv)
ACR_PASS=$(az acr credential show --name lessondashboardacr --query "passwords[0].value" -o tsv)

az webapp create \
  --resource-group lesson-dashboard-rg \
  --plan lesson-dashboard-plan \
  --name lesson-dashboard-api \
  --docker-registry-server-url "https://$ACR_SERVER" \
  --docker-registry-server-user $ACR_USER \
  --docker-registry-server-password $ACR_PASS \
  --deployment-container-image-name "$ACR_SERVER/lesson-dashboard-api:latest"
```

#### Step 7: Set Backend Environment Variables

```bash
az webapp config appsettings set \
  --resource-group lesson-dashboard-rg \
  --name lesson-dashboard-api \
  --settings \
    JWT_SECRET="YourSecureProductionKeyHere_AtLeast32Characters!" \
    JWT_EXPIRATION="86400000" \
    SPRING_PROFILES_ACTIVE="production" \
    WEBSITES_PORT="8080"
```

#### Step 8: Create and Configure Frontend Web App

```bash
az webapp create \
  --resource-group lesson-dashboard-rg \
  --plan lesson-dashboard-plan \
  --name lesson-dashboard-web \
  --docker-registry-server-url "https://$ACR_SERVER" \
  --docker-registry-server-user $ACR_USER \
  --docker-registry-server-password $ACR_PASS \
  --deployment-container-image-name "$ACR_SERVER/lesson-dashboard-web:latest"

az webapp config appsettings set \
  --resource-group lesson-dashboard-rg \
  --name lesson-dashboard-web \
  --settings WEBSITES_PORT="80"
```

#### Step 9: Verify Deployment

```bash
# Check backend health
curl https://lesson-dashboard-api.azurewebsites.net/api/auth/health

# View backend logs
az webapp log tail --name lesson-dashboard-api --resource-group lesson-dashboard-rg
```

### After Deployment

Your application is accessible at:
- **Frontend**: `https://lesson-dashboard-web.azurewebsites.net`
- **Backend API**: `https://lesson-dashboard-api.azurewebsites.net`
- **Health Check**: `https://lesson-dashboard-api.azurewebsites.net/api/auth/health`

### Cleanup (Delete Everything)

```bash
# This removes ALL resources in the group — irreversible!
az group delete --name lesson-dashboard-rg --yes --no-wait
```

---

## Troubleshooting

### Container won't start

```bash
# Check container logs
az webapp log tail --name lesson-dashboard-api --resource-group lesson-dashboard-rg

# Check if the port is correct
az webapp config appsettings list --name lesson-dashboard-api --resource-group lesson-dashboard-rg
```

### Frontend can't reach backend

The Nginx config uses `http://backend:8080` as the proxy target, which works in Docker Compose. For Azure (separate Web Apps), you'll need to update `nginx.conf` to point to the Azure backend URL:

```nginx
location /api/ {
    proxy_pass https://lesson-dashboard-api.azurewebsites.net;
    ...
}
```

Then rebuild and redeploy the frontend image.

### JWT errors after deployment

Ensure `JWT_SECRET` is set in Azure App Service settings:
```bash
az webapp config appsettings list \
  --name lesson-dashboard-api \
  --resource-group lesson-dashboard-rg
```

### View all environment variables in Azure

```bash
az webapp config appsettings list \
  --name lesson-dashboard-api \
  --resource-group lesson-dashboard-rg \
  --output table
```
