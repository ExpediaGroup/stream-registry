.PHONY: clean tests build all release

clean:
	./mvnw clean

tests:
	./mvnw test -B

build:
	./mvnw install

all: build

deploy: all
	./mvnw deploy -Poss
