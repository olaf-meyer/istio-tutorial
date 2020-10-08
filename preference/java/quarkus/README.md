# How to create new images

Create executable with podman

``` bash
mvn clean package -DskipTests -Pnative -Dquarkus.native.container-build=true -Dquarkus.native.container-runtime=podman
```

For docker the command should looks like this:

``` bash
mvn clean package -DskipTests -Pnative -Dquarkus.native.container-build=true -Dquarkus.native.container-runtime=docker
```

Build the image

``` bash
docker build -t quay.io/omeyer/istio-tutorial-preference:v1.0-kafka -f src/main/docker/Dockerfile.native .
```

Verify image creation

``` bash
docker images|grep -i preference
```

Push image
``` bash
docker push quay.io/omeyer/istio-tutorial-preference:v1.0-kafka
```
