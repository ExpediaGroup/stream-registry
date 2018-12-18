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
package com.homeaway.streamregistry.provider;

import java.util.Map;
import java.util.Optional;

import com.homeaway.streamregistry.ClusterKey;
import com.homeaway.streamregistry.ClusterValue;

/**
 * The interface Infra manager.
 * Infrastructure Manager - This is a component which has the details of all the streaming clusters and its connection details
 *
 * ClusterKey - Avro format Key containing following fields
 *      - vpc - VPC of the streaming cluster you are looking for
 *      - env - Environment
 *      - hint - If there are multiple clusters in a vpc, the specific classifiers of the clusters ex: primary, etc...
 *      - type - If the pattern is to have multiple clusters for each type of hint. i.e. Producer Cluster/ Consumer Cluster.
 *
 * ClusterValue - Avro format value containing all the connection details for a cluster
 * For example in case of a Kafka Cluster following details are expected to be provided back to the producer/consumer at the time of registering a stream
 *      - Bootstrap Server
 *      - Schema Registry Url
 *      - Cluster Name
 */
public interface InfraManager {

    void start() throws Exception;

    void stop() throws Exception;

    /**
     * Configures this class with given key-value pairs.
     *
     * @param configs the configs
     */
    void configure(Map<String, Object> configs);

    /**
     * Gets key.
     *
     * @param key the key
     * @return the key
     */
    Optional<ClusterValue> getClusterByKey(ClusterKey key);

    /**
     * Gets all clusters.
     *
     * @return the all clusters
     */
    Map<ClusterKey, ClusterValue> getAllClusters();

}
