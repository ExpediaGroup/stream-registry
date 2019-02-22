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
package com.homeaway.streamplatform.streamregistry.model;

import java.util.Map;

import javax.validation.constraints.NotNull;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Cluster {

    @NotNull
    private String clusterName;

    /**
     *  Configuration of the cluster
     *  This would be where you would expect bootstrap servers etc,
     *  if it's a Kafka cluster
     */
    @NotNull
    private Map<String, String> clusterConfig;

    /**
     * Type of the cluster.
     * This could be "kafka"
     */
    @Builder.Default
    private String clusterType = "kafka";
    //TODO: This should be provided by InfraManager


    /**
     * Status of this cluster
     * The only supported statuses are
     * PENDING, ONLINE, OFFLINE
     * This cannot be an enum since Avro DTOs
     * that transform these models need to be FULL_TRANSITIVE compatible.
     */
    @Builder.Default
    private String status = "ONLINE";
    //TODO: Currently InfraManager only provides available clusters. Ideally Inframanager should provide all clusters
    // so we can expose all the clusters and their statuses.

}
