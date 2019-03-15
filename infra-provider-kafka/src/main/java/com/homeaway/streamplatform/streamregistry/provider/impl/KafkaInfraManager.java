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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import lombok.extern.slf4j.Slf4j;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;

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
import com.homeaway.streamplatform.streamregistry.provider.InfraManager;


/**
 * An {@link InfraManager} implementation backed by a Kafka Streams {@link GlobalKTable}.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
@Slf4j
public class KafkaInfraManager implements InfraManager {

    public static final String INFRAMANAGER_TOPIC = "infraManagerTopic";
    public static final String INFRAMANAGER_STATE_STORE = "infraManagerStateStoreName";
    public static final String INFRA_KSTREAM_PROPS = "infraKStreamsProperties";

    private String infraStateStoreName;
    private KafkaStreams infraKStreams;
    private KafkaProducer<ClusterKey, ClusterValue> infraProducer;
    private GlobalKTable<ClusterKey, ClusterValue> kTable;
    private ReadOnlyKeyValueStore<ClusterKey, ClusterValue> store;
    private String infraManagerTopic;

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

        kTable = infraKStreamBuilder.globalTable(infraManagerTopic, createMaterialized(schemaRegistryUrl));
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
