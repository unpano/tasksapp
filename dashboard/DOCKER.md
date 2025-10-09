# Docker Deployment Guide

## Overview
The Tasks Dashboard frontend is containerized using Docker with a multi-stage build:
1. **Build stage**: Compiles the Vue.js application
2. **Production stage**: Serves the static files with Nginx

## Files
- `Dockerfile` - Multi-stage Docker build configuration
- `nginx.conf` - Nginx configuration with API proxy
- `docker-compose.yml` - Orchestrates frontend and backend services
- `.dockerignore` - Excludes unnecessary files from the Docker build

## Quick Start

### Option 1: Docker Compose (Recommended)
Run both frontend and backend together:

```bash
# Build and start all services
docker-compose up --build

# Run in detached mode
docker-compose up -d --build

# Stop all services
docker-compose down
```

**Note**: Update the `backend` service in `docker-compose.yml` with your actual backend image name.

### Option 2: Standalone Frontend Container
Run only the frontend (backend must be running separately):

```bash
# Build the image
docker build -t tasks-dashboard .

# Run the container
docker run -d -p 3000:80 --name tasks-dashboard tasks-dashboard

# Stop the container
docker stop tasks-dashboard
docker rm tasks-dashboard
```

## Access
- **Frontend**: http://localhost:3000
- **Backend API** (proxied through frontend): http://localhost:3000/api
- **Backend direct**: http://localhost:8080

## Architecture

### Multi-stage Build
The Dockerfile uses a two-stage build to minimize the final image size:
- **Build stage** (~500MB): Node.js image compiles the Vue app
- **Production stage** (~25MB): Nginx Alpine image serves static files

### Nginx Configuration
- Serves Vue.js SPA from `/usr/share/nginx/html`
- Proxies `/api/*` requests to the backend service on port 8080
- Handles client-side routing (Vue Router)
- Enables gzip compression
- Caches static assets for 1 year

### Network
Both services communicate on a shared Docker network (`tasks-network`), allowing the frontend to reach the backend using the hostname `backend:8080`.

## Customization

### Change Ports
Edit `docker-compose.yml`:
```yaml
services:
  frontend:
    ports:
      - "YOUR_PORT:80"  # Change 3000 to your desired port
```

### Environment Variables
Add environment variables to the backend service:
```yaml
services:
  backend:
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/tasks
      - SPRING_PROFILES_ACTIVE=prod
```

### Backend Image
Update the backend service to build from source:
```yaml
services:
  backend:
    build:
      context: ../path-to-backend
      dockerfile: Dockerfile
    # ... rest of config
```

## Production Considerations

1. **HTTPS**: Use a reverse proxy (Traefik, Nginx) for SSL termination
2. **Environment Variables**: Use Docker secrets or env files for sensitive data
3. **Resource Limits**: Add CPU/memory limits to services
4. **Health Checks**: Add health check endpoints and Docker health checks
5. **Logging**: Configure log drivers for centralized logging

Example with resource limits:
```yaml
services:
  frontend:
    deploy:
      resources:
        limits:
          cpus: '0.5'
          memory: 512M
```

## Troubleshooting

### Frontend can't connect to backend
- Ensure both services are on the same network
- Check the backend hostname in `nginx.conf` matches the service name
- Verify backend CORS configuration allows the frontend origin

### Build fails
```bash
# Clear Docker cache and rebuild
docker-compose build --no-cache
```

### Check logs
```bash
# View all logs
docker-compose logs

# View specific service logs
docker-compose logs frontend
docker-compose logs backend

# Follow logs
docker-compose logs -f
```

### Access container shell
```bash
docker exec -it tasks-dashboard sh
```

## Development vs Production

**Development** (with Vite proxy):
- Hot reload
- Source maps
- Development server

**Production** (with Docker/Nginx):
- Optimized build
- Gzip compression
- Asset caching
- No source maps
- Smaller bundle size
