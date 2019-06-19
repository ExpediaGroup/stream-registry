ARG BASE_CONTAINER=openjdk:11
FROM $BASE_CONTAINER

ENTRYPOINT ["/docker-entrypoint.sh"]
EXPOSE 8080

COPY target/lib /usr/share/stream-registry/lib
ARG JAR_FILE
COPY target/${JAR_FILE} /usr/share/stream-registry/stream-registry.jar
ARG MAIN_CLASS_ARG
ENV MAIN_CLASS=$MAIN_CLASS_ARG

ARG CONTEXT_DIR
COPY ${CONTEXT_DIR}/docker-entrypoint.sh /