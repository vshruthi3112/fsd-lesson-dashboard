# Railway Deployment Guide — Lesson Dashboard Application

This guide covers deploying the Lesson Dashboard (React frontend + Spring Boot backend) to **Railway** using Docker, with a detailed walkthrough of the entire setup, configuration, and deployment flow.

---

## Table of Contents

1. [What is Railway?](#1-what-is-railway)
2. [Architecture on Railway](#2-architecture-on-railway)
3. [Prerequisites](#3-prerequisites)
4. [Docker Setup Explained](#4-docker-setup-explained)
5. [Step-by-Step Deployment](#5-step-by-step-deployment)
6. [Environment Variables](#6-environment-variables)
7. [CORS & Networking](#7-cors--networking)
8. [Custom Domains](#8-custom-domains)
9. [Monitoring & Logs](#9-monitoring--logs)
10. [Redeployment & CI/CD](#10-redeployment--cicd)
11. [Cleanup](#11-cleanup)

---

## 1. What is Railway?

Railway is a cloud platform that deploys applications from Git repositories or Docker images. Compared to Azure App Service (covered in `DEPLOYMENT.md`), Railway is:

- **Simpler**: No resource groups, registries, or service plans to manage. Push code, Railway builds and deploys.
- **Docker-native**: Detects your Dockerfile automatically and builds from it.
- **Free tier available**: Offers a trial plan with $5 of credits (enough for learning and experimentation).
- **Environment-first**: Environment variables are a first-class concept in the dashboard.

| Feature | Azure App Service | Railway |
|---------|------------------|---------|
| Setup complexity | High (CLI, resource groups, ACR) | Low (Git push or dashboard) |
| Docker support | Yes (via ACR) | Yes (auto-detects Dockerfile) |
| Free tier | No container support on free | $5 trial credit |
| Scaling | Auto-scaling on Standard+ | Manual or auto-scale on Pro |
| Custom domains | Yes | Yes |
| SSL/HTTPS | Built-in | Built-in |

---

## 2. Architecture on Railway

On Railway, the frontend and backend run as **separate services** within a single **project**. Each service gets its own public URL, and the browser communicates directly with both.

```
┌─────────────────────────────────────────────────────────────────┐
│                        Railway Project                          │
│                                                                 │
│  ┌──────────────────────────┐  ┌──────────────────────────────┐ │
│  │  Frontend Service        │  │  Backend Service             │ │
│  │  (Nginx container)       │  │  (Spring Boot container)     │ │
│  │                          │  │                              │ │
│  │  Serves React SPA        │  │  REST API + JWT Auth         │ │
│  │  Static HTML/CSS/JS      │  │  H2 Database (in-memory)     │ │
│  │                          │  │                              │ │
│  │  Public URL:             │  │  Public URL:                 │ │
│  │  frontend-xxx.railway.app│  │  backend-xxx.railway.app     │ │
│  └──────────────────────────┘  └──────────────────────────────┘ │
│          ▲                              ▲                        │
└──────────┼──────────────────────────────┼────────────────────────┘
           │                              │
           │         Browser              │
           │  ┌─────────────────────┐     │
           └──│  Loads HTML/CSS/JS  │─────┘
              │  API calls go       │
              │  directly to        │
              │  backend URL        │
              └─────────────────────┘
```

**Key difference from Docker Compose (local):**
- Locally, Nginx proxies `/api` requests to the backend container via Docker networking (`http://backend:8080`).
- On Railway, there is no shared Docker network between services. The frontend is a static file server, and the React app calls the backend directly using its public Railway URL. The `VITE_API_URL` build variable makes this work.

---

## 3. Prerequisites

Before deploying, make sure you have:

1. **A Railway account**: Sign up at [railway.app](https://railway.app). GitHub login is easiest.
2. **Git repository**: Your code pushed to GitHub (Railway deploys from Git repos).
3. **Railway CLI** (optional but recommended):
   ```bash
   npm install -g @railway/cli
   railway login
   ```
4. **Docker Desktop** (optional): Only needed if you want to test Docker builds locally before deploying.

---

## 4. Docker Setup Explained

This section explains every Docker file in the project and how Railway uses them.

### 4.1 Backend Dockerfile (`backend/Dockerfile`)

This Dockerfile builds the Spring Boot API into a container.

```
backend/
├── Dockerfile          ← The recipe for the backend container
├── .dockerignore       ← Files excluded from the Docker build
├── pom.xml             ← Maven dependencies
└── src/                ← Java source code
```

**How it works (two-stage build):**

```
┌─────────────────────────────────────────────────────────┐
│  STAGE 1: Build (maven:3.9-eclipse-temurin-17)          │
│                                                         │
│  1. Copy pom.xml → download dependencies (cached)       │
│  2. Copy src/ → compile Java source code                │
│  3. Run: mvn clean package -DskipTests                  │
│  4. Output: target/lesson-dashboard-api-0.0.1.jar       │
│                                                         │
│  Image size: ~500MB (Maven + JDK + all build tools)     │
└──────────────────────────┬──────────────────────────────┘
                           │ Copy only the JAR
                           ▼
┌─────────────────────────────────────────────────────────┐
│  STAGE 2: Runtime (eclipse-temurin:17-jre)              │
│                                                         │
│  1. Create non-root user (appuser) for security         │
│  2. Create /app/logs directory                          │
│  3. Copy app.jar from Stage 1                           │
│  4. Run: java -jar app.jar                              │
│                                                         │
│  Final image size: ~200MB (JRE only, no build tools)    │
└─────────────────────────────────────────────────────────┘
```

**Why multi-stage?** The build stage needs Maven and the full JDK (compilers, build tools) — roughly 500MB. The runtime stage only needs the JRE to run the compiled JAR — roughly 200MB. By splitting them, the deployed image is 60% smaller.

**Layer caching:** The Dockerfile copies `pom.xml` first and runs `mvn dependency:go-offline` before copying source code. Docker caches each layer. If only your Java code changes (not dependencies), Docker skips the slow dependency download step on rebuilds.

**Security:** The runtime stage creates a non-root user (`appuser`). The application runs as this user instead of root, limiting damage if the container is compromised.

**Health check:** The Dockerfile includes a `HEALTHCHECK` instruction that pings `/api/auth/health` every 30 seconds. Railway uses this to know when the container is ready and to detect crashes.

### 4.2 Frontend Dockerfile (`Dockerfile` at project root)

This Dockerfile builds the React app and serves it with Nginx.

```
project-root/
├── Dockerfile          ← The recipe for the frontend container
├── .dockerignore       ← Files excluded from the Docker build
├── nginx.conf          ← Nginx configuration template
├── package.json        ← Node.js dependencies
├── index.html          ← Vite entry point
├── vite.config.js      ← Vite build configuration
└── src/                ← React source code
```

**How it works (two-stage build):**

```
┌─────────────────────────────────────────────────────────┐
│  STAGE 1: Build (node:20-alpine)                        │
│                                                         │
│  1. Copy package.json + package-lock.json               │
│  2. Run: yarn install (installs node_modules)           │
│  3. Copy source code (index.html, vite.config, src/)    │
│  4. Set VITE_API_URL build argument                     │
│  5. Run: vite build → produces dist/ folder             │
│                                                         │
│  Image size: ~300MB (Node.js + all npm packages)        │
└──────────────────────────┬──────────────────────────────┘
                           │ Copy only dist/ + nginx.conf
                           ▼
┌─────────────────────────────────────────────────────────┐
│  STAGE 2: Runtime (nginx:alpine)                        │
│                                                         │
│  1. Copy dist/ (static HTML/CSS/JS) to Nginx html dir   │
│  2. Copy nginx.conf as template                         │
│  3. At startup: envsubst replaces $PORT in nginx.conf   │
│  4. Start Nginx                                         │
│                                                         │
│  Final image size: ~40MB (Nginx + static files only)    │
└─────────────────────────────────────────────────────────┘
```

**The `VITE_API_URL` build argument** is critical for Railway deployment. In the Dockerfile:

```dockerfile
ARG VITE_API_URL=https://fsd-lesson-dashboard-production.up.railway.app/api
ENV VITE_API_URL=${VITE_API_URL}
RUN ./node_modules/.bin/vite build
```

Vite replaces `import.meta.env.VITE_API_URL` in the JavaScript bundle at **build time** (not runtime). The React app uses this URL to make API calls directly to the backend's Railway domain. You must set this to your actual backend Railway URL.

**Why yarn instead of npm?** The Dockerfile uses `yarn install` instead of `npm ci` due to a known npm bug in Docker builds ("Exit handler never called") that causes silent dependency installation failures.

**Dynamic port via `envsubst`:** Railway assigns a dynamic `PORT` environment variable. The `CMD` line uses `envsubst` to inject this port into the Nginx config template at container startup:

```dockerfile
CMD envsubst '$PORT' < /etc/nginx/nginx-template.conf > /etc/nginx/conf.d/default.conf && nginx -g 'daemon off;'
```

### 4.3 Nginx Configuration (`nginx.conf`)

Nginx serves the React SPA as static files. On Railway, it does **not** proxy API requests — the browser calls the backend directly.

```nginx
server {
    listen ${PORT};            # Replaced by envsubst at startup
    server_name localhost;

    root /usr/share/nginx/html;
    index index.html;

    # SPA routing: serve index.html for all routes so React Router works
    location / {
        try_files $uri $uri/ /index.html;
    }

    # Cache static assets (Vite adds content hashes to filenames)
    location /assets/ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }

    # Security headers
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
}
```

**Why `try_files $uri $uri/ /index.html`?** React Router handles client-side routing. If a user navigates to `/dashboard` and refreshes, Nginx would normally return a 404 (there's no `/dashboard` file). This directive tells Nginx to serve `index.html` instead, letting React Router handle the URL.

**Why `${PORT}` instead of `80`?** Railway dynamically assigns a port via the `PORT` environment variable. The `envsubst` command in the Dockerfile CMD replaces `${PORT}` with the actual value at startup.

### 4.4 `.dockerignore` Files

These files tell Docker which files to skip when building images, making builds faster and images smaller.

**Root `.dockerignore`** (frontend build):
- Excludes `node_modules/` (rebuilt inside Docker), `.git/`, `.env`, `backend/logs/`, IDE files
- Does NOT exclude `src/` or `dist/` (needed for the build)

**`backend/.dockerignore`** (backend build):
- Excludes `target/` (rebuilt inside Docker), `logs/`, `.git/`, IDE files

### 4.5 Docker Compose (`docker-compose.yml`) — Local Only

Docker Compose is used for **local development**, not for Railway. It orchestrates both containers on your machine with a shared network.

```yaml
services:
  backend:
    build: ./backend
    ports: ["8080:8080"]
    environment:
      - JWT_SECRET=${JWT_SECRET:-default...}
      - SERVER_PORT=8080
    networks: [app-network]

  frontend:
    build: .
    ports: ["3000:80"]
    depends_on: [backend]
    networks: [app-network]

networks:
  app-network:
    driver: bridge
```

Locally, both containers share `app-network`, so the frontend's Nginx can reach `http://backend:8080`. On Railway, each service gets its own network with a public URL instead.

---

## 5. Step-by-Step Deployment

### 5.1 Push Your Code to GitHub

Railway deploys from Git repositories. Make sure your code is pushed:

```bash
git add .
git commit -m "Prepare for Railway deployment"
git push origin main
```

### 5.2 Create a Railway Project

1. Go to [railway.app](https://railway.app) and log in.
2. Click **"New Project"** on your dashboard.
3. Select **"Empty Project"** (we'll add services manually for clarity).

You now have an empty project. We'll add two services: backend and frontend.

### 5.3 Deploy the Backend Service

1. Inside your project, click **"New Service"** → **"GitHub Repo"**.
2. Select your repository.
3. Railway will auto-detect the Dockerfile. **But** the root Dockerfile is for the frontend. You need to point it to the backend:

**Configure the build:**
- Go to the service **Settings** tab.
- Under **Build**, set:
  - **Root Directory**: `backend`
  - **Builder**: `Dockerfile` (Railway auto-detects this)
  - **Dockerfile Path**: `Dockerfile` (relative to root directory, so it reads `backend/Dockerfile`)

4. Rename the service to **"backend"** (click the service name at the top) for clarity.

5. **Add environment variables** (Settings → Variables):

   | Variable | Value |
   |----------|-------|
   | `JWT_SECRET` | A secure random string, 32+ characters (e.g., generate with `openssl rand -hex 32`) |
   | `JWT_EXPIRATION` | `86400000` |
   | `SPRING_PROFILES_ACTIVE` | `production` |
   | `PORT` | `8080` |
   | `SERVER_PORT` | `8080` |
   | `CORS_ALLOWED_ORIGINS` | *(set after frontend is deployed — see Step 5.5)* |

6. **Generate a public domain:**
   - Go to **Settings** → **Networking** → **Public Networking**.
   - Click **"Generate Domain"**.
   - Railway assigns a URL like: `backend-xxx-production.up.railway.app`
   - Note this URL — the frontend needs it.

7. Click **"Deploy"** (or it auto-deploys on the next push).

### 5.4 Deploy the Frontend Service

1. In the same project, click **"New Service"** → **"GitHub Repo"** again.
2. Select the same repository.

**Configure the build:**
- Go to **Settings** tab.
- Under **Build**:
  - **Root Directory**: Leave empty (uses project root)
  - **Builder**: `Dockerfile`
  - **Dockerfile Path**: `Dockerfile` (the root Dockerfile)

3. Rename the service to **"frontend"**.

4. **Add environment variables** (Settings → Variables):

   | Variable | Value |
   |----------|-------|
   | `VITE_API_URL` | `https://<your-backend-domain>.railway.app/api` |

   **Important:** `VITE_API_URL` is a **build-time** variable. Vite bakes it into the JavaScript bundle during `vite build`. Railway picks it up from the environment and passes it as a Docker build argument. After changing this variable, you must **redeploy** the frontend for it to take effect.

5. **Generate a public domain:**
   - **Settings** → **Networking** → **Public Networking** → **"Generate Domain"**.
   - You'll get something like: `frontend-xxx-production.up.railway.app`

6. Deploy the service.

### 5.5 Update Backend CORS

Now that both services have URLs, go back to the **backend** service and set the CORS variable:

| Variable | Value |
|----------|-------|
| `CORS_ALLOWED_ORIGINS` | `https://<your-frontend-domain>.railway.app` |

This tells Spring Security to accept API requests from the frontend's domain. Without this, the browser blocks cross-origin requests.

**Redeploy the backend** after adding the CORS variable (Railway usually auto-redeploys on variable changes).

### 5.6 Verify the Deployment

1. **Backend health check:**
   ```
   https://<your-backend-domain>.railway.app/api/auth/health
   ```
   Should return a 200 response.

2. **Frontend:**
   ```
   https://<your-frontend-domain>.railway.app
   ```
   Should load the login page. Log in with `admin/password123` or `instructor/password123`.

3. **Test the full flow:**
   - Log in
   - View lessons
   - Create a new lesson
   - Edit / delete a lesson (as admin)

---

## 6. Environment Variables

### How Environment Variables Flow on Railway

```
┌─────────────────────────┐
│  Railway Dashboard      │
│  (Variables tab)        │
│                         │
│  JWT_SECRET=abc...      │──────────┐
│  SERVER_PORT=8080       │          │
│  CORS_ALLOWED_ORIGINS=  │          │
│    https://frontend...  │          │
└─────────────────────────┘          │
                                     ▼
                          ┌──────────────────────┐
                          │  Docker Container     │
                          │                       │
                          │  Spring Boot reads:   │
                          │  ${JWT_SECRET}         │──→ jwt.secret
                          │  ${SERVER_PORT}        │──→ server.port
                          │  ${CORS_ALLOWED_ORIGINS}──→ cors.allowed-origins
                          └──────────────────────┘
```

Spring Boot automatically maps environment variables to `application.properties` values using the `${}` syntax. For example, in `application.properties`:

```properties
jwt.secret=${JWT_SECRET:mySecretKeyForJWTtokenSigning2024LessonDashboard!}
```

The `:` after `JWT_SECRET` specifies a fallback default used when the variable is not set (local dev). On Railway, the environment variable overrides the default.

### Complete Variable Reference

**Backend service:**

| Variable | Purpose | Required | Default |
|----------|---------|----------|---------|
| `JWT_SECRET` | Signs JWT tokens. Must be 32+ chars. | **Yes** | Dev fallback in properties |
| `JWT_EXPIRATION` | Token lifetime in ms | No | `86400000` (24h) |
| `SERVER_PORT` | Port Spring Boot listens on | No | `8080` |
| `PORT` | Railway's assigned port | Auto-set | Set by Railway |
| `SPRING_PROFILES_ACTIVE` | Spring profile | Recommended | `default` |
| `CORS_ALLOWED_ORIGINS` | Allowed frontend origins (comma-separated) | **Yes** | `http://localhost:3000` |
| `LOG_PATH` | Log file directory | No | `logs` |

**Frontend service:**

| Variable | Purpose | Required | When Applied |
|----------|---------|----------|--------------|
| `VITE_API_URL` | Backend API base URL | **Yes** | Build time only |
| `PORT` | Nginx listen port | Auto-set | Runtime (via envsubst) |

### Generating a Secure JWT Secret

Never use the development default in production. Generate a secure secret:

```bash
# Using openssl (recommended)
openssl rand -hex 32

# Using Node.js
node -e "console.log(require('crypto').randomBytes(32).toString('hex'))"

# Using Python
python -c "import secrets; print(secrets.token_hex(32))"
```

Copy the output and paste it as the `JWT_SECRET` value in Railway's Variables tab.

---

## 7. CORS & Networking

### Why CORS Matters on Railway

Locally, the frontend and backend run on the same origin (the Vite proxy makes it transparent). On Railway, they're on different domains:

```
Frontend: https://frontend-xxx.railway.app  (origin A)
Backend:  https://backend-xxx.railway.app   (origin B)
```

The browser enforces the Same-Origin Policy: JavaScript on origin A cannot call APIs on origin B unless origin B explicitly allows it via CORS headers.

### How CORS is Configured

In `SecurityConfig.java`, the `corsConfigurationSource()` method reads allowed origins from the `CORS_ALLOWED_ORIGINS` environment variable:

```java
@Value("${cors.allowed-origins:http://localhost:3000}")
private String allowedOrigins;

// In corsConfigurationSource():
configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
configuration.setAllowCredentials(true);
```

On Railway, set `CORS_ALLOWED_ORIGINS` to your frontend's public URL:

```
CORS_ALLOWED_ORIGINS=https://frontend-xxx-production.up.railway.app
```

For multiple origins (e.g., custom domain + Railway domain):

```
CORS_ALLOWED_ORIGINS=https://frontend-xxx-production.up.railway.app,https://lessons.yourdomain.com
```

### How the Frontend Finds the Backend

The `apiClient.js` uses `VITE_API_URL` as the base for all API calls:

```javascript
const BASE_URL = import.meta.env.VITE_API_URL || '/api';
```

- **Local dev**: `VITE_API_URL` is unset, so it defaults to `/api`. The Vite proxy forwards `/api` to `http://127.0.0.1:8080`.
- **Railway**: `VITE_API_URL` is set to `https://backend-xxx.railway.app/api` at build time. The React app calls this URL directly from the browser.

---

## 8. Custom Domains

Railway provides `.railway.app` subdomains by default. To use your own domain:

1. In the Railway service **Settings** → **Networking** → **Custom Domain**.
2. Add your domain (e.g., `lessons.yourdomain.com` for frontend, `api.lessons.yourdomain.com` for backend).
3. Railway provides DNS records (CNAME). Add them to your domain registrar.
4. Wait for DNS propagation (usually a few minutes to an hour).
5. Railway auto-provisions an SSL certificate via Let's Encrypt.

**After adding a custom domain, update:**
- `CORS_ALLOWED_ORIGINS` on the backend to include the new frontend domain.
- `VITE_API_URL` on the frontend if the backend domain changed (requires a redeploy).

---

## 9. Monitoring & Logs

### Viewing Logs in Railway

1. Click on a service in your project.
2. Go to the **"Deployments"** tab.
3. Click on the active deployment.
4. The **"Logs"** panel shows real-time stdout/stderr output.

**Backend logs** show Spring Boot startup, API requests, authentication events, and errors.
**Frontend logs** show Nginx access and error logs.

### Using the Railway CLI

```bash
# Login
railway login

# Link to your project
railway link

# Stream logs from a service
railway logs

# Open the project dashboard
railway open
```

### Health Checks

The backend Dockerfile includes a health check:

```dockerfile
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/api/auth/health || exit 1
```

Railway monitors this and will restart the container if it becomes unhealthy. The `start_period` gives the JVM 40 seconds to start before health checks begin.

---

## 10. Redeployment & CI/CD

### Automatic Deploys

By default, Railway auto-deploys when you push to the connected branch (usually `main`). Every `git push` triggers:

1. Railway detects the push via GitHub webhook.
2. Builds the Docker image using the configured Dockerfile.
3. Starts the new container.
4. Routes traffic to it once healthy.
5. Stops the old container.

This is a **rolling deployment** — zero downtime for the end user.

### Manual Redeployment

In the Railway dashboard:
1. Go to the service → **Deployments** tab.
2. Click **"Redeploy"** on the latest deployment. Or click the three dots menu → **"Redeploy"**.

Via CLI:

```bash
railway up
```

### When to Redeploy

| Change | Requires Redeploy? | Which Service? |
|--------|-------------------|----------------|
| Java code change | Yes (auto on push) | Backend |
| React code change | Yes (auto on push) | Frontend |
| Backend env var change | Yes (auto) | Backend |
| `VITE_API_URL` change | **Yes (manual or push)** | Frontend |
| `CORS_ALLOWED_ORIGINS` change | Yes (auto) | Backend |
| Nginx config change | Yes (auto on push) | Frontend |

**Important:** `VITE_API_URL` is baked into the JS bundle at build time. Changing the Railway variable alone may not trigger a rebuild. Force a redeploy after changing it.

---

## 11. Cleanup

### Delete a Single Service

1. Railway dashboard → Click the service → **Settings** → scroll down → **"Delete Service"**.

### Delete the Entire Project

1. Railway dashboard → Project → **Settings** (gear icon, top right) → **"Danger"** section → **"Delete Project"**.

This removes all services, domains, and environment variables. This action is **irreversible**.

### Via CLI

```bash
# Delete the linked project
railway down
```

---

## Quick Reference

### Deployment Checklist

- [ ] Code pushed to GitHub
- [ ] Railway project created
- [ ] Backend service created with `Root Directory: backend`
- [ ] Backend environment variables set (`JWT_SECRET`, `SPRING_PROFILES_ACTIVE=production`)
- [ ] Backend public domain generated
- [ ] Frontend service created with root Dockerfile
- [ ] Frontend `VITE_API_URL` set to backend's Railway URL + `/api`
- [ ] Frontend public domain generated
- [ ] Backend `CORS_ALLOWED_ORIGINS` set to frontend's Railway URL
- [ ] Both services deployed and healthy
- [ ] Login tested with `admin/password123`
- [ ] Full CRUD flow tested

### Key URLs (Template)

| Resource | URL |
|----------|-----|
| Frontend | `https://<frontend-service>-production.up.railway.app` |
| Backend API | `https://<backend-service>-production.up.railway.app` |
| Health Check | `https://<backend-service>-production.up.railway.app/api/auth/health` |
| Railway Dashboard | `https://railway.app/dashboard` |
