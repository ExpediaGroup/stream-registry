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
package com.homeaway.streamplatform.streamregistry.provider.impl;

import static io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import kafka.admin.AdminUtils;
import kafka.admin.RackAwareMode;
import kafka.server.ConfigType;
import kafka.utils.ZKStringSerializer$;
import kafka.utils.ZkUtils;
import lombok.extern.slf4j.Slf4j;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;
import org.apache.commons.lang3.Validate;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

import com.homeaway.digitalplatform.streamregistry.ClusterKey;
import com.homeaway.digitalplatform.streamregistry.ClusterValue;
import com.homeaway.streamplatform.streamregistry.configuration.KafkaProducerConfig;
import com.homeaway.streamplatform.streamregistry.exceptions.StreamCreationException;
import com.homeaway.streamplatform.streamregistry.provider.InfraManager;


/**
 * An {@link InfraManager} implementation backed by a Kafka Streams {@link GlobalKTable}.
 */
@SuppressWarnings("WeakerAccess")
@Slf4j
public class KafkaInfraManager implements InfraManager {

    public static final String INFRAMANAGER_TOPIC = "infraManagerTopic";
    public static final String INFRAMANAGER_STATE_STORE = "infraManagerStateStoreName";
    public static final String INFRA_KSTREAM_PROPS = "infraKStreamsProperties";

    private String infraStateStoreName;
    private KafkaStreams infraKStreams;
    private KafkaProducer<ClusterKey, ClusterValue> infraProducer;
    private ReadOnlyKeyValueStore<ClusterKey, ClusterValue> store;
    private String infraManagerTopic;

    private static Map<String,Boolean> TOPIC_CONFIG_KEY_FILTER = new HashMap<String, Boolean>() {
        private static final long serialVersionUID = -7377105429359314831L; {

            put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, true);
            put(KafkaProducerConfig.ZOOKEEPER_QUORUM, true);
        }};

    @SuppressWarnings("unchecked")
    @Override
    public void configure(Map<String, Object> configs) {
        // Get the infra manager topic name
        Validate.validState(configs.containsKey(INFRAMANAGER_TOPIC), "Infra Manager Topic name is not provided.");
        infraManagerTopic = configs.get(INFRAMANAGER_TOPIC).toString();
        log.info("Infra Manager Topic Name Read: {}", infraManagerTopic);

        // Get the infra state store name
        Validate.validState(configs.containsKey(INFRAMANAGER_STATE_STORE), "Infra Manager State Store name is not provided.");
        infraStateStoreName = configs.get(INFRAMANAGER_STATE_STORE).toString();
        log.info("Infra Manager State Store Name Read: {}", infraStateStoreName);

        // Populate our kstreams properties map
        Properties infraKStreamsProperties = new Properties();
        Validate.validState(configs.containsKey(INFRA_KSTREAM_PROPS), "InfraKStreams properties is not provided.");
        Map<String, Object> infraKStreamsPropertiesMap = (Map<String, Object>) configs.get(INFRA_KSTREAM_PROPS);
        infraKStreamsPropertiesMap.forEach(infraKStreamsProperties::put);
        infraKStreamsProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        infraKStreamsProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        log.info("Infra KStreams Properties: {}", infraKStreamsProperties);

        Validate.validState(configs.containsKey(SCHEMA_REGISTRY_URL_CONFIG), SCHEMA_REGISTRY_URL_CONFIG+" is not provided.");
        String schemaRegistryUrl = (String) configs.get(SCHEMA_REGISTRY_URL_CONFIG);

        // Create a Infra Producer
        infraProducer = new KafkaProducer<>(infraKStreamsProperties);

        // initialize the kstreams processor
        StreamsBuilder infraKStreamBuilder = new StreamsBuilder();

        infraKStreamBuilder.globalTable(infraManagerTopic, createMaterialized(schemaRegistryUrl));
        infraKStreams = new KafkaStreams(infraKStreamBuilder.build(), infraKStreamsProperties);
    }

    @Override
    public void start() {
        infraKStreams.start();
        log.info("Infrastructure Manager KStream is started");
        log.info("Infra Manager State Store Name: {}", infraStateStoreName);
        store = infraKStreams.store(infraStateStoreName, QueryableStoreTypes.keyValueStore());
    }

    @Override
    public void stop() {
        infraKStreams.close();
        log.info("Infrastructure Manager KStream is stopped");
    }

    @Override
    public Map<ClusterKey, ClusterValue> getAllClusters() {
        Map<ClusterKey, ClusterValue> clusterKeyValueMap = new HashMap<>();
        try (KeyValueIterator<ClusterKey, ClusterValue> clusterKeyValueIterator = store.all()) {
            log.debug("Approximate Num. of Entries in Infra Table-{}", store.approximateNumEntries());

            while (clusterKeyValueIterator.hasNext()) {
                KeyValue<ClusterKey, ClusterValue> next = clusterKeyValueIterator.next();
                clusterKeyValueMap.put(next.key, next.value);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Infra Manager State Store not initialized ", e);
        }
        return clusterKeyValueMap;
    }

    @Override
    public Optional<ClusterValue> getClusterByKey(ClusterKey clusterKey) {
        ClusterValue clusterValue;
        Validate.validState(store != null, "Infra Manager should be configured");

        try {
            log.debug("Approximate Num. of Entries in Infra Table-{}", store.approximateNumEntries());
            clusterValue = store.get(clusterKey);
        } catch (Exception e) {
            throw new IllegalStateException("Error while retrieving the cluster Value using cluster key:" + clusterKey, e);
        }

        if (clusterValue == null) {
            log.error("Cluster Not Found, key: {}", clusterKey);
        } else {
            log.info("Cluster Name - {}", clusterValue.getClusterProperties());
        }

        return Optional.ofNullable(clusterValue);
    }

    @Override
    public void upsertCluster(ClusterKey clusterKey, ClusterValue clusterValue) {
        try {
            infraProducer.send(new ProducerRecord<>(infraManagerTopic, clusterKey, clusterValue));
        } catch (Exception e) {
            log.error("Error producing to topic={}", infraManagerTopic, e);
        }
    }

    @Override
    public void upsertTopics(Collection<String> topics, int partitions, int replicationFactor, Properties properties, boolean isNewStream)
        throws StreamCreationException {
        // TODO - Cannot guarantee against race conditions... should probably move to event-source paradigm to
        //      protect against this (and maybe employ optimistic locking for extra safety).
        //      The issue here is there is nothing that "locks" the underlying kafka store -- something
        //      can inevitably change the underlying store while this method is evaluating, always accounting for
        //      some amount of race window.

        // TODO probably need to cache a KafkaInfraManager per "cluster" to avoid un-necessary creation / destruction of connections (#115)
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

    private ZkUtils initZkUtils(Properties config) {
        String zkConnect = config.getProperty(KafkaProducerConfig.ZOOKEEPER_QUORUM);
        ZkClient zkClient = new ZkClient(zkConnect);
        zkClient.setZkSerializer(ZKStringSerializer$.MODULE$);
        ZkConnection zkConnection = new ZkConnection(zkConnect);
        return new ZkUtils(zkClient, zkConnection, false);
    }

    // package scope so that PowerMock can leverage
    Map<String,String> filterPropertiesKeys(Properties properties, Map<String,Boolean> keyFilterMap) {
        return new HashMap<>(properties.stringPropertyNames().stream()
            .filter(key -> !keyFilterMap.containsKey(key))
            .filter(key -> properties.getProperty(key) != null)
            .collect(Collectors.toMap(key -> (String)key, properties::getProperty)));
    }

    private boolean topicExists(ZkUtils zkUtils, String topic) {
        boolean topicExists = AdminUtils.topicExists(zkUtils, topic);
        log.debug("topic: {} exists={}", topic, topicExists);
        return topicExists;
    }

    void updateTopics(ZkUtils zkUtils, List<String> topicsToUpdate, Map<String, String> topicConfigMap, boolean isStreamNotAvailableInStreamRegistryDB)
        throws StreamCreationException {
        for (String topic : topicsToUpdate) {
            // update topic
            Properties actualTopicConfig = getTopicConfig(zkUtils, topic);
            Map<String, String> actualTopicConfigMap = propertiesToMap(actualTopicConfig);
            if (actualTopicConfigMap.equals(topicConfigMap)) {
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
            //      We should probably do forceSync=true anyway, as it provides a simple way to keep things in sync (#114)
            if (isStreamNotAvailableInStreamRegistryDB) {
                throw new StreamCreationException(String.format("Error: Input configs=%s and actual configs=%s are not same for topic=%s",
                    topicConfigMap, actualTopicConfig, topic));
            }

            // If we got this far, we are "updating" an "existing" stream, and request config is different than
            // what is in stream registry. Go ahead and update now.
            updateTopic(zkUtils, topic, topicConfigMap);
        }
    }

    private Properties getTopicConfig(ZkUtils zkUtils, String topic) {
        return AdminUtils.fetchEntityConfig(zkUtils, ConfigType.Topic(), topic);
    }

    // package scope so that PowerMock can leverage
    Map<String,String> propertiesToMap(Properties properties) {
        return properties.stringPropertyNames().stream()
            .filter(key -> properties.getProperty(key) != null)
            .collect(Collectors.toMap(key -> (String)key,
                properties::getProperty));
    }

    private void updateTopic(ZkUtils zkUtils, String topic, Map<String, String> configMap) {
        Properties topicProperties = new Properties();
        topicProperties.putAll(configMap);
        AdminUtils.changeTopicConfig(zkUtils, topic, topicProperties);
    }

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

    private void shutdownZkUtils(ZkUtils zkUtils) {
        try {
            zkUtils.close();
        } catch (RuntimeException exception) {
            log.error("Unexpected exception caught during zkUtils shutdown.", exception);
        }
    }

    private Materialized createMaterialized(String schemaRegistryUrl){
        final Map<String, String> serdeConfig =
            Collections.singletonMap(SCHEMA_REGISTRY_URL_CONFIG,
                schemaRegistryUrl);

        final SpecificAvroSerde<ClusterKey> keySpecificAvroSerde = new SpecificAvroSerde<>();
        keySpecificAvroSerde.configure(serdeConfig, true);

        final SpecificAvroSerde<ClusterValue> valueSpecificAvroSerde = new SpecificAvroSerde<>();
        valueSpecificAvroSerde.configure(serdeConfig, false);

        return Materialized.<ClusterKey, ClusterValue, KeyValueStore<Bytes, byte[]>>as(infraStateStoreName)
            .withKeySerde(keySpecificAvroSerde)
            .withValueSerde(valueSpecificAvroSerde);
    }
}
