#!/bin/bash

# cleanup anything from the kubernetes cluster
kubectl delete -f cloudmatch-db/src/deployment/cloudmatch-db.yaml
kubectl delete -f cloudmatch-service/src/deployment/cloudmatch-service.yaml
kubectl delete -f cloudmatch-stream/src/deployment/cloudmatch-stream.yaml
