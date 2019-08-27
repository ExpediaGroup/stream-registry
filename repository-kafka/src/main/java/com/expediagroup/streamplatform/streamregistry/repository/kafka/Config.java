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
package com.expediagroup.streamplatform.streamregistry.repository.kafka;

import static java.util.concurrent.TimeUnit.SECONDS;

import static org.apache.kafka.common.config.ConfigResource.Type.TOPIC;

import static com.google.common.base.Preconditions.checkState;

import static kafka.log.LogConfig.CleanupPolicyProp;
import static kafka.log.LogConfig.Compact;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.annotation.PostConstruct;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.ConfigResource;
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Component
@lombok.Value
@Builder
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class Config {
  static final int PARTITIONS = 1;
  static final int TIMEOUT_SECONDS = 10;

  String bootstrapServers;
  String topic;
  short replicationFactor;
  String schemaRegistryUrl;
  @Getter(AccessLevel.PRIVATE) AdminClient client;

  @Autowired
  public Config(
      @Value("${repository.kafka.bootstrap-servers}") String bootstrapServers,
      @Value("${repository.kafka.topic:_streams}") String topic,
      @Value("${repository.kafka.replicationFactor:3}") short replicationFactor,
      @Value("${schema.registry.url}") String schemaRegistryUrl) {
    this(bootstrapServers,
        topic,
        replicationFactor,
        schemaRegistryUrl,
        AdminClient.create(Map.of("bootstrap.servers", bootstrapServers)));
  }

  @PostConstruct
  void verifyOrCreateTopic() throws InterruptedException, ExecutionException, TimeoutException {
    try {
      int partitions = partitions();
      checkState(partitions == 1, "Streams topic (%s) exists but it has %s partitions, expected 1.", topic, partitions);

      String cleanupPolicy = cleanupPolicy();
      checkState(Compact().equals(cleanupPolicy), "Streams topic (%s) exists but is not compact", topic);
    } catch (ExecutionException e) {
      if (e.getCause() instanceof UnknownTopicOrPartitionException) {
        createTopic();
      } else {
        throw e;
      }
    }
  }

  private int partitions() throws InterruptedException, ExecutionException, TimeoutException {
    return client
        .describeTopics(List.of(topic))
        .all()
        .get(TIMEOUT_SECONDS, SECONDS)
        .get(topic)
        .partitions()
        .size();
  }

  private String cleanupPolicy() throws InterruptedException, ExecutionException, TimeoutException {
    ConfigResource resource = new ConfigResource(TOPIC, topic);
    ConfigEntry configEntry = client
        .describeConfigs(List.of(resource))
        .all()
        .get(TIMEOUT_SECONDS, SECONDS)
        .get(resource)
        .get(CleanupPolicyProp());
    return Optional.ofNullable(configEntry).map(ConfigEntry::value).orElse(null);
  }

  private void createTopic() throws InterruptedException, ExecutionException, TimeoutException {
    client
        .createTopics(List.of(new NewTopic(topic, PARTITIONS, replicationFactor)
            .configs(Map.of(CleanupPolicyProp(), Compact()))))
        .all()
        .get(TIMEOUT_SECONDS, SECONDS);
  }
}
