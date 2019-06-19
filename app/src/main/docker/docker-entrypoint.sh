#!/usr/bin/env bash

# Which java to use
if [ -z "$JAVA_HOME" ]; then
  JAVA="java"
else
  JAVA="$JAVA_HOME/bin/java"
fi

# Generic jvm settings you want to add
if [ -z "$JAVA_OPTS" ]; then
  JAVA_OPTS=""
fi

# Memory options
if [ -z "$JAVA_HEAP_OPTS" ]; then
  JAVA_HEAP_OPTS="-Xmx256M"
fi

# JVM performance options
if [ -z "$JVM_PERFORMANCE_OPTS" ]; then
  JVM_PERFORMANCE_OPTS="-server -Djava.awt.headless=true -XX:MaxGCPauseMillis=20 -XX:InitiatingHeapOccupancyPercent=35 -XX:+ExplicitGCInvokesConcurrent"
fi

# Explicitly setting the classpath allows for plugins to be added later
CLASSPATH="/usr/share/stream-registry/stream-registry.jar:/usr/share/stream-registry/lib/*.jar"
exec $JAVA -cp $CLASSPATH \
    $JAVA_HEAP_OPTS $JVM_PERFORMANCE_OPTS $JAVA_OPTS \
    $MAIN_CLASS
