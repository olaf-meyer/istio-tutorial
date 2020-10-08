#!/bin/bash
#set -x

## Extract project as parameter in script call
export INGRESS_HOST=$(minikube ip)
export INGRESS_PORT=$(kubectl -n istio-system get service istio-ingressgateway -o jsonpath='{.spec.ports[?(@.name=="http2")].nodePort}')

export DEMO_APP_DOMAIN=$INGRESS_HOST:$INGRESS_PORT

#DEMO_APP_DOMAIN=`oc get gateway -n demo-app -o json|jq -r ".items[0].spec.servers[0].hosts[0]"`
while true
do curl $DEMO_APP_DOMAIN/customer
sleep .5
done
