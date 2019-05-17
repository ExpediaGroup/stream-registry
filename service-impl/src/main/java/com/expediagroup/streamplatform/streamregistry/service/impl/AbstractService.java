/**
 * Copyright (C) 2018-2019 Expedia, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.expediagroup.streamplatform.streamregistry.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import com.google.common.base.Preconditions;

import com.expediagroup.streamplatform.streamregistry.core.exception.ClusterNotFoundException;
import com.expediagroup.streamplatform.streamregistry.model.Actor;
import com.expediagroup.streamplatform.streamregistry.model.Actor.ActorBuilder;
import com.expediagroup.streamplatform.streamregistry.model.ClusterValue;
import com.expediagroup.streamplatform.streamregistry.model.RegionStreamConfig;
import com.expediagroup.streamplatform.streamregistry.model.Stream;
import com.expediagroup.streamplatform.streamregistry.service.ClusterService;
import com.expediagroup.streamplatform.streamregistry.service.RegionService;
import com.expediagroup.streamplatform.streamregistry.repository.InfraManager;

@Slf4j
public abstract class AbstractService {
  protected static final String BOOTSTRAP_SERVERS_CONFIG = "bootstrap.servers"; //ProducerConfig.BOOTSTRAP_SERVERS_CONFIG
  private static final String SCHEMA_REGISTRY_URL_CONFIG = "schema.registry.url"; //AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG

  public static final String PRIMARY_HINT = "primary";
  public static final String CLUSTER_NAME = "cluster.name";
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

  protected Actor populateActorStreamConfig(
      String streamName, String region,
      Actor actor, String operation, List<String> topicNamePostFixes, String hint,
      String actorType) throws ClusterNotFoundException {

    ActorBuilder builder = Actor.builder();

    builder.name(actor.getName());
    List<RegionStreamConfig> regionStreamConfigList = new ArrayList<>();
    regionStreamConfigList.addAll(actor.getRegionStreamConfig());

    ClusterValue clusterValue = clusterService.getCluster(region, env, hint, actorType).getClusterValue();
    Optional<String> bootstrapServers = Optional.ofNullable(clusterValue.getBootstrapServers());
    Optional<String> schemaRegistryURL = Optional.ofNullable(clusterValue.getSchemaRegistryURL());
    Optional<String> clusterName = Optional.ofNullable(clusterValue.getClusterName());

    if (!clusterName.isPresent()) {
      throw new IllegalStateException(String.format("Cluster name cannot be null. region=%s ; env=%s ; hint=%s ; actorType=%s ; Cluster Details: %s",
          region, env, hint, actorType, clusterValue));
    }

    if (operation.equalsIgnoreCase(OPERATION.CREATE.name())) {

      Map<String, String> configMap = new HashMap<>();

      List<String> topics = topicNamePostFixes
          .stream()
          .map(streamName::concat)
          .collect(Collectors.toList());

      //TODO move out to Kafka specific service module
      bootstrapServers.ifPresent(s -> configMap.put(BOOTSTRAP_SERVERS_CONFIG, s));
      schemaRegistryURL.ifPresent(s -> configMap.put(SCHEMA_REGISTRY_URL_CONFIG, s));

      RegionStreamConfig regionStreamConfig = RegionStreamConfig
          .builder()
          .region(region)
          .cluster(clusterName.get())
          .topics(topics)
          .streamConfiguration(configMap)
          .build();

      regionStreamConfigList.add(regionStreamConfig);
    } else if (operation.equalsIgnoreCase(OPERATION.UPDATE.name())) {
      for (RegionStreamConfig regionStreamConfig : regionStreamConfigList) {
        if (regionStreamConfig.getRegion().equals(region)) {
          Map<String, String> configMap = regionStreamConfig.getStreamConfiguration();
          regionStreamConfig.setCluster(clusterName.get());
          bootstrapServers.ifPresent(s -> configMap.put(BOOTSTRAP_SERVERS_CONFIG, s));
          schemaRegistryURL.ifPresent(s -> configMap.put(SCHEMA_REGISTRY_URL_CONFIG, s));
          regionStreamConfig.setStreamConfiguration(configMap);
        }
      }
    }
    builder.regionStreamConfig(regionStreamConfigList);

    log.info(
        "Populated Cluster information into the Stream AvroObject. actorName={} ; region={} ; BootstrapServer={} ; " +
            "schemaRegistryURL={} ; hint={} ; clusterName={}",
        actor.getName(), region, bootstrapServers, schemaRegistryURL, hint, clusterName);

    return builder.build();
  }

  protected String getDefaultHint(Stream stream) {
    String streamHint = stream.getTags().getHint();
    return (streamHint == null || streamHint.trim().matches("(?i:string)?")) ? AbstractService.PRIMARY_HINT : streamHint.trim().toLowerCase();
  }

  public enum OPERATION {
    CREATE,
    UPDATE
  }
}
