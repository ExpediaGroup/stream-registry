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
package com.homeaway.streamplatform.streamregistry.db.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.clients.producer.ProducerConfig;

import com.homeaway.digitalplatform.streamregistry.Actor;
import com.homeaway.digitalplatform.streamregistry.AvroStream;
import com.homeaway.digitalplatform.streamregistry.AvroStreamKey;
import com.homeaway.digitalplatform.streamregistry.ClusterKey;
import com.homeaway.digitalplatform.streamregistry.ClusterValue;
import com.homeaway.digitalplatform.streamregistry.OperationType;
import com.homeaway.digitalplatform.streamregistry.RegionStreamConfiguration;
import com.homeaway.streamplatform.streamregistry.exceptions.ClusterNotFoundException;
import com.homeaway.streamplatform.streamregistry.provider.InfraManager;
import com.homeaway.streamplatform.streamregistry.streams.ManagedKStreams;
import com.homeaway.streamplatform.streamregistry.streams.ManagedKafkaProducer;

@Slf4j
public abstract class AbstractDao {

    public static final String PRIMARY_HINT = "primary";
    public static final String CLUSTER_NAME = "cluster.name";

    public enum OPERATION {
        CREATE,
        UPDATE
    }

    protected final ManagedKafkaProducer kafkaProducer;

    protected final ManagedKStreams kStreams;

    protected final String env;

    protected final RegionDao regionDao;

    protected final InfraManager infraManager;

    protected final KafkaManager kafkaManager;

    public AbstractDao(ManagedKafkaProducer managedKafkaProducer,
        ManagedKStreams kStreams,
        String env,
        RegionDao regionDao,
        InfraManager infraManager,
        KafkaManager kafkaManager) {
        this.kafkaProducer = managedKafkaProducer;
        this.kStreams = kStreams;
        this.env = env;
        this.regionDao = regionDao;
        this.infraManager = infraManager;
        this.kafkaManager = kafkaManager;
    }

    /**
     * This method looks up in the statestore for cluster information for a given key.
     *
     * @param vpc VPC of the region that the cluster belongs to. Eg: us-east-1-vpc-defa0000
     * @param env Environment the cluster belongs to
     * @param hint Additional information that works in combination with vpc
     * @param actorType Type that defines if the operation is happening for a producer or consumer
     * @return ClusterValue
     * @throws ClusterNotFoundException thrown if no cluster is found in the region
     */
    protected ClusterValue getClusterDetails(String vpc, String env, String hint, String actorType) throws ClusterNotFoundException {
        log.info("Cluster Details: vpc: {}, env: {}, hint: {}, actorType: {}", vpc, env, hint, actorType);
        ClusterKey clusterKey = new ClusterKey(vpc, env, hint, null);

        Optional<ClusterValue> clusterValue = infraManager.getClusterByKey(clusterKey);

        if (clusterValue.isPresent()) {
            log.info("Cluster Information found - {}", clusterValue);
            return clusterValue.get();
        }
        // If no cluster information is found set the actorType and look again.
        clusterKey.setType(actorType);

        clusterValue = infraManager.getClusterByKey(clusterKey);

        if (clusterValue.isPresent()) {
            log.info("Cluster Information found - {}", clusterValue);
            return clusterValue.get();
        } else {
            log.info("Cluster Information not found for key - {}", clusterKey);
            throw new ClusterNotFoundException(clusterKey.toString());
        }
    }

    protected Pair<AvroStreamKey, Optional<AvroStream>> getAvroStreamKeyValue(String streamName) {
        AvroStreamKey key = AvroStreamKey.newBuilder().setStreamName(streamName).build();
        Optional<AvroStream> value = kStreams.getAvroStreamForKey(key);
        return new ImmutablePair<>(key, value);
    }

    public void updateAvroStream(AvroStream stream) {
        AvroStreamKey key = AvroStreamKey.newBuilder().setStreamName(stream.getName()).build();
        stream.setOperationType(OperationType.UPSERT);
        try {
            kafkaProducer.log(key, stream);
        } catch (Exception e) {
            log.error("Error logging stream - {}", stream.toString());
        }
    }

    protected com.homeaway.digitalplatform.streamregistry.Actor populateActorStreamConfig(String streamName, String region,
        com.homeaway.digitalplatform.streamregistry.Actor actor, String operation, List<String> topicNamePostFixes, String hint,
        String actorType, Map<String, String> topicConfigMap) {

        Actor.Builder actorBuilder = Actor.newBuilder();

        actorBuilder.setName(actor.getName());
        actorBuilder.setRegionStreamConfigurations(actor.getRegionStreamConfigurations());

        ClusterValue clusterValue = getClusterDetails(region, env, hint, actorType);
        Optional<String> bootstrapServers = Optional.ofNullable(clusterValue.getClusterProperties().get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
        Optional<String> schemaRegistryURL = Optional.ofNullable(clusterValue.getClusterProperties().get(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG));
        Optional<String> clusterName = Optional.ofNullable(clusterValue.getClusterProperties().get(CLUSTER_NAME));

        if (!clusterName.isPresent()) {
            throw new IllegalStateException("Cluster name cannot be null");
        }

        if (operation.equalsIgnoreCase(OPERATION.CREATE.name())) {

            actorBuilder.setRegionStreamConfigurations(new ArrayList<>());
            Map<String, String> configMap = new HashMap<>();

            Properties topicConfig = new Properties();
            if (topicConfigMap != null) {
                topicConfig.putAll(topicConfigMap);
            }

            List<String> topics = topicNamePostFixes
                .stream()
                .map(streamName::concat)
                .collect(Collectors.toList());

            bootstrapServers.ifPresent(s -> configMap.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, s));
            schemaRegistryURL.ifPresent(s -> configMap.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, s));

            RegionStreamConfiguration regionStreamConfiguration =
                new RegionStreamConfiguration(region, clusterName.get(), topics, configMap);

            actorBuilder.getRegionStreamConfigurations().add(regionStreamConfiguration);
        } else if (operation.equalsIgnoreCase(OPERATION.UPDATE.name())) {
            for (RegionStreamConfiguration regionStreamConfiguration : actor.getRegionStreamConfigurations()) {
                if (regionStreamConfiguration.getRegion().equals(region)) {
                    Map<String, String> configMap = regionStreamConfiguration.getStreamConfiguration();
                    regionStreamConfiguration.setCluster(clusterName.get());
                    bootstrapServers.ifPresent(s -> configMap.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, s));
                    schemaRegistryURL.ifPresent(s -> configMap.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, s));
                    regionStreamConfiguration.setStreamConfiguration(configMap);
                }
            }
        }

        log.info(
            "Populated Cluster information into the Stream AvroObject. actorName={} ; region={} ; BootstrapServer={} ; " +
                "schemaRegistryURL={} ; hint={} ; clusterName={}",
            actor.getName(), region, bootstrapServers, schemaRegistryURL, hint, clusterName);

        return actorBuilder.build();
    }
}
