# Setup demo application to log calls to a Kafka topic

1. Install operators

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

1. Setup istio control plane

    ``` bash
    oc process -f istiofiles/setup_istio.yaml |oc create -f - -n istio-system
    ```

1. Deploy example to OpenShift Project

    ``` bash
    oc process -f istiofiles/setup_demo_app.yaml -p DEPLOYMENT_NAMESPACE=demo-app -p CLUSTER_DOMAIN=apps-crc.testing |oc create -f -
    ```

1. Deploy istio configuration

    ``` bash
    oc process -f istiofiles/setup_istio_for_demo_app.yaml -p DEPLOYMENT_NAMESPACE=demo-app -p CLUSTER_DOMAIN=apps-crc.testing -p CONTROLPLANE_NAMESPACE=istio-system|oc create -f -
    ```

1. Test application

    ``` bash
    ./scripts/run_demo_application_gateway.sh
    ```

## How to deployment demo app in Kubernetes

1. Create namespaces

    ``` bash
    kubectl create istio-system demo-app kafka
    ```

## TODO section missing how to install istio and kiali and so on

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

    ``` bash
    kubectl create -f istiofiles/setup_istio_for_demo_app_kubernetes.yaml
    ```
