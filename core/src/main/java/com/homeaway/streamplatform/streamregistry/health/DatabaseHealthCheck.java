/* Copyright (c) 2018-Present Expedia Group.
 * All rights reserved.  http://www.homeaway.com

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *      http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.homeaway.streamplatform.streamregistry.health;

import static org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG;

import java.util.*;
import java.util.concurrent.ExecutionException;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheck;

import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.config.ConfigResource;
import org.apache.kafka.common.config.ConfigResource.Type;

import com.homeaway.streamplatform.streamregistry.configuration.StreamRegistryConfiguration;

/**
 * StreamRegistry uses Kafka as its Data-store.
 * Kafka's default retention setting is "cleanup.policy=delete(7 days)", and any message older than 7 days would be
 * deleted. So, it is necessary to make sure we override the config of the Kafka Topic with "cleanup.policy=compact"
 * so that there is possibility of loss of Stream metadata.
 *
 * This HealthCheck returns "Unhealthy" if the configuration "cleanup.policy=compact" is not available for the Kafka Topic,
 * and return the metric "app.is_kafka_topic_compacted=2" at http://HOST:8081/private/metrics
 *
 */
@Slf4j
public class DatabaseHealthCheck extends HealthCheck {
    private static final String METRIC_IS_KAFKA_TOPIC_COMPACTED = "app.is_kafka_topic_compacted";

    @Getter@Setter
    private boolean isKafkaTopicCompacted;
    private final AdminClient kafkaAdminClient;
    private final String topicName;

    public DatabaseHealthCheck(StreamRegistryConfiguration configuration, MetricRegistry metricRegistry) {
        metricRegistry.register(METRIC_IS_KAFKA_TOPIC_COMPACTED, (Gauge<Integer>)() -> isKafkaTopicCompacted() ? 1 : 2);

        String kafkaBootstrapURI = configuration.getKafkaProducerConfig().getKafkaProducerProperties().get("bootstrap.servers");
        Properties properties = new Properties();
        properties.put(BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapURI);
        this.kafkaAdminClient = KafkaAdminClient.create(properties);

        this.topicName = configuration.getTopicsConfig().getProducerTopic();
        try {
            if (isKafkaTopicAvailable()) {
                makeSureTopicIsCompacted();
            } else {
                createTopicWithCompactionEnabled();
            }
        } catch (Exception e) {
            log.error("Error while communication with the underlying data-store. Topic={} BootstrapURI={}", topicName, kafkaBootstrapURI);
            throw new RuntimeException(e);
        }
    }

    private boolean isKafkaTopicAvailable() throws InterruptedException, ExecutionException {
        DescribeTopicsResult describeTopicsResult = kafkaAdminClient.describeTopics(Collections.singleton(topicName));
        try {
            TopicDescription topicDescription = describeTopicsResult.values().get(topicName).get();
            log.info("Topic present. {}", topicDescription);
            return true;
        } catch (ExecutionException e) {
            if ("org.apache.kafka.common.errors.UnknownTopicOrPartitionException".equals(e.getCause().getClass().getName())) {
                log.error("Topic {} not present.", topicName);
                return false;
            }
            throw e;
        }
    }

    private void makeSureTopicIsCompacted() throws ExecutionException, InterruptedException {
        ConfigResource configResource = new ConfigResource(Type.TOPIC, topicName);

        Config config = kafkaAdminClient.describeConfigs(Collections.singleton(configResource)).values().get(configResource).get();
        if (isCompactionConfigAvailable(config)) {
            log.info("Compaction config available in the topic {}", topicName);
        } else {
            config.entries().add(new ConfigEntry("cleanup.policy", "compact"));
            kafkaAdminClient.alterConfigs(Collections.singletonMap(configResource, config))
                    .values()
                    .get(configResource)
                    .get();
            log.info("Stream Registry underlying data store - kafka topic {} updated with config {}", topicName, config);
        }
    }

    private void createTopicWithCompactionEnabled() throws ExecutionException, InterruptedException {
        int partitions = 2;
        short replicationFactor = 3;
        NewTopic streamRegistryTopic = new NewTopic(this.topicName, partitions, replicationFactor);
        Map<String, String> configs = new HashMap<>();
        configs.put("min.insync.replicas", "2");
        configs.put("cleanup.policy", "compact");
        streamRegistryTopic.configs(configs);
        try {
            kafkaAdminClient.createTopics(Collections.singleton(streamRegistryTopic)).values().get(topicName).get();
        } catch (ExecutionException e) {
            if ("org.apache.kafka.common.errors.InvalidReplicationFactorException".equals(e.getCause().getClass().getName())) {
                log.warn("Some environments like dev or laptop may have a cluster with only one broker. So, try to create with one replica.");
                replicationFactor = 1;
                streamRegistryTopic = new NewTopic(this.topicName, partitions, replicationFactor);
                configs = new HashMap<>();
                configs.put("cleanup.policy", "compact");
                streamRegistryTopic.configs(configs);
                kafkaAdminClient.createTopics(Collections.singleton(streamRegistryTopic)).values().get(topicName).get();
            } else {
                throw e;
            }
        }
        log.info("Stream Registry underlying data store - kafka topic {} created with replicationFator {}.", topicName, replicationFactor);
    }

    @Override
    protected Result check() throws ExecutionException, InterruptedException {
        ConfigResource configResource = new ConfigResource(Type.TOPIC, topicName);
        Config configs = kafkaAdminClient
                .describeConfigs(Collections.singleton(configResource))
                .values()
                .get(configResource)
                .get();

        boolean isTopicCompacted = isCompactionConfigAvailable(configs);
        this.setKafkaTopicCompacted(isTopicCompacted);

        if (isTopicCompacted)
            return Result.builder()
                    .healthy()
                    .withMessage("The Kafka Topic used as a Data store is Compacted.")
                    .build();
        else
            return Result.builder()
                    .unhealthy()
                    .withMessage("The Kafka Topic used as a Data store is not Compacted, so there is a possible loss of Stream Metadata.")
                    .build();
    }

    private boolean isCompactionConfigAvailable(Config configs) {
        return configs.entries().stream()
                .anyMatch(config -> "cleanup.policy".equals(config.name()) && "compact".equals(config.value()));
    }
}
