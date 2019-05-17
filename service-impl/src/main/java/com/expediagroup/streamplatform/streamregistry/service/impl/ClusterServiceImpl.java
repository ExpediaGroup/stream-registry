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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import com.expediagroup.streamplatform.streamregistry.core.exception.ClusterNotFoundException;
import com.expediagroup.streamplatform.streamregistry.core.exception.InvalidClusterException;
import com.expediagroup.streamplatform.streamregistry.core.extension.validation.ClusterValidator;
import com.expediagroup.streamplatform.streamregistry.model.Cluster;
import com.expediagroup.streamplatform.streamregistry.model.ClusterKey;
import com.expediagroup.streamplatform.streamregistry.model.ClusterValue;
import com.expediagroup.streamplatform.streamregistry.repository.InfraManager;
import com.expediagroup.streamplatform.streamregistry.service.ClusterService;

/**
 * The type Cluster dao.
 */
@Slf4j
public class ClusterServiceImpl implements ClusterService {

  /**
   * The Infra manager.
   */
  protected final InfraManager infraManager;

  /**
   * Instantiates a new Cluster dao.
   *
   * @param infraManager the infra manager
   */
  public ClusterServiceImpl(InfraManager infraManager) {
    this.infraManager = infraManager;
  }

  /**
   * Get all Clusters from the Infra Manager
   *
   * @return Map of ClusterKey and ClusterValue
   */
  @Override
  public List<Cluster> getAllClusters() throws IllegalStateException {
    log.info("Get all clusters from infra manager...");
    return infraManager
        .getAllClusters()
        .entrySet()
        .stream()
        .map(e -> Cluster.builder().clusterKey(e.getKey()).clusterValue(e.getValue()).build())
        .collect(Collectors.toList());
  }

  /**
   * This method looks up in the statestore for cluster information for a given key.
   *
   * @param vpc       VPC of the region that the cluster belongs to. Eg: us-east-1-vpc-defa0000
   * @param env       Environment the cluster belongs to
   * @param hint      Additional information that works in combination with vpc
   * @param actorType Type that defines if the operation is happening for a producer or consumer
   * @return ClusterValue cluster details
   * @throws ClusterNotFoundException thrown if no cluster is found in the region
   */
  public Cluster getCluster(String vpc, String env, String hint, String actorType) throws ClusterNotFoundException {
    log.info("Cluster Details: vpc: {}, env: {}, hint: {}, actorType: {}", vpc, env, hint, actorType);

    // Because the default is to have producer/consumer be the same cluster, lets see first try actorType=null to see if a cluster exists
    ClusterKey clusterKey = ClusterKey
        .builder()
        .vpc(vpc)
        .env(env)
        .hint(hint)
        .type(null)
        .build();

    Optional<ClusterValue> clusterValue = infraManager.getClusterByKey(clusterKey);

    if (!clusterValue.isPresent()) {
      clusterKey = clusterKey.withType(actorType);
      clusterValue = infraManager.getClusterByKey(clusterKey);

      // There is no clusters second time as well so throwing out the exception.
      if (!clusterValue.isPresent()) {
        throw new ClusterNotFoundException(String.format("Cluster Information not found for key - %s", clusterKey.toString()));
      }
    }

    return Cluster.builder().clusterKey(clusterKey).clusterValue(clusterValue.get()).build();
  }

  /**
   * Upsert a cluster object
   *
   * @param cluster - the input JSON cluster
   * @throws InvalidClusterException - if cluster parameters are not defined
   */
  @Override
  public void upsertCluster(Cluster cluster) throws InvalidClusterException {
    log.info("Upserting Cluster {}", cluster);

    ClusterValidator.validate(cluster);

    infraManager.upsertCluster(cluster.getClusterKey(), cluster.getClusterValue());
  }
}
