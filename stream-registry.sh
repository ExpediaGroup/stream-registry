#!/usr/bin/env bash

set -e

###########################
# Variable Setup
###########################

# Which java to use
if [ -z "$JAVA_HOME" ]; then
  JAVA="java"
else
  JAVA="$JAVA_HOME/bin/java"
fi

# Memory options
if [ -z "$STREAM_REGISTRY_HEAP_OPTS" ]; then
  STREAM_REGISTRY_HEAP_OPTS="-Xmx2G"
fi

# JVM performance options
if [ -z "$STREAM_REGISTRY_JVM_PERFORMANCE_OPTS" ]; then
  STREAM_REGISTRY_JVM_PERFORMANCE_OPTS="-server -XX:+UseG1GC -XX:MaxGCPauseMillis=20 -XX:InitiatingHeapOccupancyPercent=35 -XX:+ExplicitGCInvokesConcurrent -Djava.awt.headless=true"
fi

case $1 in
build)
  ./mvnw clean install
  ;;
run)
  exec $JAVA $STREAM_REGISTRY_HEAP_OPTS $STREAM_REGISTRY_JVM_PERFORMANCE_OPTS -jar assembly/target/stream-registry*SNAPSHOT.jar server config-dev.yaml
  ;;
debug)
  STREAM_REGISTRY_DEBUG_PORT=${STREAM_REGISTRY_DEBUG_PORT:-5105}
  STREAM_REGISTRY_DEBUG=${STREAM_REGISTRY_DEBUG:-n}
  STREAM_REGISTRY_DEBUG_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=${STREAM_REGISTRY_DEBUG},address=${STREAM_REGISTRY_DEBUG_PORT}"
  exec $JAVA $STREAM_REGISTRY_HEAP_OPTS $STREAM_REGISTRY_JVM_PERFORMANCE_OPTS $STREAM_REGISTRY_DEBUG_OPTS -jar assembly/target/stream-registry*SNAPSHOT.jar server config-dev.yaml
  ;;
*)
  echo "Usage: `basename $0` {build|run|debug}"
  exit 3;
esac
