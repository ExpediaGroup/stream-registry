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
package com.homeaway.streamplatform.streamregistry.db.dao.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import kafka.admin.AdminUtils;
import kafka.admin.RackAwareMode;
import kafka.server.ConfigType;
import kafka.utils.ZKStringSerializer$;
import kafka.utils.ZkUtils;
import lombok.extern.slf4j.Slf4j;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;
import org.apache.kafka.clients.producer.ProducerConfig;

import com.homeaway.streamplatform.streamregistry.configuration.KafkaProducerConfig;
import com.homeaway.streamplatform.streamregistry.db.dao.KafkaManager;
import com.homeaway.streamplatform.streamregistry.exceptions.StreamCreationException;

@Slf4j
public class KafkaManagerImpl implements KafkaManager {
    private static Map<String,Boolean> TOPIC_CONFIG_KEY_FILTER = new HashMap<String, Boolean>() {
        private static final long serialVersionUID = -7377105429359314831L; {

        put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, true);
        put(KafkaProducerConfig.ZOOKEEPER_QUORUM, true);
    }};

    // internal state


    private ZkUtils initZkUtils(Properties config) {
        String zkConnect = config.getProperty(KafkaProducerConfig.ZOOKEEPER_QUORUM);
        ZkClient zkClient = new ZkClient(zkConnect);
        zkClient.setZkSerializer(ZKStringSerializer$.MODULE$);
        ZkConnection zkConnection = new ZkConnection(zkConnect);
        ZkUtils zkUtils = new ZkUtils(zkClient, zkConnection, false);
        return zkUtils;
    }

    public void shutdownZkUtils(ZkUtils zkUtils) {
        try {
            zkUtils.close();
        } catch (RuntimeException exception) {
            log.error("Unexpected exception caught during zkUtils shutdown.", exception);
        }
    }

    /**
     * Create and/or Update Topics using AdminClient and AdminUtils
     *
     * @param topics            list of topics that need to be created
     * @param partitions        no. of partitions for each topic that will be created
     * @param replicationFactor replication for each topic that will be created
     * @param properties        properties that will be set on each topic in the list
     */
    public void upsertTopics(Collection<String> topics, int partitions, int replicationFactor, Properties properties, boolean isNewStream) {
        // TODO - Can't guarantee against race conditions... should probably move to event-source paradigm to
        //      protect against this (and maybe employ optimistic locking for extra safety).
        //      The issue here is there is nothing that "locks" the underlying kafka store -- something
        //      can inevitably change the underlying store while this method is evaluating, always accounting for
        //      some amount of race window.

        // TODO probably need to cache a KafkaManagerImpl per "cluster" to avoid un-necessary creation / destruction of connections
        ZkUtils zkUtils = initZkUtils(properties);
        try {
            // remove client connection properties to leave only topic configs
            Map<String, String> topicConfigMap = filterPropertiesKeys(properties, TOPIC_CONFIG_KEY_FILTER);

            // partition the list by whether the topic exists or not
            Map<Boolean, List<String>> partitionMaps = topics.stream().collect(Collectors.partitioningBy(topic -> topicExists(zkUtils, topic)));

            // if it exists, update it.  If it doesn't exist, create it
            List<String> topicsToUpdate = partitionMaps.get(true);
            List<String> topicsToCreate = partitionMaps.get(false);

            // update any topics that are necessary
            updateTopics(zkUtils, topicsToUpdate, topicConfigMap, isNewStream);

            // now create any topics that were necessary to create this run
            createTopics(zkUtils, topicsToCreate, partitions, replicationFactor, topicConfigMap);
        } finally {
            shutdownZkUtils(zkUtils);
        }
    }

    // package scope so that PowerMock can verify
    void updateTopics(ZkUtils zkUtils, List<String> topicsToUpdate, Map<String, String> topicConfigMap, boolean isNewStream) {
        for (String topic : topicsToUpdate) {
            // update topic
            Properties actualTopicConfig = getTopicConfig(zkUtils, topic);
            Map<String, String> actualTopicConfigMap = propertiesToMap(actualTopicConfig);
            if(actualTopicConfigMap.equals(topicConfigMap)) {
                // NOTHING TO DO!
                log.info("topic config for {} exactly match. Ignoring.", topic);
                continue;
            }

            // NOTE: If a newly created stream is requested in Stream Registry but it is already present
            // in the underlying streaming infrastructure... AND we got this far, this means configuration
            // is different.  We want to prevent this from actually changing the underlying infrastructure.
            // Therefore the operation is failed with an exception.
            //
            // This provides a safety mechanism and a migration path by requiring folks
            // to exactly match downstream config when the stream-registry has not "onboarded" existing topic
            // for the first time.

            // TODO Alternatively we can add a forceSync=true flag, ignoring any user provided info, and only updating SR with the underlying settings
            //      We should probably do forceSync=true anyway, as it provides a simple way to keep things in sync
            if(isNewStream) {
                // FIXME!! Fix exception reporting... just reporting the topic failed with no message is not a good developer experience. Consider replacing topic with message. (and include topic name in message).
                // throw new StreamCreationException("topic: " + topic " already exists but config is different than requested!"); // consider using forceSync=true
                throw new StreamCreationException(topic);
            }

            // If we got this far, we are "updating" an "existing" stream, and request config is different than
            // what is in stream registry. Go ahead and update now.
            updateTopic(zkUtils, topic, topicConfigMap);
        }
    }

    private void updateTopic(ZkUtils zkUtils, String topic, Map<String, String> configMap) {
        Properties topicProperties = new Properties();
        topicProperties.putAll(configMap);
        AdminUtils.changeTopicConfig(zkUtils, topic, topicProperties);
    }

    // TODO need to check if topic exists instead of relying on exception path or just create one since check already occurred above
    // TODO Timeout exception needs to propagate and not be handled here
    // TODO Need JavaDoc
    // package scope so that PowerMock can verify
    void createTopics(ZkUtils zkUtils, Collection<String> topics, int partitions, int replicationFactor, Map<String, String> topicConfigMap) {
        for(String topic : topics) {
            createTopic(zkUtils, topic, partitions, replicationFactor, topicConfigMap);
        }
    }

    private void createTopic(ZkUtils zkUtils, String topic, int partitions, int replicationFactor, Map<String, String> topicConfigMap) {
        Properties topicProperties = new Properties();
        topicProperties.putAll(topicConfigMap);
        AdminUtils.createTopic(zkUtils, topic, partitions, replicationFactor, topicProperties, RackAwareMode.Enforced$.MODULE$);
    }

    // utility methods for this class

    // package scope so that PowerMock can leverage
    @SuppressWarnings("SuspiciousMethodCalls")
    Map<String,String> filterPropertiesKeys(Properties properties, Map<String,Boolean> keyFilterMap) {
        return new HashMap<>(properties.stringPropertyNames().stream()
                .filter(key -> !keyFilterMap.containsKey(key))
                .filter(key -> properties.getProperty(key) != null)
                .collect(Collectors.toMap(key -> (String)key, key -> properties.getProperty(key))));
    }

    // package scope so that PowerMock can leverage
    Map<String,String> propertiesToMap(Properties properties) {
        return properties.stringPropertyNames().stream()
                .filter(key -> properties.getProperty(key) != null)
                .collect(Collectors.toMap(key -> (String)key,
                        key -> properties.getProperty(key)));
    }

    private boolean topicExists(ZkUtils zkUtils, String topic) {
        boolean topicExists = AdminUtils.topicExists(zkUtils, topic);
        log.debug("topic: {} exists={}", topic, topicExists);
        return topicExists;
    }

    private Properties getTopicConfig(ZkUtils zkUtils, String topic) {
        return AdminUtils.fetchEntityConfig(zkUtils, ConfigType.Topic(), topic);
    }
}
