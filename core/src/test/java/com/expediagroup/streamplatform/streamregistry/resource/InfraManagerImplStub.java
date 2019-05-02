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
package com.expediagroup.streamplatform.streamregistry.resource;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import com.expediagroup.streamplatform.streamregistry.ClusterKey;
import com.expediagroup.streamplatform.streamregistry.ClusterValue;
import com.expediagroup.streamplatform.streamregistry.provider.InfraManager;

public class InfraManagerImplStub implements InfraManager {

    private final Map<ClusterKey, ClusterValue> clusterMap = new HashMap<>();

    @Override
    public void start() {}

    @Override
    public void stop() {}

    @Override
    public void configure(Map<String, Object> configs) {}

    @Override
    public void upsertCluster(ClusterKey key, ClusterValue value) {
        this.clusterMap.put(key, value);
    }

    @Override
    public Optional<ClusterValue> getClusterByKey(ClusterKey key) {
        return this.clusterMap.get(key) == null ? Optional.empty() : Optional.of(this.clusterMap.get(key));
    }

    @Override
    public void upsertTopics(Collection<String> topics, int partitions, int replicationFactor, Properties topicConfig, boolean isNewStream) {
    }

    @Override
    public Map<ClusterKey, ClusterValue> getAllClusters() {
        return clusterMap;
    }

}
