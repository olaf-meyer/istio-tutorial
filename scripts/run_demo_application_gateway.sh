#!/bin/bash
#set -x

NAMESPACE=$1
if [ -z "$NAMESPACE" ]
  then
    NAMESPACE="demo-app"
fi
## Extract project as parameter in script call
DEMO_APP_DOMAIN=`oc get gateway -n $NAMESPACE -o json|jq -r ".items[0].spec.servers[0].hosts[0]"`
while true
do curl $DEMO_APP_DOMAIN/customer
sleep .5
done
