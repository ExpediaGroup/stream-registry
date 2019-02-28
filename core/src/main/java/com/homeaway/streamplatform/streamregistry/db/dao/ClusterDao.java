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

import java.util.Map;
import java.util.Optional;

import com.homeaway.streamplatform.streamregistry.exceptions.ClusterNotFoundException;
import com.homeaway.streamplatform.streamregistry.exceptions.InvalidClusterException;
import com.homeaway.streamplatform.streamregistry.model.ClusterKey;
import com.homeaway.streamplatform.streamregistry.model.ClusterValue;
import com.homeaway.streamplatform.streamregistry.model.JsonCluster;

/**
 * Interface for Cluster dao.
 */
public interface ClusterDao {
    /**
     * Gets all clusters.
     *
     * @return the all clusters
     */
    Map<ClusterKey, ClusterValue> getAllClusters();

    /**
     * Gets cluster.
     *
     * @param vpc the vpc
     * @param env the env
     * @param hint the hint
     * @param actorType the actor type
     * @return the cluster
     * @throws ClusterNotFoundException the cluster not found exception
     */
    com.homeaway.digitalplatform.streamregistry.ClusterValue getCluster(String vpc, String env, String hint, String actorType) throws ClusterNotFoundException;

    /**
     * Gets cluster.
     *
     * @param clusterName the cluster name
     * @return the cluster
     */
    Optional<ClusterValue> getCluster(String clusterName);

    /**
     * Upsert cluster.
     *
     * @param jsonCluster the json cluster
     */
    void upsertCluster(JsonCluster jsonCluster) throws InvalidClusterException;
}
