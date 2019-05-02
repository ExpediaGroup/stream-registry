/* Copyright (c) 2018-2019 Expedia, Inc.
 * All rights reserved.  http://www.expediagroup.com

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
package com.expediagroup.streamplatform.streamregistry.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import com.google.common.base.Preconditions;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;

import org.apache.kafka.clients.producer.ProducerConfig;

import com.expediagroup.streamplatform.streamregistry.Actor;
import com.expediagroup.streamplatform.streamregistry.AvroStream;
import com.expediagroup.streamplatform.streamregistry.ClusterValue;
import com.expediagroup.streamplatform.streamregistry.RegionStreamConfiguration;
import com.expediagroup.streamplatform.streamregistry.exceptions.ClusterNotFoundException;
import com.expediagroup.streamplatform.streamregistry.provider.InfraManager;

@Slf4j
public abstract class AbstractService {

    public static final String PRIMARY_HINT = "primary";
    public static final String CLUSTER_NAME = "cluster.name";

    public enum OPERATION {
        CREATE,
        UPDATE
    }

    protected final String env;

    protected final RegionService regionService;

    protected final InfraManager infraManager;

    protected final ClusterService clusterService;

    public AbstractService(String env,
                           RegionService regionService,
                           ClusterService clusterService,
                           InfraManager infraManager) {
        Preconditions.checkNotNull(env, "env should not be null");
        Preconditions.checkNotNull(regionService, "regionService should not be null");
        Preconditions.checkNotNull(infraManager, "infraManager should not be null");
        Preconditions.checkNotNull(clusterService, "clusterService should not be null");

        this.env = env;
        this.regionService = regionService;
        this.infraManager = infraManager;
        this.clusterService = clusterService;
    }

    protected Actor populateActorStreamConfig(String streamName, String region,
        Actor actor, String operation, List<String> topicNamePostFixes, String hint,
        String actorType) throws ClusterNotFoundException {

        Actor.Builder actorBuilder = Actor.newBuilder();

        actorBuilder.setName(actor.getName());
        actorBuilder.setRegionStreamConfigurations(actor.getRegionStreamConfigurations());

        ClusterValue clusterValue = clusterService.getCluster(region, env, hint, actorType);
        Optional<String> bootstrapServers = Optional.ofNullable(clusterValue.getClusterProperties().get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
        Optional<String> schemaRegistryURL = Optional.ofNullable(clusterValue.getClusterProperties().get(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG));
        Optional<String> clusterName = Optional.ofNullable(clusterValue.getClusterProperties().get(CLUSTER_NAME));

        if (!clusterName.isPresent()) {
            throw new IllegalStateException(String.format("Cluster name cannot be null. region=%s ; env=%s ; hint=%s ; actorType=%s ; Cluster Details: %s",
                    region, env, hint, actorType, clusterValue));
        }

        if (operation.equalsIgnoreCase(OPERATION.CREATE.name())) {

            actorBuilder.setRegionStreamConfigurations(new ArrayList<>());
            Map<String, String> configMap = new HashMap<>();

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

    protected String getDefaultHint(AvroStream avroStream) {
        String streamHint = avroStream.getTags().getHint();
        return (streamHint == null || streamHint.trim().matches("(?i:string)?")) ? AbstractService.PRIMARY_HINT : streamHint.trim().toLowerCase();
    }
}
