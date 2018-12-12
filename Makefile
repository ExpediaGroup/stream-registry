.PHONY: clean tests build run debug all just-deploy deploy ci-setup ci-deploy

clean:
	./mvnw clean

tests:
	./mvnw clean test -B

build:
	./mvnw clean install -B

run:
	java -Xmx2G -server -XX:+UseG1GC -XX:MaxGCPauseMillis=20 -XX:InitiatingHeapOccupancyPercent=35 -XX:+ExplicitGCInvokesConcurrent -Djava.awt.headless=true -jar assembly/target/stream-registry*SNAPSHOT.jar server config-dev.yaml

debug:
	java -Xmx2G -server -XX:+UseG1GC -XX:MaxGCPauseMillis=20 -XX:InitiatingHeapOccupancyPercent=35 -XX:+ExplicitGCInvokesConcurrent -Djava.awt.headless=true -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5105 -jar assembly/target/stream-registry*SNAPSHOT.jar server config-dev.yaml

all: build

# invoke -Psigned profile for signing artifacts
just-deploy:
	./mvnw deploy -DskipTests -Psigned -B

deploy: all just-deploy

