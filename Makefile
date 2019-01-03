# run 'STREAM_REGISTRY_DEBUG_SUSPEND=y make debug' in order to enable suspend
STREAM_REGISTRY_DEBUG_SUSPEND ?= n
STREAM_REGISTRY_DEBUG_PORT ?= 5105

STREAM_REGISTRY_HEAP_OPTS ?= -Xmx2g
STREAM_REGISTRY_JAVA_OPTS = $(STREAM_REGISTRY_HEAP_OPTS) --add-opens java.base/java.lang=ALL-UNNAMED -server -XX:+UseG1GC -XX:MaxGCPauseMillis=20 -XX:InitiatingHeapOccupancyPercent=35 -XX:+ExplicitGCInvokesConcurrent -Djava.awt.headless=true
STREAM_REGISTRY_DEBUG_OPTS = $(STREAM_REGISTRY_JAVA_OPTS) --add-opens java.base/java.lang=ALL-UNNAMED -agentlib:jdwp=transport=dt_socket,server=y,suspend=$(STREAM_REGISTRY_DEBUG_SUSPEND),address=$(STREAM_REGISTRY_DEBUG_PORT)

.PHONY: clean tests build run debug all just-deploy deploy ci-setup ci-deploy

clean:
	./mvnw clean

tests:
	./mvnw clean test -B

build:
	./mvnw clean install -B

run:
	MAVEN_OPTS="$(STREAM_REGISTRY_JAVA_OPTS)" ./mvnw exec:java

debug:
	MAVEN_OPTS="$(STREAM_REGISTRY_DEBUG_OPTS)" ./mvnw exec:java

all: build

# invoke -Psigned profile for signing artifacts
just-deploy:
	./mvnw deploy -DskipTests -Psigned -B

deploy: all just-deploy

