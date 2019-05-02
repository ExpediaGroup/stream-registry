/* Copyright (C) 2018-2019 Expedia, Inc.
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
package com.expediagroup.streamplatform.streamregistry.service;

import java.util.List;

import com.expediagroup.streamplatform.streamregistry.ClusterValue;
import com.expediagroup.streamplatform.streamregistry.exceptions.ClusterNotFoundException;
import com.expediagroup.streamplatform.streamregistry.exceptions.InvalidClusterException;
import com.expediagroup.streamplatform.streamregistry.model.JsonCluster;

/**
 * Interface for Cluster dao.
 */
public interface ClusterService {
    /**
     * Gets all clusters.
     *
     * @return the all clusters
     */
    List<JsonCluster> getAllClusters();

    /**
     * Gets cluster.
     *
     * @param vpc - the vpc
     * @param env - the env
     * @param hint - the hint
     * @param actorType - the actor type
     * @return ClusterValue
     * @throws ClusterNotFoundException - if the cluster is not found
     */
    ClusterValue getCluster(String vpc, String env, String hint, String actorType) throws ClusterNotFoundException;

    /**
     * Upsert cluster.
     *
     * @param jsonCluster the json cluster
     * @throws InvalidClusterException - if the cluster properties are not defined
     */
    void upsertCluster(JsonCluster jsonCluster) throws InvalidClusterException;
}
