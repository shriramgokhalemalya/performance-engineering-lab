# Kubernetes

Build the image:

```powershell
.\mvnw.cmd clean package
docker build -t login-service:0.0.1 .
```

Deploy to Docker Desktop Kubernetes:

```powershell
kubectl apply -f k8s/
kubectl rollout status deployment/login-service -n performance-lab
```

Test locally with port forwarding:

```powershell
kubectl port-forward service/login-service 8080:8080 -n performance-lab
```

Then call:

```powershell
Invoke-RestMethod -Method Post `
  -Uri http://localhost:8080/api/auth/login `
  -ContentType "application/json" `
  -Body '{"username":"admin","password":"password"}'
```
