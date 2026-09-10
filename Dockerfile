# ============================================
# Dockerfile for Lesson Dashboard Frontend (React + Vite)
# ============================================
#
# MULTI-STAGE BUILD:
#   Stage 1 ("build"): Node.js — installs dependencies, builds React
#   Stage 2 ("runtime"): Nginx — serves the static HTML/CSS/JS files
#
# This Dockerfile is used by Railway (cloud deployment).

# ── Stage 1: Build the React application ────────────────────────
FROM node:20-alpine AS build

WORKDIR /app

# Copy package files first (Docker layer caching)
COPY package.json package-lock.json ./

# Install dependencies using npm install instead of npm ci
# to work around a known npm ci bug ("Exit handler never called")
# that silently skips devDependencies in Docker builds.
RUN npm install --registry https://registry.npmjs.org/

# Copy source code and config
COPY index.html vite.config.js ./
COPY src ./src

# Build the production bundle
ARG VITE_API_URL=/api
ENV VITE_API_URL=${VITE_API_URL}
RUN npx vite build

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
