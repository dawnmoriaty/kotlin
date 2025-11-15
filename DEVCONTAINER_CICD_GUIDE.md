# 🚀 DevContainer & CI/CD Setup Guide

## 📋 Overview

Dự án đã được setup với:
- ✅ **DevContainer** - Development environment trong Docker
- ✅ **Multi-stage Dockerfile** - Optimize image size
- ✅ **GitHub Actions CI/CD** - Auto build, test, deploy
- ✅ **Production Docker Compose** - Production-ready setup
- ✅ **Automated Backups** - Daily database backups

---

## 🎯 DevContainer Setup

### Prerequisites
- Docker Desktop
- VS Code với extension "Dev Containers"

### Quick Start
1. Open project in VS Code
2. Press `F1` → "Dev Containers: Reopen in Container"
3. Wait for container to build (first time takes 5-10 mins)
4. Done! Coding environment ready 🎉

### What You Get
- ✅ JDK 21 (Amazon Corretto)
- ✅ Gradle 8.5
- ✅ PostgreSQL access
- ✅ MinIO access
- ✅ All VS Code extensions installed
- ✅ Auto port forwarding

### Services Available
- **Application**: http://localhost:8080
- **PostgreSQL**: localhost:5432
- **MinIO API**: http://localhost:9000
- **MinIO Console**: http://localhost:9001
- **pgAdmin**: http://localhost:5050

---

## 🔨 Build Commands

### Development
```bash
# Run in DevContainer
./gradlew run

# Build
./gradlew build

# Test
./gradlew test

# Clean build
./gradlew clean build
```

### Docker
```bash
# Build Docker image
docker build -t financial-app:latest .

# Run with Docker Compose (dev)
docker-compose up -d

# Run with Docker Compose (prod)
docker-compose -f docker-compose.prod.yml up -d

# Stop services
docker-compose down

# View logs
docker-compose logs -f app
```

---

## 🚀 CI/CD Pipeline

### GitHub Actions Workflow

Pipeline tự động chạy khi:
- Push to `main` or `develop` branch
- Pull request to `main` or `develop`
- Manual trigger

### Pipeline Stages

#### 1. Build & Test
- ✅ Checkout code
- ✅ Setup JDK 21
- ✅ Cache Gradle dependencies
- ✅ Build application
- ✅ Run tests
- ✅ Upload artifacts

#### 2. Code Quality
- ✅ Kotlin linter (detekt)
- ✅ Dependency vulnerability scan (Trivy)
- ✅ Upload results to GitHub Security

#### 3. Docker Build
- ✅ Build multi-stage Docker image
- ✅ Push to GitHub Container Registry
- ✅ Scan image for vulnerabilities
- ✅ Tag with branch name, SHA, and 'latest'

#### 4. Deploy Staging (develop branch)
- ✅ Deploy to staging environment
- ✅ Run smoke tests
- ✅ Environment: staging

#### 5. Deploy Production (main branch)
- ✅ Deploy to production
- ✅ Health check
- ✅ Send notifications
- ✅ Environment: production

#### 6. Database Migration (optional)
- ✅ Trigger with commit message `[migrate]`
- ✅ Run migrations before deployment

---

## 📦 Docker Images

### Development Image
- Base: `gradle:8.5-jdk21`
- Size: ~1.5GB (includes build tools)
- Use: DevContainer development

### Production Image (Multi-stage)
- Base: `amazoncorretto:21-alpine`
- Size: ~200-300MB
- Optimized for production
- Non-root user
- Health check included

### Image Registry
```bash
# Images pushed to GitHub Container Registry
ghcr.io/your-username/financial-app:latest
ghcr.io/your-username/financial-app:main
ghcr.io/your-username/financial-app:develop
ghcr.io/your-username/financial-app:main-abc1234
```

---

## 🔐 Secrets & Environment Variables

### Required GitHub Secrets
Setup in: Repository → Settings → Secrets and variables → Actions

```
# None required! Uses GITHUB_TOKEN automatically
# GITHUB_TOKEN has permissions to push to ghcr.io
```

### Production Environment Variables
Copy `.env.example` to `.env` and fill in:

```bash
# Database
DB_USER=financial_user
DB_PASSWORD=<strong-password>
DB_NAME=financial_db_prod

# MinIO
MINIO_ACCESS_KEY=<minio-access-key>
MINIO_SECRET_KEY=<minio-secret-key>

# JWT
JWT_SECRET=<min-256-bits-secret>

# App
APP_ENV=production
DEBUG_MODE=false
```

---

## 🗄️ Database Backups

### Automatic Daily Backups
- ✅ Runs daily at midnight
- ✅ Compressed with gzip
- ✅ Retention: 7 days
- ✅ Location: `./backups/`

### Manual Backup
```bash
# From host
docker-compose exec db pg_dump -U root financial_db_prod | gzip > backup_$(date +%Y%m%d).sql.gz

# From container
docker exec financial_postgres_prod pg_dump -U root financial_db_prod | gzip > backup.sql.gz
```

### Restore Backup
```bash
# Restore from backup
gunzip -c backup_20250115.sql.gz | docker-compose exec -T db psql -U root -d financial_db_prod
```

---

## 🌐 Deployment Options

### Option 1: GitHub Container Registry → Docker Host
```bash
# On production server
docker pull ghcr.io/your-username/financial-app:latest
docker-compose -f docker-compose.prod.yml up -d
```

### Option 2: Self-Hosted GitHub Actions Runner
Setup runner on your server for direct deployment.

### Option 3: Kubernetes
```bash
# Use kubectl or Helm
kubectl apply -f k8s/
```

### Option 4: Cloud Platforms
- **AWS ECS/EKS**: Use Fargate or EC2
- **Google Cloud Run**: Serverless containers
- **Azure Container Instances**: Quick deployment
- **DigitalOcean App Platform**: Simple PaaS
- **Fly.io**: Global edge deployment

---

## 📊 Monitoring & Logging

### Application Logs
```bash
# View app logs
docker-compose logs -f app

# View all logs
docker-compose logs -f

# Last 100 lines
docker-compose logs --tail=100 app
```

### Health Checks
```bash
# Application health
curl http://localhost:8080/

# Database health
docker-compose exec db pg_isready -U root

# MinIO health
curl http://localhost:9000/minio/health/live
```

### Metrics (Future Enhancement)
Consider adding:
- **Prometheus** - Metrics collection
- **Grafana** - Dashboards
- **Loki** - Log aggregation

---

## 🔧 Troubleshooting

### DevContainer Issues

**Problem**: Container fails to start
```bash
# Solution: Rebuild container
F1 → "Dev Containers: Rebuild Container"
```

**Problem**: Port conflicts
```bash
# Solution: Check ports in use
docker ps
# Stop conflicting containers
docker stop <container-id>
```

### CI/CD Issues

**Problem**: Build fails
```bash
# Check GitHub Actions logs
# Repository → Actions → Select failed workflow → View logs
```

**Problem**: Docker push fails
```bash
# Ensure GITHUB_TOKEN has package write permissions
# Settings → Actions → General → Workflow permissions → Read and write
```

### Docker Issues

**Problem**: Out of disk space
```bash
# Clean up Docker
docker system prune -a --volumes
```

**Problem**: Database connection refused
```bash
# Wait for healthcheck to pass
docker-compose ps
# Check logs
docker-compose logs db
```

---

## 🎯 Best Practices

### Development
1. ✅ Use DevContainer for consistent environment
2. ✅ Run tests before committing
3. ✅ Keep dependencies updated
4. ✅ Use meaningful commit messages

### CI/CD
1. ✅ Always test in staging before production
2. ✅ Use semantic versioning for releases
3. ✅ Tag Docker images properly
4. ✅ Monitor pipeline failures

### Security
1. ✅ Never commit secrets to Git
2. ✅ Use environment variables
3. ✅ Scan images for vulnerabilities
4. ✅ Keep base images updated
5. ✅ Use non-root user in containers

### Production
1. ✅ Use Docker Compose production file
2. ✅ Enable HTTPS with SSL certificates
3. ✅ Set up regular backups
4. ✅ Monitor resource usage
5. ✅ Implement log rotation

---

## 📝 Quick Reference

### Development Workflow
```bash
# 1. Start DevContainer in VS Code
# 2. Make changes
# 3. Test locally
./gradlew test

# 4. Commit & push
git add .
git commit -m "feat: add new feature"
git push origin develop

# 5. CI/CD automatically:
#    - Builds
#    - Tests
#    - Creates Docker image
#    - Deploys to staging
```

### Production Deployment
```bash
# 1. Merge to main branch
git checkout main
git merge develop
git push origin main

# 2. CI/CD automatically:
#    - Builds production image
#    - Runs tests
#    - Deploys to production
#    - Runs health checks

# 3. Verify deployment
curl https://your-domain.com/
```

### Emergency Rollback
```bash
# Roll back to previous version
docker-compose pull
docker tag ghcr.io/your-username/financial-app:previous ghcr.io/your-username/financial-app:latest
docker-compose up -d
```

---

## ✅ Checklist

### Initial Setup
- [ ] Install Docker Desktop
- [ ] Install VS Code + Dev Containers extension
- [ ] Clone repository
- [ ] Open in DevContainer
- [ ] Run `./gradlew build`
- [ ] Test services (DB, MinIO, App)

### Before First Deploy
- [ ] Set up GitHub repository
- [ ] Configure GitHub Actions secrets (if needed)
- [ ] Enable GitHub Container Registry
- [ ] Set workflow permissions (read & write)
- [ ] Update `.env.example` with your values
- [ ] Configure production environment

### Production Checklist
- [ ] Set strong passwords in `.env`
- [ ] Enable HTTPS/SSL
- [ ] Configure backups
- [ ] Set up monitoring
- [ ] Test health checks
- [ ] Document deployment process
- [ ] Create disaster recovery plan

---

## 🎉 Summary

**Congratulations!** Dự án đã có:

1. ✅ **DevContainer** - Consistent dev environment
2. ✅ **CI/CD Pipeline** - Automated build, test, deploy
3. ✅ **Multi-stage Docker** - Optimized images
4. ✅ **Production Setup** - Ready for deployment
5. ✅ **Automated Backups** - Data safety
6. ✅ **Security Scanning** - Vulnerability detection

**Next Steps**:
1. Test DevContainer locally
2. Push to GitHub to trigger CI/CD
3. Deploy to production
4. Monitor & enjoy! 🚀

**Questions?** Check troubleshooting section or GitHub Issues!

