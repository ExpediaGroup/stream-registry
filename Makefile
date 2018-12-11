.PHONY: clean tests build all release

clean:
	./mvnw clean

tests:
	./mvnw test -B

build:
	./mvnw install -Poss

all: build

deploy: all
	cp .travis.settings.xml $HOME/.m2/settings.xml && ./mvnw deploy -Poss
