# ============================================
# Dockerfile for Lesson Dashboard Frontend (React + Vite)
# ============================================
#
# MULTI-STAGE BUILD:
#   Stage 1 ("build"): Node.js — installs dependencies from public npm, builds React
#   Stage 2 ("runtime"): Nginx — serves the static HTML/CSS/JS files
#
# This Dockerfile is used by Railway (cloud deployment).
# Railway can access the public npm registry, so npm ci works here.

# ── Stage 1: Build the React application ────────────────────────
FROM node:18 AS build

WORKDIR /app

# Ensure devDependencies are installed (vite is a devDependency)
ENV NODE_ENV=development
ENV PATH=/app/node_modules/.bin:$PATH

# Copy package files and install dependencies
COPY package.json package-lock.json ./
RUN npm ci --registry https://registry.npmjs.org/

# Copy source code and config
COPY index.html vite.config.js ./
COPY src ./src

# Build the production bundle
ARG VITE_API_URL=/api
ENV VITE_API_URL=${VITE_API_URL}
RUN vite build

# ── Stage 2: Serve with Nginx ───────────────────────────────────
FROM nginx:alpine

COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
