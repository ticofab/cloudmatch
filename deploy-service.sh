#!/bin/bash

echo "building and deployment of service "$1
(cd $1/; sbt docker:publishLocal; cd ..)
kubectl apply -f $1/src/deployment/$1.yaml
