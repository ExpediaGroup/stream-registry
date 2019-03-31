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
 * Kafka Topic's default retention setting is "cleanup.policy=delete(7 days)", and any message older than 7 days would be
 * deleted. So, it is necessary to make sure we periodically verify the availability of topic config "cleanup.policy=compact",
 * and monitor this metric.
 *
 * This HealthCheck returns "Unhealthy" if the configuration "cleanup.policy=compact" is not available for the Kafka Topic,
 * and return the metric "app.is_kafka_topic_compacted=2" at http://HOST:8081/private/metrics
 *
 */
@Slf4j
public class CompactedEventStoreHealthCheck extends HealthCheck {
    private static final String METRIC_IS_KAFKA_TOPIC_COMPACTED = "app.is_kafka_topic_compacted";

    @Getter@Setter
    private boolean isKafkaTopicCompacted;
    private final AdminClient kafkaAdminClient;
    private final String topicName;

    public CompactedEventStoreHealthCheck(StreamRegistryConfiguration configuration, MetricRegistry metricRegistry) {
        metricRegistry.register(METRIC_IS_KAFKA_TOPIC_COMPACTED, (Gauge<Integer>)() -> isKafkaTopicCompacted() ? 1 : 2);

        String kafkaBootstrapURI = configuration.getKafkaProducerConfig().getKafkaProducerProperties().get(BOOTSTRAP_SERVERS_CONFIG);
        Properties properties = new Properties();
        properties.put(BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapURI);
        this.kafkaAdminClient = KafkaAdminClient.create(properties);

        this.topicName = configuration.getTopicsConfig().getProducerTopic();
    }

    @Override
    protected Result check() throws ExecutionException, InterruptedException {
        ConfigResource configResource = new ConfigResource(Type.TOPIC, topicName);
        Config configs = kafkaAdminClient
                .describeConfigs(Collections.singleton(configResource))
                .values()
                .get(configResource)
                .get();

        isKafkaTopicCompacted = isCompactionConfigAvailable(configs);

        if (isKafkaTopicCompacted)
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

    public static boolean isCompactionConfigAvailable(Config configs) {
        return configs.entries().stream()
                .anyMatch(config -> "cleanup.policy".equals(config.name()) && "compact".equals(config.value()));
    }
}
