# run 'STREAM_REGISTRY_DEBUG_SUSPEND=y make debug' in order to enable suspend
STREAM_REGISTRY_DEBUG_SUSPEND ?= n

.PHONY: clean tests build run debug all just-deploy deploy ci-setup ci-deploy

clean:
	./mvnw clean

tests:
	./mvnw clean test -B

build:
	./mvnw clean install -B

run:
	java --add-opens java.base/java.lang=ALL-UNNAMED -Xmx2G -server -XX:+UseG1GC -XX:MaxGCPauseMillis=20 -XX:InitiatingHeapOccupancyPercent=35 -XX:+ExplicitGCInvokesConcurrent -Djava.awt.headless=true -jar assembly/target/stream-registry*SNAPSHOT.jar server config-dev.yaml

debug:
	java -Xmx2G -server -XX:+UseG1GC -XX:MaxGCPauseMillis=20 -XX:InitiatingHeapOccupancyPercent=35 -XX:+ExplicitGCInvokesConcurrent -Djava.awt.headless=true -agentlib:jdwp=transport=dt_socket,server=y,suspend=$(STREAM_REGISTRY_DEBUG_SUSPEND),address=5105 -jar assembly/target/stream-registry*SNAPSHOT.jar server config-dev.yaml

all: build

# invoke -Psigned profile for signing artifacts
just-deploy:
	./mvnw deploy -DskipTests -Psigned -B

deploy: all just-deploy

