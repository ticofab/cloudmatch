#!/bin/bash

# clean all services from the cluster
CLEAN_SCRIPT="clean-cluster.sh"
echo "executing "$CLEAN_SCRIPT
. "$CLEAN_SCRIPT"

# deploy all services
SERVICE_SCRIPT="deploy-service.sh"

echo "deploying services with "$SERVICE_SCRIPT
. "$SERVICE_SCRIPT" cloudmatch-service
. "$SERVICE_SCRIPT" cloudmatch-db
. "$SERVICE_SCRIPT" cloudmatch-stream
