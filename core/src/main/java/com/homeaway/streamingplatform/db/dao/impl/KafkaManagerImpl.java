/* Copyright (c) 2018 Expedia Group.
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
package com.homeaway.streamingplatform.db.dao.impl;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import kafka.admin.AdminUtils;
import kafka.utils.ZKStringSerializer$;
import kafka.utils.ZkUtils;
import lombok.extern.slf4j.Slf4j;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.errors.TimeoutException;
import org.apache.kafka.common.errors.TopicExistsException;

import com.homeaway.streamingplatform.configuration.KafkaProducerConfig;
import com.homeaway.streamingplatform.db.dao.KafkaManager;

@Slf4j
public class KafkaManagerImpl implements KafkaManager {

    /**
     * Create and/or Update Topics using AdminClient and AdminUtils
     *
     * @param topics            list of topics that need to be created
     * @param partitions        no. of partitions for each topic that will be created
     * @param replicationFactor replication for each topic that will be created
     * @param properties        properties that will be set on each topic in the list
     */
    public void upsertTopics(Collection<String> topics, int partitions, int replicationFactor, Properties properties) {
        // remove client connection properties to leave only topic configs
        Map<String, String> topicConfigMap = new HashMap<>(properties.entrySet()
                .stream()
                .filter(e -> !e.getKey().equals(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG))
                .filter(e -> !e.getKey().equals(KafkaProducerConfig.ZOOKEEPER_QUORUM))
                .collect(Collectors.toMap(e -> e.getKey().toString(), e -> e.getValue().toString())));

        String zkConnect = properties.getProperty(KafkaProducerConfig.ZOOKEEPER_QUORUM);
        ZkClient zkClient = new ZkClient(zkConnect);
        zkClient.setZkSerializer(ZKStringSerializer$.MODULE$);
        ZkConnection zkConnection = new ZkConnection(zkConnect);
        ZkUtils zkUtils = new ZkUtils(zkClient, zkConnection, false);

        try {
            List<String> topicsToCreate = new ArrayList<>();
            for (String topic : topics) {
                if (AdminUtils.topicExists(zkUtils, topic)) {
                    log.info("Stream Registry does not allow updating configs for existing topics.");
                    //updateTopic(zkUtils, topic, topicConfigMap);
                } else {
                    topicsToCreate.add(topic);
                }
            }

            createTopics(topicsToCreate, partitions, replicationFactor, properties, topicConfigMap);
        } finally {
            try {
                zkClient.close();
                zkConnection.close();
                zkUtils.close();
            } catch (InterruptedException e) {
                log.info("caught an exception while closing ZK clients: {}", e.getMessage());
            }
        }
    }

    private void updateTopic(ZkUtils zkUtils, String topic, Map<String, String> configMap) {
        Properties topicProperties = new Properties();
        topicProperties.putAll(configMap);
        AdminUtils.changeTopicConfig(zkUtils, topic, topicProperties);
    }

    // TODO need to check if topic exists instead of relying on exception path or just create one since check already occurred above
    // TODO Timeout exception needs to propagate and not be handled here
    // TODO Interrupted Exception also needs to propagate and not be handled here
    private void createTopics(Collection<String> topics, int partitions, int replicationFactor, Properties adminClientProperties, Map<String, String> topicConfigMap) {
        try (AdminClient adminClient = AdminClient.create(adminClientProperties)) {
            List<NewTopic> newTopicList = topics.stream()
                    .map(topic -> {
                        NewTopic newTopic = new NewTopic(topic, partitions, (short) replicationFactor);
                        newTopic.configs(topicConfigMap);
                        return newTopic;
                    })
                    .collect(toList());

            CreateTopicsResult createTopicsResult = adminClient.createTopics(newTopicList);
            createTopicsResult.all().get();
        } catch (ExecutionException e) {
            if (e.getCause() instanceof TopicExistsException) {
                log.info("Topic already exists !!");
            } else if (e.getCause() instanceof TimeoutException) {
                log.error("Timeout !!", e);
            }
        } catch (InterruptedException e) {
            log.error("Exception in creating topics:", e);
        }
    }
}
