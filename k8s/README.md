# Kubernetes

Build a local image:

```powershell
.\mvnw.cmd clean package
docker build -t login-service:0.0.1 .
```

Build the image directly from the current repository checkout:

```powershell
docker build -t login-service:git-build .
```

GitHub Actions release image flow:

- Create and publish a GitHub Release.
- GitHub checks out the release source.
- The workflow builds the image from that checked-out source.
- The image is pushed to GitHub Container Registry.

Published image tags:

```text
ghcr.io/shriramgokhalemalya/login-service:<release-tag>
ghcr.io/shriramgokhalemalya/login-service:latest
```

The Kubernetes deployment pulls the release image from GitHub Container Registry:

```yaml
image: ghcr.io/shriramgokhalemalya/login-service:v0.0.1
imagePullPolicy: Always
```

For now, `k8s/deployment.yaml` uses:

```yaml
image: ghcr.io/shriramgokhalemalya/login-service:latest
imagePullPolicy: Always
```

This makes Kubernetes pull the latest release image each time the deployment is restarted or recreated.

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
