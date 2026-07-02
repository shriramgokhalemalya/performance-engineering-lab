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

performance-engineering-lab

Branch:

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
- Kubernetes namespace, Secret, Deployment, and Service added
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
- Pending: Run Container directly with docker run

Sprint 5

Kubernetes

- Completed: Namespace
- Completed: Deployment
- Completed: Service
- Completed: Secret
- Pending: ConfigMap
- Ingress
- HPA

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
