# Kubernetes Manifests Plan for Userservice

## Context
The VibeVault userservice is a Spring Boot 4.0.1 microservice (OAuth2 Authorization Server, MySQL/Flyway, JWT) that is already Dockerized. AWS infrastructure (VPC, EKS, RDS, ECR) is deployed via Terraform. We need Kubernetes manifests to deploy this service — first on Minikube locally, then on AWS EKS.

## Directory Structure
```
k8s/
├── namespace.yaml
├── configmap.yaml
├── secret.yaml
├── mysql.yaml
├── deployment.yaml
└── service.yaml
```
All manifests live under `k8s/` at the project root (`/home/sparsh-raj/IdeaProjects/userservice/k8s/`).

---

## 1. Namespace (`namespace.yaml`)
- Create a `vibevault` namespace to isolate all VibeVault resources
- All subsequent manifests will reference `namespace: vibevault`

## 2. ConfigMap (`configmap.yaml`)
Non-sensitive, externalized configuration injected as environment variables:

| Key | Value | Notes |
|-----|-------|-------|
| `PORT` | `8081` | Server port |
| `DB_URL` | `jdbc:mysql://userservice-mysql:3306/userservice?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC` | Minikube default; change for RDS on EKS |
| `ADMIN_EMAIL` | `admin@gmail.com` | Initial admin email |
| `ADMIN_FIRST_NAME` | `Admin` | |
| `ADMIN_LAST_NAME` | `User` | |
| `CLIENT_ID` | `vibevault-client` | OAuth2 client ID |
| `REDIRECT_URI` | `https://oauth.pstmn.io/v1/callback` | OAuth2 redirect |
| `ISSUER_URI` | `http://userservice:8081` | Points to the K8s service name |
| `SPRING_PROFILES_ACTIVE` | `default` | Spring profile |

## 3. Secret (`secret.yaml`)
Sensitive data, base64-encoded (plain values in comments for reference):

| Key | Example Value | Notes |
|-----|---------------|-------|
| `DB_USERNAME` | `root` | DB credentials |
| `DB_PASSWORD` | *(from docker-compose)* | DB credentials |
| `ADMIN_PASSWORD` | *(from docker-compose)* | Initial admin password |
| `CLIENT_SECRET` | *(from docker-compose)* | OAuth2 client secret |

- Type: `Opaque`
- **Note:** On EKS, these should eventually be replaced with AWS Secrets Manager + External Secrets Operator. For now, plain K8s Secrets are fine.

## 4. MySQL (`mysql.yaml`) — Minikube Only
For local testing, a MySQL pod runs inside the cluster:
- **PersistentVolumeClaim:** 1Gi storage for `/var/lib/mysql`
- **Deployment:** Single replica, `mysql:8.0` image, `MYSQL_ROOT_PASSWORD` pulled from `userservice-secret`
- **Service:** ClusterIP on port 3306, name `userservice-mysql` (matches `DB_URL` in ConfigMap)
- **Health checks:** `mysqladmin ping` for both liveness and readiness
- **Strategy:** `Recreate` (not rolling — single DB instance)
- **Note:** On EKS, replace with AWS RDS and update `DB_URL` in ConfigMap accordingly.

## 5. Deployment (`deployment.yaml`)

**Metadata & Labels:**
- `app: userservice`, `version: v1`, `part-of: vibevault`
- Selector: `app: userservice`

**Pod Spec:**
- **Replicas:** 2
- **Container:**
  - Image: `userservice:latest` (Minikube); configurable for ECR (`<account>.dkr.ecr.<region>.amazonaws.com/userservice:<tag>`)
  - `imagePullPolicy: IfNotPresent` (for Minikube local images)
  - Port: `containerPort: 8081`
  - `envFrom:` referencing both ConfigMap and Secret
- **Resource Requests/Limits:**
  - Requests: `cpu: 250m`, `memory: 512Mi`
  - Limits: `cpu: 500m`, `memory: 768Mi`
  - (JVM uses `MaxRAMPercentage=75.0` so ~576Mi heap within 768Mi limit)
- **Health Checks (Spring Boot Actuator):**
  - **Liveness Probe:** `httpGet /actuator/health/liveness`, port 8081, `initialDelaySeconds: 60`, `periodSeconds: 30`, `timeoutSeconds: 5`, `failureThreshold: 3`
  - **Readiness Probe:** `httpGet /actuator/health/readiness`, port 8081, `initialDelaySeconds: 30`, `periodSeconds: 10`, `timeoutSeconds: 5`, `failureThreshold: 3`
  - Uses Kubernetes probes (`management.endpoint.health.probes.enabled=true` already set in application.properties)
- **Strategy:** `RollingUpdate` with `maxUnavailable: 0`, `maxSurge: 1`

## 6. Service (`service.yaml`)
- Type: `ClusterIP` (internal-only; Ingress/LoadBalancer added later)
- Port: `80` -> targetPort: `8081`
- Selector: `app: userservice`
- Service name: `userservice` (so `ISSUER_URI=http://userservice:8081` resolves within the cluster)

---

## Key Files Referenced
| File | Purpose |
|------|---------|
| `src/main/resources/application.properties` | All env vars and actuator config |
| `Dockerfile` | Port 8081, JVM flags, non-root user, health check |
| `docker-compose.yml` | Reference env var values |
| `src/.../security/SecurityConfig.java` | Actuator endpoints are public |

---

## Minikube Testing Workflow

### Full testing steps:

```bash
# 1. Start minikube
minikube start

# 2. Build userservice image inside minikube's docker
eval $(minikube docker-env)
docker build -t userservice:latest .

# 3. Create namespace first
kubectl apply -f k8s/namespace.yaml

# 4. Apply secrets & configmap (mysql needs the secret)
kubectl apply -f k8s/secret.yaml
kubectl apply -f k8s/configmap.yaml

# 5. Deploy MySQL and wait for it to be ready
kubectl apply -f k8s/mysql.yaml
kubectl -n vibevault wait --for=condition=ready pod -l app=userservice-mysql --timeout=120s

# 6. Deploy userservice
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml

# 7. Watch pods come up
kubectl -n vibevault get pods -w

# 8. Once pods are Running, port-forward and test
kubectl -n vibevault port-forward svc/userservice 8081:80
# In another terminal:
curl http://localhost:8081/actuator/health
```

### Useful debugging commands:
```bash
# Check pod logs
kubectl -n vibevault logs -f deployment/userservice

# Describe pod if it's crash-looping
kubectl -n vibevault describe pod -l app=userservice

# Check MySQL is accessible
kubectl -n vibevault exec deployment/userservice-mysql -- mysqladmin ping -h localhost

# Check all resources
kubectl -n vibevault get all

# Clean up everything
kubectl delete namespace vibevault
```

### Implementation Order
1. `namespace.yaml`
2. `secret.yaml`
3. `configmap.yaml`
4. `mysql.yaml` (wait for ready)
5. `deployment.yaml`
6. `service.yaml`
