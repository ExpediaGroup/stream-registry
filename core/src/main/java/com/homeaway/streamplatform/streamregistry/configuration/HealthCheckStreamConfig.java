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
package com.homeaway.streamplatform.streamregistry.configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import lombok.Data;

/**
 * A stream is upserted for every HeathCheck. This configuration object has the properties of the created Stream.
 */
@Data
public class HealthCheckStreamConfig {

    /**
     * name of the Stream upserted at HeathCheck
     */
    @Valid
    @NotNull
    String name;

    /**
     * region of the Kafka Cluster where the Stream gets upserted at HeathCheck
     */
    @Valid
    @NotNull
    String clusterRegion;

    /**
     * partition count of the Stream upserted at HeathCheck
     */
    @Valid
    @NotNull
    int partitions;

    /**
     * replication factor of the Stream upserted at HeathCheck
     */
    @Valid
    @NotNull
    int replicationFactor;

}