show-images:
	docker images|grep -i kafka
build-customer:
	mvn clean package -f customer/java/quarkus/ -DskipTests -Pnative -Dquarkus.native.container-build=true -Dquarkus.native.container-runtime=podman
	docker build -t quay.io/omeyer/istio-tutorial-customer:v1.0-kafka -f customer/java/quarkus/src/main/docker/Dockerfile.native customer/java/quarkus/
	docker push quay.io/omeyer/istio-tutorial-customer:v1.0-kafka
build-preference:
	mvn clean package -f preference/java/quarkus/ -DskipTests -Pnative -Dquarkus.native.container-build=true -Dquarkus.native.container-runtime=podman
	docker build -t quay.io/omeyer/istio-tutorial-preference:v1.0-kafka -f preference/java/quarkus/src/main/docker/Dockerfile.native preference/java/quarkus/
	docker push quay.io/omeyer/istio-tutorial-preference:v1.0-kafka
build-recommendation:
	mvn clean package -f recommendation/java/quarkus/ -DskipTests -Pnative -Dquarkus.native.container-build=true -Dquarkus.native.container-runtime=podman
	docker build -t quay.io/omeyer/istio-tutorial-recommendation:v2.0-kafka -f recommendation/java/quarkus/src/main/docker/Dockerfile.native recommendation/java/quarkus/
	docker push quay.io/omeyer/istio-tutorial-recommendation:v2.0-kafka
build-consumer:
	mvn clean package -f consumer/java/quarkus/ -DskipTests -Pnative -Dquarkus.native.container-build=true -Dquarkus.native.container-runtime=podman
	docker build -t quay.io/omeyer/istio-tutorial-consumer:v1.0-kafka -f consumer/java/quarkus/src/main/docker/Dockerfile.native consumer/java/quarkus/
	docker push quay.io/omeyer/istio-tutorial-consumer:v1.0-kafka
customer: build-customer show-images
preference: build-preference show-images
recommendation: build-recommendation show-images
consumer: build-consumer show-images
all: build-customer build-preference build-recommendation build-consumer show-images
