# TODO änderungen von den kubernetes deployments nach openshift templates nachziehen!!

# Setup demo application to log calls to a Kafka topic

1. Install operators. After executing the command, wait some time till the operators are

    ``` bash
    oc process -f istiofiles/setup_operators.yaml |oc create -f - -n openshift-operators
    ```

1. Setup projects

    ``` bash
    oc process -f istiofiles/setup_projects.yaml |oc create -f - -n istio-system
    ```

1. Setup kafka cluster

    ``` bash
    oc process -f istiofiles/setup_kafka_cluster.yaml -p DEPLOYMENT_NAMESPACE=kafka |oc create -f -
    ```

1. Copy secrets from kafka users to demo-app project

    ``` bash
    oc get secret --namespace kafka -l strimzi.io/cluster=demo-app-log-cluster,strimzi.io/kind=KafkaUser -o json | sed 's/"namespace"\:\s*"kafka"/"namespace": "demo-app"/g'|jq 'del(.items[].metadata.ownerReferences)'|oc apply -f - -n demo-app
    ```

1. Setup istio control plane

    ``` bash
    oc process -f istiofiles/setup_istio.yaml |oc create -f - -n istio-system
    ```

1. Deploy example to OpenShift Project

    ``` bash
    oc process -f istiofiles/setup_demo_app.yaml -p DEPLOYMENT_NAMESPACE=demo-app -p CLUSTER_DOMAIN=apps-crc.testing |oc create -f -
    ```

1. Delete pod in kafka project to inject Istio sidecar container

    ``` bash
    oc delete pod -n kafka --all
    ```

1. Deploy istio configuration

    ``` bash
    oc process -f istiofiles/setup_istio_for_demo_app.yaml -p DEPLOYMENT_NAMESPACE=demo-app -p CLUSTER_DOMAIN=apps-crc.testing -p CONTROLPLANE_NAMESPACE=istio-system -p KAFKA_NAMESPACE=kafka|oc create -f -
    ```

1. Test application

    ``` bash
    ./scripts/run_demo_application_gateway.sh
    ```

## How to deployment demo app in Kubernetes

1. Create namespaces

    ``` bash
    kubectl create namespace istio-system
    kubectl create namespace demo-app
    kubectl create namespace kafka
    ```

# How to install Istio:

Download Istio with this command:

``` bash
curl -L https://istio.io/downloadIstio | sh -
```

Move to the Istio package directory. For example, if the package is istio-1.7.4:

``` bash
$ cd istio-1.7.4
```

Add the istioctl client to your path (Linux or macOS):

``` bash
$ export PATH=$PWD/bin:$PATH
```

For this installation, we use the demo configuration profile. It’s selected to have a good set of defaults for testing, but there are other profiles for production or performance testing.

``` bash
$ istioctl install --set profile=demo
✔ Istio core installed
✔ Istiod installed
✔ Egress gateways installed
✔ Ingress gateways installed
✔ Installation complete
```

Add a namespace label to instruct Istio to automatically inject Envoy sidecar proxies when you deploy your application later:

``` bash
$ kubectl label namespace kafka istio-injection=enabled
namespace/kafka labeled
$ kubectl label namespace demo-app istio-injection=enabled
namespace/demo-app labeled
```

``` bash
kubectl apply -f samples/addons
```

Ignore the error message `unable to recognize "samples/addons/kiali.yaml": no matches for kind "MonitoringDashboard" in version "monitoring.kiali.io/v1alpha1"` for a demo setup.

More details can be found here: [Getting started with Istio](https://istio.io/latest/docs/setup/getting-started/)
All available options for the installation are described on this web site: [Istio Installation guides](https://istio.io/latest/docs/setup/install/)

1. Setup demo app in namespace demo app

    ``` bash
    kubectl create -f istiofiles/setup_demo_app_kubernetes.yaml
    ```

1. Deploy kafka operator in namespace kafka

    ``` bash
    kubectl apply -f 'https://strimzi.io/install/latest?namespace=kafka' -n kafka
    ```

1. Create kafka cluster in namespace kafka

    ``` bash
    kubectl create -f istiofiles/setup_kafka_cluster_kubernetes.yaml
    ```

1. Enable auto injection in namespace demo-app

    ``` bash
    kubectl label namespace demo-app istio-injection=enabled
    ```

1. Delete pods in namespace demo-app so that sidecar container can be injected

    ``` bash
    kubectl delete pods -n demo-app --all
    ```

1. Update sidecar injector not to inject a side car into the strimzi controller

    ``` bash
    kubectl edit cm istio-sidecar-injector -n istio-system
    ```

    Replace the neverInjectSelector with this expression

    ``` yaml
    neverInjectSelector:
      - matchExpressions:
        - {key: strimzi.io/kind, operator: In, values: [cluster-operator]}
      - matchExpressions:
        - {key: app-type, operator: In, values: [entity-operator]}
    ```

    Delete istiod pod to reload settings

    ``` bash
    kubectl delete pod -n istio-system -l app=istiod
    ```

1. Enable auto injection in namespace kafka

    ``` bash
    kubectl label namespace kafka istio-injection=enabled
    ```

1. Delete pods in namespace kafka so that sidecar container can be injected

    ``` bash
    kubectl delete po -n kafka -l strimzi.io/cluster=demo-app-log-cluster
    ```

1. Setup istio config for demo application
ToDo Update istio seeting
    ``` bash
    kubectl create -f istiofiles/setup_istio_for_demo_app_kubernetes.yaml
    ```

## TODO Add environment variable to setup tp kafka cluster

kubectl run kafdrop --port=9000 --env="KAFKA_BROKERCONNECT=demo-app-log-cluster-kafka-bootstrap.kafka.svc.cluster.local:9092" --image=obsidiandynamics/kafdrop -n kafka


Peer stuff is missing

kubectl get secret --namespace kafka -l strimzi.io/cluster=demo-app-log-cluster,strimzi.io/kind=KafkaUser -o json | sed 's/"namespace"\:\s*"kafka"/"namespace": "demo-app"/g'|jq 'del(.items[].metadata.ownerReferences)'|kubectl apply -f - -n demo-app


In configmap istio-system/istio-sidecar-injector change value for key .Values.global.proxy.privileged to true to allow execution of sudo command.


Verify ssl handshare via tcp dump
sudo tcpdump -ni eth0 "tcp port 9092 and (tcp[((tcp[12] & 0xf0) >> 2)] = 0x16)"
Source https://stackoverflow.com/questions/39624745/capture-only-ssl-handshake-with-tcpdump
or 
openssl s_client -showcerts -servername -connect demo-app-log-cluster-kafka-bootstrap.kafka.svc.cluster.local:9092 </dev/null

Prüfen ob man parameter -servername wirklick braucht?

Beim Artikel Links zu Quarkus Seite für Kafka angeben!