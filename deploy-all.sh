#!/bin/bash

# package the latest docker images
(cd cloudmatch-db/; sbt docker:publishLocal; cd ..)
(cd cloudmatch-service/; sbt docker:publishLocal; cd ..)

# deploy all the things!
kubectl apply -f cloudmatch-db/src/deployment/cloudmatch-db.yaml
kubectl apply -f cloudmatch-service/src/deployment/cloudmatch-service.yaml
