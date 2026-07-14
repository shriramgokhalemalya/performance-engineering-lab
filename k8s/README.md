# Kubernetes Deployment

This service is deployed to Docker Desktop Kubernetes using an image published to GitHub Container Registry.

## Release Image Flow

Create a GitHub Release. The GitHub Actions workflow builds the Docker image and pushes:

```text
ghcr.io/shriramgokhalemalya/login-service:<release-tag>
ghcr.io/shriramgokhalemalya/login-service:latest
```

Example release command from Command Prompt:

```cmd
git tag v0.0.1
git push origin v0.0.1
gh release create v0.0.1 --title "v0.0.1" --notes "Initial login-service release with JWT, Docker, GHCR, and Kubernetes support"
```

Example release command from PowerShell:

```powershell
gh release create v0.0.1 `
  --title "v0.0.1" `
  --notes "Initial login-service release with JWT, Docker, GHCR, and Kubernetes support"
```

## Current Deployment Image

`deployment.yaml` currently pulls the `latest` image from GHCR:

```yaml
image: ghcr.io/shriramgokhalemalya/login-service:latest
imagePullPolicy: Always
```

This means Kubernetes pulls the latest release image whenever the deployment is restarted or recreated.

## Deploy To Kubernetes

Apply the manifests:

```powershell
kubectl apply -f k8s
```

Restart the deployment so it pulls `latest`:

```powershell
kubectl rollout restart deployment/login-service -n performance-lab
```

Wait for rollout:

```powershell
kubectl rollout status deployment/login-service -n performance-lab
```

Check resources:

```powershell
kubectl get pods,svc -n performance-lab
```

## Test Locally

Port-forward the service:

```powershell
kubectl port-forward service/login-service 8080:8080 -n performance-lab
```

Login:

```powershell
$login = Invoke-RestMethod -Method Post `
  -Uri http://localhost:8080/api/auth/login `
  -ContentType "application/json" `
  -Body '{"username":"admin","password":"password"}'
```

Call the protected endpoint:

```powershell
Invoke-RestMethod -Method Get `
  -Uri http://localhost:8080/api/users/me `
  -Headers @{ Authorization = "Bearer $($login.token)" }
```

Expected response:

```json
{
  "username": "admin"
}
```

## Local Docker Build

For local testing without GHCR:

```powershell
docker build -t login-service:local .
```


# Kind Kubernetes Cluster Setup (v0.0.2+)

## Create Cluster
```powershell
kind create cluster --name performance-lab --config cluster/kind-config.yaml
```

## Install Metrics Server
```powershell
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml 
```
Add `--kubelet-insecure-tls` to the metrics-server deployment when using Kind.

## Install NGINX Ingress
```powershell
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/main/deploy/static/provider/kind/deploy.yaml
kubectl get pods -n ingress-nginx
kubectl get svc -n ingress-nginx
kubectl get ingressclass
```

## Deploy
```powershell
kubectl apply -f k8s
kubectl get all -n performance-lab
kubectl get endpoints -n performance-lab
```

## Ingress
```powershell
kubectl apply -f k8s/ingress.yaml
kubectl get ingress -n performance-lab
kubectl describe ingress login-ingress -n performance-lab
```

## Troubleshooting
```powershell
kubectl logs -n ingress-nginx deployment/ingress-nginx-controller
kubectl port-forward service/ingress-nginx-controller 8081:80 -n ingress-nginx
kubectl port-forward service/login-service 8080:8080 -n performance-lab
kubectl get pods -n ingress-nginx -o wide
kubectl get nodes
kubectl top nodes
kubectl top pods -A
docker ps
```

## Scaling the pods

```powershell
kubectl scale deployment login-service --replicas=10 -n performance-lab
kubectl top pods -n performance-lab
kubectl get pods -n performance-lab
```

## URLs
- http://localhost:8080/
- http://localhost:8080/actuator/health
- Debug: http://localhost:8081/
