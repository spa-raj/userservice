#!/usr/bin/env bash
set -euo pipefail

NAMESPACE="vibevault"
JOB_NAME="seed-users"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

# Resolve latest image from the running deployment
IMAGE=$(kubectl get deployment userservice -n "$NAMESPACE" -o jsonpath='{.spec.template.spec.containers[0].image}' 2>/dev/null || echo "")

if [ -z "$IMAGE" ]; then
    echo "ERROR: Could not resolve image from userservice deployment."
    echo "Usage: Set image manually by editing seed-job.yaml or ensure the deployment exists."
    exit 1
fi

echo "Using image: $IMAGE"

# Delete previous job if it exists
kubectl delete job "$JOB_NAME" -n "$NAMESPACE" --ignore-not-found

# Apply the job with the resolved image
sed "s|image: IMAGE_PLACEHOLDER|image: $IMAGE|" "$SCRIPT_DIR/seed-job.yaml" | kubectl apply -f -

echo "Job '$JOB_NAME' created. Following logs..."
echo "---"

# Wait for the pod to be created, then follow logs
kubectl wait --for=condition=Ready pod -l "purpose=seed-data,app=userservice" -n "$NAMESPACE" --timeout=120s 2>/dev/null || true
kubectl logs -f "job/$JOB_NAME" -n "$NAMESPACE"
