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

import java.util.concurrent.ExecutionException;

import com.homeaway.streamplatform.streamregistry.configuration.EventStoreTopic;
import com.homeaway.streamplatform.streamregistry.utils.KafkaTopicClient;
import lombok.extern.slf4j.Slf4j;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheck;

import com.homeaway.streamplatform.streamregistry.configuration.StreamRegistryConfiguration;

/**
 * StreamRegistry uses Kafka as its Data-store.
 * Kafka Topic's default retention setting is "cleanup.policy=delete(7 days)", and any message older than 7 days would be
 * deleted. So, it is necessary to make sure we periodically verify the availability of topic config "cleanup.policy=compact",
 * and monitor this metric.
 *
 * This HealthCheck returns "Unhealthy" if the configuration "cleanup.policy=compact" is not available for the Kafka Topic,
 * and return the metric "app.is_event_store_kafka_topic_config_valid=2" at http://HOST:8081/private/metrics
 *
 */
@Slf4j
public class EventStoreConfigHealthCheck extends HealthCheck {
    private static final String METRIC_IS_EVENT_STORE_KAFKA_TOPIC_CONFIG_VALID = "app.is_event_store_kafka_topic_config_valid";

    private boolean isEventStoreTopicConfigsValid;
    private final KafkaTopicClient kafkaTopicClient;
    private final EventStoreTopic eventStoreTopic;

    public EventStoreConfigHealthCheck(StreamRegistryConfiguration configuration, MetricRegistry metricRegistry) {
        metricRegistry.register(METRIC_IS_EVENT_STORE_KAFKA_TOPIC_CONFIG_VALID, (Gauge<Integer>)() -> isEventStoreTopicConfigsValid ? 1 : 2);

        String kafkaBootstrapURI = configuration.getKafkaProducerConfig().getKafkaProducerProperties().get(BOOTSTRAP_SERVERS_CONFIG);
        kafkaTopicClient = new KafkaTopicClient(kafkaBootstrapURI);

        eventStoreTopic = configuration.getTopicsConfig().getEventStoreTopic();
    }

    /**
     * validate the properties of the EventStore topic with the expected properties in the config.yaml
     *
     * @return Result
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Override
    protected Result check() throws ExecutionException, InterruptedException {
        try {
            kafkaTopicClient.validateTopicConfigs(eventStoreTopic.getName(), eventStoreTopic.getProperties());
            isEventStoreTopicConfigsValid = true;
            return Result.builder()
                    .healthy()
                    .withMessage(String.format("The Event Store topic config is valid. Configs=%s", eventStoreTopic.getProperties()))
                    .build();

        } catch (RuntimeException e) {
            isEventStoreTopicConfigsValid = true;
            return Result.builder()
                    .unhealthy()
                    .withMessage(e.getMessage())
                    .build();
        }
    }

}