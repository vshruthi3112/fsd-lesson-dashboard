#!/bin/bash
# ============================================
# Azure App Service Deployment Script
# ============================================
#
# WHAT IS AZURE APP SERVICE?
# Azure App Service is a fully managed platform for hosting web applications.
# You push your Docker container, and Azure handles:
#   - Server provisioning (you don't manage VMs)
#   - Auto-scaling (handles traffic spikes)
#   - SSL certificates (HTTPS)
#   - Custom domains
#   - Health monitoring
#   - Continuous deployment
#
# PREREQUISITES:
# 1. Azure CLI installed: https://learn.microsoft.com/en-us/cli/azure/install-azure-cli
# 2. Docker installed and running
# 3. An Azure account (free tier works for learning)
#
# USAGE:
#   chmod +x azure-deploy.sh
#   ./azure-deploy.sh
#
# ============================================

# ── Configuration ─────────────────────────────────────
# Change these values to match your Azure setup
RESOURCE_GROUP="lesson-dashboard-rg"
LOCATION="eastus"
ACR_NAME="lessondashboardacr"          # Azure Container Registry (must be globally unique, lowercase, no dashes)
APP_SERVICE_PLAN="lesson-dashboard-plan"
BACKEND_APP_NAME="lesson-dashboard-api"
FRONTEND_APP_NAME="lesson-dashboard-web"

echo "============================================"
echo "  Lesson Dashboard — Azure Deployment"
echo "============================================"

# ── Step 1: Login to Azure ────────────────────────────
echo ""
echo "Step 1: Logging in to Azure..."
az login

# ── Step 2: Create Resource Group ─────────────────────
# A Resource Group is a container that holds related Azure resources.
# Think of it like a folder for your project's cloud resources.
echo ""
echo "Step 2: Creating Resource Group '$RESOURCE_GROUP' in '$LOCATION'..."
az group create \
  --name $RESOURCE_GROUP \
  --location $LOCATION

# ── Step 3: Create Azure Container Registry (ACR) ────
# ACR is like Docker Hub, but private and hosted on Azure.
# It stores your Docker images so Azure App Service can pull them.
echo ""
echo "Step 3: Creating Azure Container Registry '$ACR_NAME'..."
az acr create \
  --resource-group $RESOURCE_GROUP \
  --name $ACR_NAME \
  --sku Basic \
  --admin-enabled true

# Get ACR credentials (username + password for pushing images)
ACR_USERNAME=$(az acr credential show --name $ACR_NAME --query username -o tsv)
ACR_PASSWORD=$(az acr credential show --name $ACR_NAME --query "passwords[0].value" -o tsv)
ACR_LOGIN_SERVER=$(az acr show --name $ACR_NAME --query loginServer -o tsv)

echo "  ACR Login Server: $ACR_LOGIN_SERVER"

# ── Step 4: Build and Push Docker Images ──────────────
# Build the images locally, tag them with the ACR address, and push them.
echo ""
echo "Step 4: Building and pushing Docker images..."

# Login to ACR (so Docker can push to it)
az acr login --name $ACR_NAME

# Build and push Backend image
echo "  Building backend image..."
docker build -t $ACR_LOGIN_SERVER/$BACKEND_APP_NAME:latest ./backend
echo "  Pushing backend image..."
docker push $ACR_LOGIN_SERVER/$BACKEND_APP_NAME:latest

# Build and push Frontend image
echo "  Building frontend image..."
docker build -t $ACR_LOGIN_SERVER/$FRONTEND_APP_NAME:latest .
echo "  Pushing frontend image..."
docker push $ACR_LOGIN_SERVER/$FRONTEND_APP_NAME:latest

# ── Step 5: Create App Service Plan ──────────────────
# An App Service Plan defines the compute resources (CPU, RAM, pricing tier).
# B1 (Basic) is the minimum tier that supports custom Docker containers.
# Free/Shared tiers don't support containers.
echo ""
echo "Step 5: Creating App Service Plan '$APP_SERVICE_PLAN'..."
az appservice plan create \
  --name $APP_SERVICE_PLAN \
  --resource-group $RESOURCE_GROUP \
  --sku B1 \
  --is-linux

# ── Step 6: Create Backend Web App ───────────────────
echo ""
echo "Step 6: Creating Backend Web App '$BACKEND_APP_NAME'..."
az webapp create \
  --resource-group $RESOURCE_GROUP \
  --plan $APP_SERVICE_PLAN \
  --name $BACKEND_APP_NAME \
  --docker-registry-server-url "https://$ACR_LOGIN_SERVER" \
  --docker-registry-server-user $ACR_USERNAME \
  --docker-registry-server-password $ACR_PASSWORD \
  --deployment-container-image-name "$ACR_LOGIN_SERVER/$BACKEND_APP_NAME:latest"

# ── Step 7: Configure Backend Environment Variables ───
# These are the ENVIRONMENT VARIABLES for the backend.
# Azure App Service injects them into the container at runtime.
# This is the same as the .env file, but managed in Azure's dashboard.
echo ""
echo "Step 7: Configuring Backend environment variables..."
az webapp config appsettings set \
  --resource-group $RESOURCE_GROUP \
  --name $BACKEND_APP_NAME \
  --settings \
    JWT_SECRET="YourProductionSecretKeyHere_ChangeThisToSomethingSecure123!" \
    JWT_EXPIRATION="86400000" \
    SPRING_PROFILES_ACTIVE="production" \
    WEBSITES_PORT="8080"

# WEBSITES_PORT tells Azure which port your container listens on.
# Azure maps its own port 80/443 to your container's WEBSITES_PORT.

# ── Step 8: Create Frontend Web App ──────────────────
echo ""
echo "Step 8: Creating Frontend Web App '$FRONTEND_APP_NAME'..."
az webapp create \
  --resource-group $RESOURCE_GROUP \
  --plan $APP_SERVICE_PLAN \
  --name $FRONTEND_APP_NAME \
  --docker-registry-server-url "https://$ACR_LOGIN_SERVER" \
  --docker-registry-server-user $ACR_USERNAME \
  --docker-registry-server-password $ACR_PASSWORD \
  --deployment-container-image-name "$ACR_LOGIN_SERVER/$FRONTEND_APP_NAME:latest"

# ── Step 9: Configure Frontend ───────────────────────
echo ""
echo "Step 9: Configuring Frontend environment variables..."
az webapp config appsettings set \
  --resource-group $RESOURCE_GROUP \
  --name $FRONTEND_APP_NAME \
  --settings \
    WEBSITES_PORT="80"

# ── Step 10: Enable Logging ──────────────────────────
echo ""
echo "Step 10: Enabling container logging..."
az webapp log config \
  --resource-group $RESOURCE_GROUP \
  --name $BACKEND_APP_NAME \
  --docker-container-logging filesystem

az webapp log config \
  --resource-group $RESOURCE_GROUP \
  --name $FRONTEND_APP_NAME \
  --docker-container-logging filesystem

# ── Done! ─────────────────────────────────────────────
BACKEND_URL="https://$BACKEND_APP_NAME.azurewebsites.net"
FRONTEND_URL="https://$FRONTEND_APP_NAME.azurewebsites.net"

echo ""
echo "============================================"
echo "  Deployment Complete!"
echo "============================================"
echo ""
echo "  Backend API:  $BACKEND_URL"
echo "  Frontend App: $FRONTEND_URL"
echo "  Health Check: $BACKEND_URL/api/auth/health"
echo ""
echo "  View logs:"
echo "    az webapp log tail --name $BACKEND_APP_NAME --resource-group $RESOURCE_GROUP"
echo "    az webapp log tail --name $FRONTEND_APP_NAME --resource-group $RESOURCE_GROUP"
echo ""
echo "  To clean up (delete everything):"
echo "    az group delete --name $RESOURCE_GROUP --yes --no-wait"
echo ""
