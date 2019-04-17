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
package com.homeaway.streamplatform.streamregistry.dto;

import java.util.*;

import javax.ws.rs.BadRequestException;

import com.homeaway.digitalplatform.streamregistry.*;
import com.homeaway.streamplatform.streamregistry.model.ClusterKey;
import com.homeaway.streamplatform.streamregistry.model.ClusterValue;
import com.homeaway.streamplatform.streamregistry.model.ConnectorConfig;
import com.homeaway.streamplatform.streamregistry.model.Stream;

public class JsonToAvroDTO {

    public static final String CLUSTER_NAME = "cluster.name";
    public static final String BOOTSTRAP_SERVER = "bootstrap.servers";
    public static final String SCHEMA_REGISTRY_URL = "schema.registry.url";
    public static final String ZOOKEEPER_QUORUM = "zookeeper.quorum";

    public static AvroStream convertJsonToAvro(Stream jsonStream, OperationType operationType) {

        com.homeaway.streamplatform.streamregistry.model.Schema jsonKeySchema = jsonStream.getLatestKeySchema();
        com.homeaway.streamplatform.streamregistry.model.Schema jsonValueSchema = jsonStream.getLatestValueSchema();
        com.homeaway.streamplatform.streamregistry.model.Tags jsonTags = jsonStream.getTags();

        if (jsonKeySchema == null || jsonValueSchema == null
            || jsonStream.getOwner() == null
            || jsonTags == null || jsonTags.getProductId() == -1) {
            throw new BadRequestException("Input Validation Failed. Mandatory Input Fields: keySchema, valueSchema, tags.productId, owner");
        }

        Schema keySchema = Schema.newBuilder()
                .setSchemaString(jsonKeySchema.getSchemaString())
                .setId(jsonKeySchema.getId())
                .setSubjectId(jsonKeySchema.getVersion())
                .setCreated(String.valueOf(Calendar.getInstance().getTime()))
                .setUpdated(String.valueOf(Calendar.getInstance().getTime()))
                .build();


        Schema valueSchema = Schema.newBuilder()
                .setSchemaString(jsonValueSchema.getSchemaString())
                .setId(jsonValueSchema.getId())
                .setSubjectId(jsonValueSchema.getVersion())
                .setCreated(String.valueOf(Calendar.getInstance().getTime()))
                .setUpdated(String.valueOf(Calendar.getInstance().getTime()))
                .build();
        Tags tags = Tags.newBuilder().setProductId(jsonTags.getProductId())
            .setPortfolioId(jsonTags.getPortfolioId())
            .setBrand(jsonTags.getBrand())
            .setAssetProtectionLevel(jsonTags.getAssetProtectionLevel())
            .setComponentId(jsonTags.getComponentId())
            .setHint(jsonTags.getHint())
            .build();

        AvroStream avroStream= AvroStream.newBuilder()
                .setName(jsonStream.getName())
                .setLatestKeySchema(keySchema)
                .setLatestValueSchema(valueSchema)
                .setOperationType(operationType)
                .setOwner(jsonStream.getOwner())
                .setUpdated(System.currentTimeMillis())
                .setGithubUrl(jsonStream.getGithubUrl())
                .setIsDataNeededAtRest(jsonStream.getIsDataNeededAtRest())
                .setIsAutomationNeeded(jsonStream.getIsAutomationNeeded())
                .setTags(tags)
                .setVpcList(jsonStream.getVpcList())
                .setReplicatedVpcList(jsonStream.getReplicatedVpcList())
                .setTopicConfig(jsonStream.getTopicConfig())
                .setPartitions(jsonStream.getPartitions())
                .setReplicationFactor(jsonStream.getReplicationFactor())
                .setAlertConfigList(convertAlertListToAvro(jsonStream.getAlertConfigList()))
                .setConnectorConfigList(convertConnectorListToAvro(jsonStream.getConnectorConfigList()))
                .build();

        if (jsonStream.getSchemaCompatibility() != null)
            avroStream.setSchemaCompatibility(jsonStream.getSchemaCompatibility());

        return avroStream;

    }

    public static List<com.homeaway.digitalplatform.streamregistry.Connector> convertConnectorListToAvro(List<ConnectorConfig> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        ArrayList<com.homeaway.digitalplatform.streamregistry.Connector> retList = new ArrayList<>();
        for (ConnectorConfig elem : list) {
            if (elem != null) {
                Map<String, String> configs = new HashMap<>();
                if (elem.getProperties() != null) {
                    elem.getProperties().forEach(configs::put);
                }
                retList.add(new com.homeaway.digitalplatform.streamregistry.Connector(elem.getName(), elem.getType(), configs));
            }
        }
        return retList;
    }

    public static List<com.homeaway.digitalplatform.streamregistry.Alert> convertAlertListToAvro(List<com.homeaway.streamplatform.streamregistry.model.AlertConfig> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        ArrayList<com.homeaway.digitalplatform.streamregistry.Alert> retList = new ArrayList<>();
        for (com.homeaway.streamplatform.streamregistry.model.AlertConfig elem : list) {
            if (elem != null && elem.getType() != null && elem.getDestination() != null) {
                retList.add(new com.homeaway.digitalplatform.streamregistry.Alert(elem.getType(), elem.getDestination()));
            }
        }
        return retList;
    }

    public static com.homeaway.digitalplatform.streamregistry.ClusterKey getAvroClusterKey(ClusterKey jsonClusterKey){
        return com.homeaway.digitalplatform.streamregistry.ClusterKey.newBuilder()
            .setVpc(jsonClusterKey.getVpc())
            .setEnv(jsonClusterKey.getEnv())
            .setHint(jsonClusterKey.getHint())
            .setType(jsonClusterKey.getType())
            .build();
    }

    public static com.homeaway.digitalplatform.streamregistry.ClusterValue getAvroClusterValue(ClusterValue jsonClusterValue){
        Map<String, String> clusterProperties = new HashMap<>();
        clusterProperties.put(CLUSTER_NAME, jsonClusterValue.getClusterName());
        clusterProperties.put(BOOTSTRAP_SERVER, jsonClusterValue.getBootstrapServers());
        clusterProperties.put(SCHEMA_REGISTRY_URL, jsonClusterValue.getSchemaRegistryURL());
        clusterProperties.put(ZOOKEEPER_QUORUM, jsonClusterValue.getZookeeperQuorum());

        return com.homeaway.digitalplatform.streamregistry.ClusterValue.newBuilder()
            .setClusterProperties(clusterProperties)
            .build();
    }
}