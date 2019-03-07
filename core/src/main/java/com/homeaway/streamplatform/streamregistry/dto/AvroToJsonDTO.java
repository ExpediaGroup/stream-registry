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
package com.homeaway.streamplatform.streamregistry.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;

import org.apache.kafka.clients.producer.ProducerConfig;

import com.homeaway.digitalplatform.streamregistry.AvroStream;
import com.homeaway.digitalplatform.streamregistry.RegionStreamConfiguration;
import com.homeaway.streamplatform.streamregistry.model.ClusterKey;
import com.homeaway.streamplatform.streamregistry.model.ClusterValue;
import com.homeaway.streamplatform.streamregistry.model.Consumer;
import com.homeaway.streamplatform.streamregistry.model.Producer;
import com.homeaway.streamplatform.streamregistry.model.RegionStreamConfig;
import com.homeaway.streamplatform.streamregistry.model.Schema;
import com.homeaway.streamplatform.streamregistry.model.Stream;
import com.homeaway.streamplatform.streamregistry.model.StreamConfig;
import com.homeaway.streamplatform.streamregistry.model.Tags;

public class AvroToJsonDTO {

    public static final String CLUSTER_NAME = "cluster.name";
    public static final String BOOTSTRAP_SERVER = "bootstrap.servers";
    public static final String SCHEMA_REGISTRY_URL = "schema.registry.url";
    public static final String ZOOKEEPER_QUORUM = "zookeeper.quorum";

    public static Stream convertAvroToJson(AvroStream avroStream) {

        Schema keySchema = Schema.builder()
                .schemaString(avroStream.getLatestKeySchema().getSchemaString())
                .id(avroStream.getLatestKeySchema().getId())
                .version(avroStream.getLatestKeySchema().getSubjectId())
                .created(avroStream.getLatestKeySchema().getCreated())
                .updated(avroStream.getLatestKeySchema().getUpdated())
                .build();

        Schema valueSchema = Schema.builder()
                .schemaString(avroStream.getLatestValueSchema().getSchemaString())
                .id(avroStream.getLatestValueSchema().getId())
                .version(avroStream.getLatestValueSchema().getSubjectId())
                .created(avroStream.getLatestValueSchema().getCreated())
                .updated(avroStream.getLatestValueSchema().getUpdated())
                .build();

        com.homeaway.digitalplatform.streamregistry.Tags tagsAvro = avroStream.getTags();
        Tags tags = Tags.builder().productId(tagsAvro.getProductId())
            .portfolioId(tagsAvro.getPortfolioId())
            .brand(tagsAvro.getBrand())
            .assetProtectionLevel(tagsAvro.getAssetProtectionLevel())
            .componentId(tagsAvro.getComponentId())
            .hint(tagsAvro.getHint())
            .build();

        return Stream.builder()
                .name(avroStream.getName())
                .schemaCompatibility(avroStream.getSchemaCompatibility())
                .owner(avroStream.getOwner())
                .created(avroStream.getCreated())
                .updated(avroStream.getUpdated())
                .githubUrl(avroStream.getGithubUrl())
                .latestKeySchema(keySchema)
                .latestValueSchema(valueSchema)
                .isDataNeededAtRest(avroStream.getIsDataNeededAtRest())
                .isAutomationNeeded(avroStream.getIsAutomationNeeded())
                .tags(tags)
                .vpcList(avroStream.getVpcList())
                .replicatedVpcList(avroStream.getReplicatedVpcList())
                .topicConfig(avroStream.getTopicConfig())
                .partitions(avroStream.getPartitions())
                .replicationFactor(avroStream.getReplicationFactor())
                .build();
    }

    public static Producer getJsonProducer(com.homeaway.digitalplatform.streamregistry.Producer avroProducer) {
        Producer.ProducerBuilder builder = Producer.builder();
        if (avroProducer.getActor().getName() != null) {
            builder.name(avroProducer.getActor().getName());
        }
        if (avroProducer.getActor().getRegionStreamConfigurations() != null) {
            List<RegionStreamConfiguration> regionConfigAvroList = avroProducer.getActor().getRegionStreamConfigurations();

            List<RegionStreamConfig> regionConfigJsonList = buildRegionStreamConfig(regionConfigAvroList);
            builder.regionStreamConfigList(regionConfigJsonList);
        }
        return builder.build();
    }

    public static Consumer getJsonConsumer(com.homeaway.digitalplatform.streamregistry.Consumer avroConsumer) {
        Consumer.ConsumerBuilder builder = Consumer.builder();
        if (avroConsumer.getActor().getName() != null) {
            builder.name(avroConsumer.getActor().getName());
        }
        if (avroConsumer.getActor().getRegionStreamConfigurations() != null) {
            List<RegionStreamConfiguration> regionConfigAvroList = avroConsumer.getActor().getRegionStreamConfigurations();

            List<RegionStreamConfig> regionConfigJsonList = buildRegionStreamConfig(regionConfigAvroList);
            builder.regionStreamConfigList(regionConfigJsonList);
        }
        return builder.build();
    }

    private static List<RegionStreamConfig> buildRegionStreamConfig(List<RegionStreamConfiguration> regionConfigAvroList) {
        List<RegionStreamConfig> regionConfigJsonList = new ArrayList<>();
        for (RegionStreamConfiguration regionConfig : regionConfigAvroList) {
            Map<String, String> streamConfigMap = new HashMap<>();
            if(regionConfig.getStreamConfiguration()!= null) {
                // Copy from Avro to StreamConfiguration Object and then get it from Object to Json
                StreamConfig streamConfig = new StreamConfig();
                streamConfig.setBootstrapServers(Optional.of(regionConfig.getStreamConfiguration().get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG)));
                streamConfig.setSchemaRegistryUrl(Optional.of(regionConfig.getStreamConfiguration().get(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG)));

                if(streamConfig.getBootstrapServers().isPresent()) {
                    streamConfigMap.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, streamConfig.getBootstrapServers().get());
                }
                if(streamConfig.getSchemaRegistryUrl().isPresent()) {
                    streamConfigMap.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, streamConfig.getSchemaRegistryUrl().get());
                }
            }

            RegionStreamConfig regionKafkaStreamJson = RegionStreamConfig
                .builder()
                .region(regionConfig.getRegion())
                .cluster(regionConfig.getCluster())
                .streamConfiguration(streamConfigMap)
                .topics(regionConfig.getTopics())
                .build();

            regionConfigJsonList.add(regionKafkaStreamJson);
        }
        return regionConfigJsonList;
    }


    public static ClusterKey getJsonClusterKey(com.homeaway.digitalplatform.streamregistry.ClusterKey avroClusterKey){
            return ClusterKey.builder()
                .vpc(avroClusterKey.getVpc())
                .env(avroClusterKey.getEnv())
                .hint(avroClusterKey.getHint())
                .type(avroClusterKey.getType())
                .build();
    }


    public static ClusterValue getJsonClusterValue(com.homeaway.digitalplatform.streamregistry.ClusterValue avroClusterValue){
        return ClusterValue.builder()
            .bootstrapServers(avroClusterValue.getClusterProperties().get(BOOTSTRAP_SERVER))
            .clusterName(avroClusterValue.getClusterProperties().get(CLUSTER_NAME))
            .schemaRegistryURL(avroClusterValue.getClusterProperties().get(SCHEMA_REGISTRY_URL))
            .zookeeperQuorum(avroClusterValue.getClusterProperties().get(ZOOKEEPER_QUORUM))
            .build();
    }
}