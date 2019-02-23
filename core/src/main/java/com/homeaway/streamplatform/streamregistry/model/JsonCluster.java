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


import javax.validation.constraints.NotNull;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(builder = JsonCluster.JsonClusterBuilder.class)
@Builder
@Getter
@ToString
public class JsonCluster {

    @NotNull
    private Key clusterKey;

    @NotNull
    private Value clusterValue;

    @Getter
    @Builder
    @JsonDeserialize(builder = Key.KeyBuilder.class)
    @ToString
    public static class Key {

        @NotNull
        String vpc;

        @NotNull
        String env;

        @NotNull
        String hint;

        String type;
    }

    @Getter
    @Builder
    @JsonDeserialize(builder = Value.ValueBuilder.class)
    @ToString
    public static class Value {
        @NotNull
        String clusterName;

        @NotNull
        String bootstrapServers;

        @NotNull
        String zookeeperQuorum;

        String schemaRegistryURL;
    }

}
