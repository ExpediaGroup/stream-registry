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
package com.expediagroup.streamplatform.streamregistry.extensions.validation;

import com.expediagroup.streamplatform.streamregistry.exceptions.InvalidClusterException;
import com.expediagroup.streamplatform.streamregistry.model.ClusterKey;
import com.expediagroup.streamplatform.streamregistry.model.ClusterValue;
import com.expediagroup.streamplatform.streamregistry.model.JsonCluster;

/**
 * The type Cluster validator.
 */
public class ClusterValidator {

    /**
     * Validate.
     *
     * @param cluster - the cluster
     * @throws InvalidClusterException - if the cluster parameters are not correctly defined
     */
    public static void validate(JsonCluster cluster) throws InvalidClusterException {
        if (cluster == null || cluster.getClusterKey() == null || cluster.getClusterValue() == null) {
            throw new InvalidClusterException("Cluster Key/Value cannot be null ");
        }

        ClusterKey clusterKey = cluster.getClusterKey();

        if (clusterKey.getEnv() == null || clusterKey.getEnv().trim().equals("") ||
            clusterKey.getHint() == null || clusterKey.getHint().trim().equals("") ||
            clusterKey.getVpc() == null || clusterKey.getVpc().trim().equals("")) {
            throw new InvalidClusterException(String.format("Cluster Keys (Env/Hint/VPC) cannot be null or empty, clusterKey:%s",clusterKey));
        }

        ClusterValue clusterValue = cluster.getClusterValue();

        if (clusterValue.getClusterName() == null || clusterValue.getClusterName().trim().equals("") ||
            clusterValue.getBootstrapServers() == null || clusterValue.getBootstrapServers().trim().equals("") ||
            clusterValue.getZookeeperQuorum() == null || clusterValue.getZookeeperQuorum().trim().equals("")) {
            throw new InvalidClusterException(String.format("Cluster Values (Cluster Name/Bootstrap servers/Zookeeper Quorum) cannot be null or empty, clusterKey:%s",clusterKey));
        }
    }
}
