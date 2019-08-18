#!/bin/bash

echo "removing from cluster service "$1
kubectl delete -f $1/src/deployment/$1.yaml
