# Performance Engineering Lab

## Project Goal

This project is **NOT** intended to become a production-grade authentication system.

The primary objective is to build a realistic Java Spring Boot microservice that can be used to learn and demonstrate:

- Spring Boot
- Spring Security
- JWT Authentication
- Docker
- Kubernetes
- Prometheus
- Grafana
- Performance Engineering
- JVM Internals
- Heap Dumps
- Thread Dumps
- GC Analysis
- Load Testing using k6 and JMeter

Ultimately this project will become a Kubernetes based performance engineering playground.

---

# Current Environment

Operating System

- Windows 11

Development

- VS Code
- Git
- GitHub
- Docker Desktop
- Kubernetes enabled in Docker Desktop

Java

- JDK 17

Maven

- Apache Maven 3.9.16

Git

Repository:

https://github.com/shriramgokhalemalya/performance-engineering-lab

Branch:

Current working branch during development:

feature/login-api

Main branch:

main

---

# Spring Boot

Generated using Spring Initializr.

Version

Spring Boot 3.5.x

Java

17

Packaging

Jar

Dependencies

- Spring Web
- Spring Security
- Spring Boot Actuator
- Micrometer Prometheus Registry
- JJWT
- Lombok
- Spring Boot Test
- Spring Security Test

---

# Current Package Structure

com.perflab.login

```
config
constant
controller
dto
exception
lab
metrics
model
security
service
util
validator
```

---

# Current Status

Completed

- Spring Boot project generated
- Maven build successful
- Application starts successfully
- Actuator working
- /actuator/health returns UP
- Git repository initialized
- GitHub configured
- Main branch standardized
- Source files moved into Maven standard path: src/main/java
- Custom SecurityFilterChain implemented
- Login API implemented
- AuthService implemented
- Static admin/password login validation implemented
- JWT generation implemented
- JWT validation filter implemented
- Protected API implemented
- Maven tests passing
- Dockerfile added
- Docker image built locally as login-service:0.0.1
- Dockerfile simplified to build from checked-out source code
- GitHub Actions release workflow added
- GitHub Container Registry image publishing flow added
- Kubernetes namespace, Secret, Deployment, and Service added
- Kubernetes Deployment updated to pull image from GHCR
- login-service deployed successfully to Docker Desktop Kubernetes
- Kubernetes login and protected endpoint flow verified through port-forward

---

# Completed Authentication Flow

Public login endpoint:

POST

```
/api/auth/login
```

Request

```json
{
    "username":"admin",
    "password":"password"
}
```

Successful response

```json
{
    "token":"<jwt-token>",
    "expiresIn":3600
}
```

Invalid credentials return:

```http
401 Unauthorized
```

Protected endpoint:

GET

```
/api/users/me
```

Requires:

```
Authorization: Bearer <jwt-token>
```

Successful response:

```json
{
    "username":"admin"
}
```

Without a valid token, protected endpoints return:

```http
401 Unauthorized
```

---

# Current Security Configuration

Implemented SecurityConfig:

- CSRF disabled
- Stateless sessions
- Public endpoints:

```
/api/auth/**
/actuator/**
```

- Every other endpoint requires authentication
- HTTP Basic disabled
- Form login disabled
- JWT filter added before UsernamePasswordAuthenticationFilter
- Empty UserDetailsService bean added to prevent generated Spring Security password

---

# Docker Configuration

Dockerfile:

- Uses multi-stage Docker build
- Build stage uses:

```
maven:3.9-eclipse-temurin-17
```

- Runtime stage uses:

```
eclipse-temurin:17-jre
```

- Dockerfile copies source from the Docker build context:

```
COPY pom.xml .
COPY src ./src
```

- Dockerfile builds the application inside Docker:

```
mvn clean package
```

- Final image contains only the runnable Spring Boot jar:

```
/app/app.jar
```

Important decision:

- Dockerfile does NOT clone the Git repository.
- GitHub Actions already checks out the release source before building.
- Therefore Docker builds from the checked-out source.

Local Docker build command:

```powershell
docker build -t login-service:local .
```

---

# GitHub Actions Release Flow

Workflow file:

```
.github/workflows/release-image.yml
```

Trigger:

- Runs when a GitHub Release is published

```yaml
on:
  release:
    types:
      - published
```

Runner:

```yaml
runs-on: ubuntu-latest
```

Meaning:

- The workflow runs on a GitHub-hosted Linux machine.
- It does NOT run locally.

Workflow behavior:

- Checks out repository source using `actions/checkout`
- Sets up Docker Buildx
- Logs in to GitHub Container Registry using `GITHUB_TOKEN`
- Builds the Docker image
- Pushes the Docker image to GitHub Container Registry

Published image names:

```text
ghcr.io/shriramgokhalemalya/login-service:<release-tag>
ghcr.io/shriramgokhalemalya/login-service:latest
```

Example:

If GitHub Release is:

```text
v0.0.1
```

Images pushed:

```text
ghcr.io/shriramgokhalemalya/login-service:v0.0.1
ghcr.io/shriramgokhalemalya/login-service:latest
```

Required GitHub permissions:

```yaml
permissions:
  contents: read
  packages: write
```

Current approach:

- Kubernetes pulls `latest` for now.
- Versioned tags are also created and can be used later for safer production-style deployment.

---

# Kubernetes Configuration

Kubernetes manifests are in:

```
k8s/
```

Files:

- `namespace.yaml`
- `secret.yaml`
- `deployment.yaml`
- `service.yaml`
- `README.md`

Namespace:

```text
performance-lab
```

Deployment name:

```text
login-service
```

Service name:

```text
login-service
```

Service type:

```text
ClusterIP
```

Container image currently configured:

```yaml
image: ghcr.io/shriramgokhalemalya/login-service:latest
imagePullPolicy: Always
```

Reason:

- After a GitHub Release, GitHub Actions pushes `latest`.
- When Kubernetes pods restart, Docker Desktop Kubernetes pulls the latest image from GHCR.

JWT secret is provided through Kubernetes Secret:

```yaml
name: login-service-secret
key: JWT_SECRET
```

Application properties support environment overrides:

```properties
jwt.secret=${JWT_SECRET:performance-lab-jwt-secret-key-for-learning-only-2026}
jwt.expiration-seconds=${JWT_EXPIRATION_SECONDS:3600}
```

Deploy or update Kubernetes resources:

```powershell
kubectl apply -f k8s
```

Restart deployment to pull latest image:

```powershell
kubectl rollout restart deployment/login-service -n performance-lab
```

Wait for rollout:

```powershell
kubectl rollout status deployment/login-service -n performance-lab
```

Check pods and service:

```powershell
kubectl get pods,svc -n performance-lab
```

Port forward for local testing:

```powershell
kubectl port-forward service/login-service 8080:8080 -n performance-lab
```

Test login:

```powershell
Invoke-RestMethod -Method Post `
  -Uri http://localhost:8080/api/auth/login `
  -ContentType "application/json" `
  -Body '{"username":"admin","password":"password"}'
```

Test protected endpoint:

```powershell
$login = Invoke-RestMethod -Method Post `
  -Uri http://localhost:8080/api/auth/login `
  -ContentType "application/json" `
  -Body '{"username":"admin","password":"password"}'

Invoke-RestMethod -Method Get `
  -Uri http://localhost:8080/api/users/me `
  -Headers @{ Authorization = "Bearer $($login.token)" }
```

Expected protected endpoint response:

```json
{
    "username": "admin"
}
```

Important note:

- If GHCR package is public, no Kubernetes imagePullSecret is needed.
- If GHCR package is private, Kubernetes needs an image pull secret for `ghcr.io`.

---

# Current Release To Kubernetes Flow

High-level flow:

1. Code is pushed to GitHub.
2. A GitHub Release is published.
3. GitHub Actions runs on GitHub.
4. GitHub Actions builds Docker image.
5. GitHub Actions pushes image to GHCR.
6. Local Kubernetes deployment is applied or restarted using PowerShell.
7. Docker Desktop Kubernetes pulls image from GHCR.
8. Service is tested through `kubectl port-forward`.

Commands after GitHub Release is complete:

```powershell
kubectl apply -f k8s
kubectl rollout restart deployment/login-service -n performance-lab
kubectl rollout status deployment/login-service -n performance-lab
kubectl port-forward service/login-service 8080:8080 -n performance-lab
```

---

# Project Roadmap

Sprint 1

- Completed: Environment Setup

Sprint 2

- Completed: Login API
- Completed: Service Layer
- Completed: Static User Store

Sprint 3

- Completed: JWT
- Completed: JWT Validation
- Completed: Protected APIs

Sprint 4

Docker

- Completed: Dockerfile
- Completed: Build Image
- Completed: GitHub Actions release image workflow
- Completed: Push image to GHCR on GitHub Release
- Pending: Run Container directly with docker run

Sprint 5

Kubernetes

- Completed: Namespace
- Completed: Deployment
- Completed: Service
- Completed: Secret
- Completed: Pull image from GHCR
- Pending: ConfigMap
- Pending: Ingress
- Pending: HPA

Sprint 6

Monitoring

- Spring Boot Actuator
- Prometheus
- Grafana
- JVM Metrics

Sprint 7

Load Testing

- k6
- JMeter
- NeoLoad

Sprint 8

Performance Lab

Add special endpoints.

```
/lab/cpu

/lab/memory

/lab/thread

/lab/deadlock

/lab/gc
```

These endpoints intentionally generate

- CPU spikes
- Memory pressure
- Thread starvation
- Deadlocks
- GC pressure

Purpose is to learn

- Heap Dumps
- Thread Dumps
- VisualVM
- Java Mission Control
- Eclipse MAT

---

# Kubernetes Goals

Eventually deploy

login-service

Later

user-service

Later

audit-service

Architecture

```
k6/JMeter

        |

login-service

    |

-------------------------

|                       |

user-service      audit-service
```

---

# Monitoring Stack

Micrometer

↓

Prometheus

↓

Grafana

Need dashboards for

- JVM Heap
- GC
- CPU
- Memory
- Threads
- HTTP Requests
- Response Time

---

# Coding Guidelines

Keep architecture clean.

Controllers

↓

Services

↓

Utility Classes

Avoid business logic in controllers.

Small commits.

One feature per commit.

---

# Git Workflow

main

feature/login-api

feature/jwt

feature/docker

feature/kubernetes

feature/prometheus

feature/loadtest

feature/jvm-analysis

---

# Long Term Objective

This repository will become an interview demonstration project showing hands-on experience with

- Java Performance
- Kubernetes
- Docker
- Spring Boot
- JVM Internals
- Performance Engineering
- Site Reliability Engineering
