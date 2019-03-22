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

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheck;

import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.config.ConfigResource;

import com.homeaway.streamplatform.streamregistry.configuration.StreamRegistryConfiguration;

public class DataStoreHealthCheck extends HealthCheck {
    @Getter@Setter
    private boolean isTopicCompacted;

    private AdminClient adminClient;
    private String topicName;

    public DataStoreHealthCheck(StreamRegistryConfiguration configuration, MetricRegistry metricRegistry) {
        metricRegistry.register("app.is_kafka_topic_compacted",
                (Gauge<Integer>)() -> isTopicCompacted() ? 1 : 2);

        String kafkaEndpoint = configuration.getKafkaProducerConfig().getKafkaProducerProperties().get("bootstrap.servers");
        Properties properties = new Properties();
        properties.put(BOOTSTRAP_SERVERS_CONFIG, kafkaEndpoint);
        this.adminClient = KafkaAdminClient.create(properties);

        this.topicName = configuration.getTopicsConfig().getProducerTopic();
        // TODO: Check whether the topic is created already.
        NewTopic streamRegistryTopic = new NewTopic(this.topicName, 2, (short)3);
        Map<String, String> configs = new HashMap<>();
        configs.put("min.insync.replicas", "2");
        configs.put("cleanup.policy", "compact");
        streamRegistryTopic.configs(configs);
        adminClient.createTopics(Collections.singleton(streamRegistryTopic));
    }

    @Override
    protected Result check() throws ExecutionException, InterruptedException {
        ConfigResource configResource = new ConfigResource(ConfigResource.Type.TOPIC, topicName);
        Config configs = adminClient.describeConfigs(Collections.singleton(configResource)).values().get(configResource).get();
        Optional<ConfigEntry> compactionConfigEntry = configs.entries().stream()
                .filter(config -> config.name() == "cleanup.policy" && config.value() == "compact")
                .findAny();

        this.setTopicCompacted(compactionConfigEntry.isPresent());

        if (compactionConfigEntry.isPresent()) {
            return Result.builder()
                    .healthy()
                    .withMessage(configs.toString())
                    .build();
        } else {
            return Result.builder()
                    .unhealthy()
                    .withMessage("Compact Config is not set. So, there is a possible data loss.", configs)
                    .build();
        }
    }
}
