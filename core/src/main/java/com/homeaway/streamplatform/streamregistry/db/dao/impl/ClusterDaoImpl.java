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
package com.homeaway.streamplatform.streamregistry.db.dao.impl;

import java.util.Map;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

import com.homeaway.digitalplatform.streamregistry.ClusterKey;
import com.homeaway.digitalplatform.streamregistry.ClusterValue;
import com.homeaway.streamplatform.streamregistry.db.dao.ClusterDao;
import com.homeaway.streamplatform.streamregistry.exceptions.ClusterNotFoundException;
import com.homeaway.streamplatform.streamregistry.provider.InfraManager;

/**
 * The type Cluster dao.
 */
@Slf4j
public class ClusterDaoImpl implements ClusterDao{

    /**
     * The Infra manager.
     */
    protected final InfraManager infraManager;

    /**
     * Instantiates a new Cluster dao.
     *
     * @param infraManager the infra manager
     */
    public ClusterDaoImpl(InfraManager infraManager) {
        this.infraManager = infraManager;
    }

    /**
     * Get all Clusters from the Infra Manager
     * @return Map of ClusterKey and ClusterValue
     */
    @Override
    public Map<ClusterKey, ClusterValue> getAllClusters() {
        return infraManager.getAllClusters();
    }

    /**
     * This method looks up in the statestore for cluster information for a given key.
     *
     * @param vpc VPC of the region that the cluster belongs to. Eg: us-east-1-vpc-defa0000
     * @param env Environment the cluster belongs to
     * @param hint Additional information that works in combination with vpc
     * @param actorType Type that defines if the operation is happening for a producer or consumer
     * @return ClusterValue cluster details
     * @throws ClusterNotFoundException thrown if no cluster is found in the region
     */
    public ClusterValue getCluster(String vpc, String env, String hint, String actorType) throws ClusterNotFoundException {
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
}
