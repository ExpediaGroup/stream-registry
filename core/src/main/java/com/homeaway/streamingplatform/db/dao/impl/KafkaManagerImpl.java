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

import java.util.*;
import java.util.stream.Collectors;

import kafka.admin.AdminUtils;
import kafka.server.ConfigType;
import kafka.utils.ZKStringSerializer$;
import kafka.utils.ZkUtils;
import lombok.extern.slf4j.Slf4j;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;

import com.homeaway.streamingplatform.configuration.KafkaProducerConfig;
import com.homeaway.streamingplatform.db.dao.KafkaManager;
import com.homeaway.streamingplatform.exceptions.StreamCreationException;

@Slf4j
public class KafkaManagerImpl implements KafkaManager {
    private static Map<String,Boolean> TOPIC_CONFIG_KEY_FILTER = new HashMap<String, Boolean>() {
        private static final long serialVersionUID = -7377105429359314831L; {

        put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, true);
        put(KafkaProducerConfig.ZOOKEEPER_QUORUM, true);
    }};

    // internal state

    private ZkClient zkClient;
    private ZkUtils zkUtils;

    /**
     * Create and/or Update Topics using AdminClient and AdminUtils
     *
     * @param topics            list of topics that need to be created
     * @param partitions        no. of partitions for each topic that will be created
     * @param replicationFactor replication for each topic that will be created
     * @param properties        properties that will be set on each topic in the list
     */
    public void upsertTopics(Collection<String> topics, int partitions, int replicationFactor, Properties properties, boolean isNewStream) {
        // remove client connection properties to leave only topic configs
        Map<String, String> topicConfigMap = filterPropertiesKeys(properties, TOPIC_CONFIG_KEY_FILTER);

        List<String> topicsToCreate = new ArrayList<>();
        for (String topic : topics) {
            // check if topic doesn't exist and add to list
            if (!topicExists(topic)) {
                // buffer creation request
                topicsToCreate.add(topic);
                continue;
            }

            // update topic
            Properties actualTopicConfig = getTopicConfig(topic);
            Map<String, String> actualTopicConfigMap = propertiesToMap(actualTopicConfig);
            if(actualTopicConfig.equals(topicConfigMap)) {
                // NOTHING TO DO!
                log.info("topic config for {} exactly match. Ignoring.", topic);
                continue;
            }

            // FIXME!! This wreaks of non-declarative.. in a fully declarative world we would log the request and based on some policy accept or reject the request.
            // FIXME!! Here we are coding up the policy to reject the request if the config is different and in the newStream requested state.

            // NOTE: If a newly created stream is requested in Stream Registry but it is already present
            // in the underlying streaming infrastructure... AND we got this far, this means configuration
            // is different.  We want to prevent this from actually changing the underlying infrastructure.
            // Therefore the operation is failed with an exception.
            //
            // This provides a safety mechanism and a migration path by requiring folks
            // to exactly match downstream config when the stream-registry has not "onboarded" existing topic
            // for the first time.

            // TODO: Alternatively we can add a updateMetadataOnly=true flag, and this would not have any side-effects
            if(isNewStream) {
                // FIXME!! Fix exception reporting... just reporting the topic failed with no message is not a good developer experience. Consider replacing topic with message. (and include topic name in message).
                throw new StreamCreationException(topic);
            }

            // If we got this far, we are not a new stream, and request config is different than
            // what is in stream registry. Feel fe
            updateTopic(zkUtils, topic, topicConfigMap);
        }

        // now create any topics that were necessary to create this run
        createTopics(topicsToCreate, partitions, replicationFactor, properties, topicConfigMap);
    }

    private void updateTopic(ZkUtils zkUtils, String topic, Map<String, String> configMap) {
        Properties topicProperties = new Properties();
        topicProperties.putAll(configMap);
        AdminUtils.changeTopicConfig(zkUtils, topic, topicProperties);
    }

    // TODO need to check if topic exists instead of relying on exception path or just create one since check already occurred above
    // TODO Timeout exception needs to propagate and not be handled here
    // TODO Need JavaDoc
    protected void createTopics(Collection<String> topics, int partitions, int replicationFactor, Properties adminClientProperties, Map<String, String> topicConfigMap) {
        try {
            AdminClient adminClient = AdminClient.create(adminClientProperties);
            List<NewTopic> newTopicList = topics.stream()
                    .map(topic ->
                            new NewTopic(topic, partitions, (short) replicationFactor)
                                    .configs(topicConfigMap))
                    .collect(toList());

            CreateTopicsResult createTopicsResult = adminClient.createTopics(newTopicList);
            // synchronous block
            createTopicsResult.all().get();
        } catch (Exception exception) {
            // Unexpected exception
            throw new IllegalStateException("Unexpected exception", exception);
        }
    }

    @Override
    public void init(Properties config) {
        String zkConnect = config.getProperty(KafkaProducerConfig.ZOOKEEPER_QUORUM);
        zkClient = new ZkClient(zkConnect);
        zkClient.setZkSerializer(ZKStringSerializer$.MODULE$);
        ZkConnection zkConnection = new ZkConnection(zkConnect);
        zkUtils = new ZkUtils(zkClient, zkConnection, false);
    }

    @Override
    public void shutdown() {
        try {
            zkUtils.close();
            zkClient.close();
        } catch (RuntimeException exception) {
            log.error("Unexpected exception caught during KafkaManagerImpl shutdown.", exception);
        }
    }

    private Map<String,String> filterPropertiesKeys(Properties properties, Map<String,Boolean> keyfilterMap) {
        return new HashMap<>(properties.entrySet().stream()
                .filter(entry -> keyfilterMap.containsKey(entry.getKey()))
                .collect(Collectors.toMap(entry -> (String)entry.getKey(), entry -> (String)entry.getValue())));
    }

    private Map<String,String> propertiesToMap(Properties properties) {
        return properties.entrySet().stream()
                .collect(Collectors.toMap(entry -> (String)entry.getKey(), entry -> (String)entry.getValue()));
    }

    private boolean topicExists(String topic) {
        return AdminUtils.topicExists(zkUtils, topic);
    }

    private Properties getTopicConfig(String topic) {
        return AdminUtils.fetchEntityConfig(zkUtils, ConfigType.Topic(), topic);
    }
}
