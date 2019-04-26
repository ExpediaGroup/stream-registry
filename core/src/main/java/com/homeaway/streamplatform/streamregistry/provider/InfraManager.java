/* Copyright (c) 2018-Present Expedia Group.
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
package com.homeaway.streamplatform.streamregistry.provider;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import com.homeaway.digitalplatform.streamregistry.ClusterKey;
import com.homeaway.digitalplatform.streamregistry.ClusterValue;
import com.homeaway.streamplatform.streamregistry.exceptions.StreamCreationException;

/**
 * The Infrastructure Manager is a key-value store containing the details of the Stream Registry clusters and their metadata.
 * <br>
 * <b><code>ClusterKey</code></b> - Avro encoded key containing the following fields
 * <ul>
 * <li><code>vpc</code> - VPC of the Stream Registry cluster</li>
 * <li><code>env</code> - Environment, for example staging or production</li>
 * <li><code>hint</code> - If there are multiple clusters in a vpc, the specific classifier of the cluster. E.g: metrics, logging, etc.</li>
 * <li><code>type</code> - If there are multiple clusters in a vpc for a type of hint, the specific type of the cluster. E.g. metrics-aggregate or logging-mirror.</li>
 * </ul>
 * <br>
 * <b><code>ClusterValue</code></b> - Avro encoded value containing containing a <code>Map&lt;String&gt;</code> of Stream Registry cluster properties.
 * This map will be returned to a Stream Registry client at the time of registration.<br>
 * <br>
 * An example of a Confluent Platform Kafka Cluster would contain the following details
 * <ul>
 * <li><code>bootstrap.servers</code></li>
 * <li><code>schema.registry.url</code></li>
 * <li><code>cluster.name</code></li>
 * <li><code>zookeeper.quorum</code></li>
 * </ul>
 */
public interface InfraManager {

    /**
     * Start.
     *
     */
    void start();

    /**
     * Stop.
     *
     */
    void stop();

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


    /**
     * Upsert cluster.
     *
     * @param clusterKey the cluster key
     * @param clusterValue the cluster value
     */
    void upsertCluster(ClusterKey clusterKey, ClusterValue clusterValue);

    /**
     * Creates topics in underlying implementation of InfraManager provider.
     * @param topics topics to creates
     * @param partitions number of partitions for each of those topics
     * @param replicationFactor replicationFactor for each of those topics
     * @param topicConfig topic config to use for each of these topics
     * @param isNewStream whether or not this invocation results from existing or new stream in stream registry.
     * @param forceSync whether or not the provided topic config be forcibly synced with the existing, underlying topic (if not the same).
     * @throws StreamCreationException  when Stream could not be created in the underlying infrastructure for following reasons
     *      a) Input Configs and the existing configs does not match for a new Stream on-boarded to StreamRegistry,
     *      but already available in the infrastructure.
     */
    void upsertTopics(Collection<String> topics, int partitions, int replicationFactor, Properties topicConfig, boolean isNewStream, boolean forceSync)
        throws StreamCreationException;

}
