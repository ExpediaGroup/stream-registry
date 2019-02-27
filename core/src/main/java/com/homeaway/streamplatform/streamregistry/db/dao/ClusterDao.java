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
import com.homeaway.streamplatform.streamregistry.model.ClusterKey;
import com.homeaway.streamplatform.streamregistry.model.ClusterValue;
import com.homeaway.streamplatform.streamregistry.model.JsonCluster;

/**
 * Interface for Cluster dao.
 */
public interface ClusterDao {
    Map<ClusterKey, ClusterValue> getAllClusters();

    com.homeaway.digitalplatform.streamregistry.ClusterValue getCluster(String vpc, String env, String hint, String actorType) throws ClusterNotFoundException;

    Optional<ClusterValue> getCluster(String clusterName);

    void upsertCluster(JsonCluster jsonCluster);
}
