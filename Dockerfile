# ============================================
# Dockerfile for Lesson Dashboard Frontend (React + Vite)
# ============================================
#
# SINGLE-STAGE BUILD — Nginx serves pre-built files
#
# WHY THIS APPROACH?
# The project uses a corporate npm registry (Artifactory) that Docker
# containers can't reach. Additionally, native binaries (rollup, esbuild)
# are platform-specific — Windows node_modules won't work in Linux containers.
#
# Solution: Build on the host machine where npm works, then Docker only
# packages the output into an Nginx image. This is actually a common
# production pattern — CI/CD pipelines often build artifacts separately
# and then package them into minimal Docker images.
#
# BEFORE BUILDING:
#   npm install          (if not already done)
#   npm run build        (creates dist/ folder)
#   docker build -t lesson-dashboard-frontend .
#   docker run -p 3000:80 lesson-dashboard-frontend

# ── Nginx serves the pre-built React application ────────────────
FROM nginx:alpine

# Copy the pre-built React files into Nginx's serving directory.
# The dist/ folder is created by running "npm run build" on the host machine.
COPY dist /usr/share/nginx/html

# Copy our custom Nginx configuration (handles SPA routing + API proxy)
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Nginx runs on port 80 by default
EXPOSE 80

# Start Nginx in the foreground (required for Docker)
CMD ["nginx", "-g", "daemon off;"]
