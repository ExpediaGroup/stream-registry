.PHONY: clean tests build all release

clean:
	./mvnw clean

tests:
	./mvnw clean test -B

build:
	./mvnw clean install -B

all: build

# invoke -Psigned profile for signing artifacts
just-deploy:
	./mvnw deploy -B -Dmaven.test.skip=true -DskipTests -Psigned

deploy: all just-deploy

