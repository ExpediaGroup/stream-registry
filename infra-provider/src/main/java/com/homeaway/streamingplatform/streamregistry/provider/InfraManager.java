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
package com.homeaway.streamingplatform.streamregistry.provider;

import java.util.Map;
import java.util.Optional;

import com.homeaway.digitalplatform.streamregistry.ClusterKey;
import com.homeaway.digitalplatform.streamregistry.ClusterValue;

/**
 * The Infrastructure Manager is a key-value store containing the details of the Stream Registry clusters and their metadata.
 * <p>
 * <b><code>ClusterKey</code></b> - Avro encoded key containing the following fields
 * <ul>
 *     <li><code>vpc</code> - VPC of the Stream Registry cluster</li>
 *     <li><code>env</code> - Environment, for example staging or production</li>
 *     <li><code>hint</code> - If there are multiple clusters in a vpc, the specific classifier of the cluster. E.g: metrics, logging, etc.</li>
 *     <li><code>type</code> - If there are multiple clusters in a vpc for a type of hint, the specific type of the cluster. E.g. metrics-aggregate or logging-mirror.</li>
 * </ul></p>
 * <p>
 * <b><code>ClusterValue</code></b> - Avro encoded value containing containing a <code>Map&lt;String&gt;</code> of Stream Registry cluster properties.
 * This map will be returned to a Stream Registry client at the time of registration.
 * <br>An example of a Confluent Platform Kafka Cluster would contain the following details
 * <ul>
 *     <li><code>bootstrap.servers</code></li>
 *     <li><code>schema.registry.url</code></li>
 * </ul></p>
 */
public interface InfraManager {

    void start() throws Exception;

    void stop() throws Exception;

    /**
     * Provides the {@link InfraManager} class with the given values for later configuration.
     *
     * @param configs the configurations specific for the {@link InfraManager}
     */
    void configure(Map<String, Object> configs);

    /**
     * Gets all clusters known to the Stream Registry.
     *
     * @return All clusters known to the Stream Registry
     */
    Map<ClusterKey, ClusterValue> getAllClusters();

    /**
     * Gets a Stream Registry cluster by its key.
     *
     * @param key The key for a Stream Registry cluster
     * @return The cluster in the Stream Registry added with the given key
     */
    Optional<ClusterValue> getClusterByKey(ClusterKey key);

}
