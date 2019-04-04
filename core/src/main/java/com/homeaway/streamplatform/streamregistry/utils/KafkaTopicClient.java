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
package com.homeaway.streamplatform.streamregistry.utils;

import static org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import lombok.extern.slf4j.Slf4j;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.ConfigResource;
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException;

@Slf4j
public class KafkaTopicClient {

    private String kafkaBootstrapServers;
    private AdminClient adminClient;

    public KafkaTopicClient(String kafkaBootstrapServers) {
        Properties properties = new Properties();
        properties.put(BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
        this.adminClient = KafkaAdminClient.create(properties);
        this.kafkaBootstrapServers = kafkaBootstrapServers;
    }

    public boolean isKafkaTopicPresent(String topicName) {
        DescribeTopicsResult describeTopicsResult = adminClient.describeTopics(Collections.singleton(topicName));
        try {
            describeTopicsResult.values().get(topicName).get();
            return true;
        } catch (InterruptedException | ExecutionException e) {
            // If the exception is an instance of UnknownTopicOrPartitionException, then the topic is not available.
            if (e.getCause() instanceof UnknownTopicOrPartitionException) {
                log.error("Topic {} not present.", topicName);
                return false;
            }
            /* If exception is not an instance of UnknownTopicOrPartitionException, then the Kafka Cluster is not reachable,
             and it is better to halt the application by throwing back the exception. */
            throw new RuntimeException(String.format("Error while describing the Topic=%s. " +
                    "Please make sure the cluster=%s is accessible" , topicName, kafkaBootstrapServers), e);
        }
    }

    public void createTopic(String topicName,
                                   int partitionCount,
                                   short replicationFactor,
                                   Map<String, String> configs) throws ExecutionException, InterruptedException {
        NewTopic streamRegistryTopic = new NewTopic(topicName, partitionCount, replicationFactor);
        streamRegistryTopic.configs(configs);
        adminClient.createTopics(Collections.singleton(streamRegistryTopic)).values().get(topicName).get();
    }

    public void validateTopicConfigs(String topicName,
                                     Map<String, String> expectedProperties) throws ExecutionException, InterruptedException {
        ConfigResource configResource = new ConfigResource(ConfigResource.Type.TOPIC, topicName);
        // check whether the compaction property is available
        Config config = adminClient.describeConfigs(Collections.singleton(configResource)).values().get(configResource).get();

        Collection<ConfigEntry> actualProperties = config.entries();

        expectedProperties.forEach((key, value) -> {
            boolean isConfigAvailable = actualProperties.stream().anyMatch(configEntry ->
                    configEntry.name().equals(key) && configEntry.value().equals(value));
            if (! isConfigAvailable)
                throw new RuntimeException(String.format("Topic config mismatch. BootstrapURI=%s TopicName=%s Excepted Config=%s ;" +
                        " Actual Config=%s", kafkaBootstrapServers, topicName, expectedProperties, actualProperties));
        });
    }
}
