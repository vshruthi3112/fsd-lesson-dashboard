# ============================================
# Dockerfile for Lesson Dashboard Frontend (React + Vite)
# ============================================
#
# MULTI-STAGE BUILD:
#   Stage 1 ("build"): Node.js — installs dependencies, builds React
#   Stage 2 ("runtime"): Nginx — serves the static HTML/CSS/JS files
#
# Uses yarn instead of npm to work around a known npm ci/install bug
# in Docker builds ("Exit handler never called") that causes silent
# dependency installation failures.

# ── Stage 1: Build the React application ────────────────────────
FROM node:20-alpine AS build

WORKDIR /app

# Copy package files
COPY package.json package-lock.json ./

# Use yarn to install dependencies (npm has a known Docker bug)
# --frozen-lockfile is yarn's equivalent of npm ci
RUN yarn install

# Verify vite is actually installed
RUN ls node_modules/.bin/vite && echo "vite found"

# Copy source code and config
COPY index.html vite.config.js ./
COPY src ./src

# Build the production bundle
# VITE_API_URL is set at build time. For Railway deployment, this should
# point directly to the backend's Railway URL (no Nginx proxy needed).
ARG VITE_API_URL=https://fsd-lesson-dashboard-production.up.railway.app/api
ENV VITE_API_URL=${VITE_API_URL}
RUN ./node_modules/.bin/vite build

# ── Stage 2: Serve with Nginx ───────────────────────────────────
FROM nginx:alpine

COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx-template.conf

EXPOSE 80

# Railway sets PORT dynamically. Default to 80 for local Docker.
ENV PORT=80

# Use envsubst to replace only $PORT in the template, preserving
# Nginx variables like $uri and $scheme. Then start Nginx.
CMD envsubst '$PORT' < /etc/nginx/nginx-template.conf > /etc/nginx/conf.d/default.conf && nginx -g 'daemon off;'
