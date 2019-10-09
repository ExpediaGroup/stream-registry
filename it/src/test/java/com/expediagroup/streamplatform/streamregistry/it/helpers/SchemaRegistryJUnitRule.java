/**
 * Copyright (C) 2018-2019 Expedia, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.expediagroup.streamplatform.streamregistry.it.helpers;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.streams.integration.utils.EmbeddedKafkaCluster;
import org.junit.rules.ExternalResource;
import org.slf4j.bridge.SLF4JBridgeHandler;

import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.rest.SchemaRegistryConfig;
import io.confluent.kafka.schemaregistry.rest.SchemaRegistryRestApplication;

public class SchemaRegistryJUnitRule extends ExternalResource {

  static {
    SLF4JBridgeHandler.removeHandlersForRootLogger();
    SLF4JBridgeHandler.install();
  }

  private final EmbeddedKafkaCluster kafka = new EmbeddedKafkaCluster(1, brokerConfig());
  private SchemaRegistryRestApplication schemaRegistry;
  private String url;
  private SchemaRegistryClient client;

  private static int randomPort() throws IOException {
    try (ServerSocket socket = new ServerSocket(0)) {
      return socket.getLocalPort();
    }
  }

  private static Properties brokerConfig() {
    Properties properties = new Properties();
    properties.put("auto.create.topics.enable", false);
    return properties;
  }

  @Override
  protected void before() {
    start();
  }

  @Override
  protected void after() {
    stop();
  }

  public void start() {
    try {
      kafka.start();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(e);
    }

    try {
      schemaRegistry = createSchemaRegistry();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    try {
      schemaRegistry.start();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    int port = schemaRegistry.getConfiguration().getInt(SchemaRegistryConfig.PORT_CONFIG);
    url = "http://localhost:" + port;
    client = new CachedSchemaRegistryClient(url, 100);
  }

  public String url() {
    return url;
  }

  public SchemaRegistryClient client() {
    return client;
  }

  public void stop() {
    try {
      schemaRegistry.stop();
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      stopKafka();
    }
  }

  private void stopKafka() {
    try {
      Method method = EmbeddedKafkaCluster.class.getDeclaredMethod("stop");
      method.setAccessible(true);
      method.invoke(kafka);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private SchemaRegistryRestApplication createSchemaRegistry() throws Exception {
    Map<String, String> topicProperties = new HashMap<>();
    topicProperties.put("cleanup.policy", "compact");
    kafka.createTopic("_schemas", 1, 1, topicProperties);
    Thread.sleep(Duration.ofSeconds(1).toMillis());

    int port = randomPort();
    Properties registryProperties = new Properties();
    registryProperties.put(SchemaRegistryConfig.PORT_CONFIG, port);
    registryProperties.put(SchemaRegistryConfig.KAFKASTORE_BOOTSTRAP_SERVERS_CONFIG, "PLAINTEXT://" + kafka.bootstrapServers());

    return new SchemaRegistryRestApplication(registryProperties);
  }
}
