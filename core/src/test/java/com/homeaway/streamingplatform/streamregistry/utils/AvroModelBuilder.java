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
package com.homeaway.streamingplatform.streamregistry.utils;

import static com.homeaway.streamingplatform.streamregistry.resource.BaseResourceIT.US_EAST_CLUSTER_GENERAL;
import static com.homeaway.streamingplatform.streamregistry.resource.BaseResourceIT.US_EAST_REGION;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import com.homeaway.digitalplatform.streamregistry.Actor;
import com.homeaway.digitalplatform.streamregistry.AvroStream;
import com.homeaway.digitalplatform.streamregistry.AvroStreamKey;
import com.homeaway.digitalplatform.streamregistry.Consumer;
import com.homeaway.digitalplatform.streamregistry.OperationType;
import com.homeaway.digitalplatform.streamregistry.Producer;
import com.homeaway.digitalplatform.streamregistry.RegionReplicator;
import com.homeaway.digitalplatform.streamregistry.RegionStreamConfiguration;
import com.homeaway.digitalplatform.streamregistry.Schema;
import com.homeaway.digitalplatform.streamregistry.Tags;

@Slf4j
public class AvroModelBuilder {

    public AvroModelBuilder() {
    }

    public AbstractMap.SimpleEntry<AvroStreamKey,AvroStream> buildSampleMessage(String streamName, OperationType operationType){
        AvroStreamKey avroStreamKey = AvroStreamKey.newBuilder().setStreamName(streamName).build();
        AvroStream avroStream = this.buildAvroStream(streamName, operationType);

        return new AbstractMap.SimpleEntry<>(avroStreamKey,avroStream);
    }

    /**
     * Build an Avro stream object given a stream name and an operation type.
     * @param streamName
     * @param operationType
     * @return
     */
    public AvroStream buildAvroStream(String streamName, OperationType operationType){

        List<RegionStreamConfiguration> regionKafkaStreamConfigurations = new ArrayList<>();
        RegionStreamConfiguration regionKafkaStreamConfiguration = RegionStreamConfiguration.newBuilder()
            .setRegion(US_EAST_REGION)
            .setCluster(US_EAST_CLUSTER_GENERAL)
            .setTopics(Collections.singletonList(streamName))
            .build();
        regionKafkaStreamConfigurations.add(regionKafkaStreamConfiguration);

        Actor producerActor = Actor.newBuilder()
            .setName("digitalplatform")
            .setRegionStreamConfigurations(regionKafkaStreamConfigurations)
            .build();


        Producer producer1 = Producer.newBuilder()
            .setActor(producerActor)
            .build();

        /** get consumers ready */

        Actor consumerActor = Actor.newBuilder()
            .setName("consumer")
            .setRegionStreamConfigurations(regionKafkaStreamConfigurations)
            .build();

        Consumer consumer1 = Consumer.newBuilder()
            .setActor(consumerActor)
            .build();

        /** get mirrormaker status */
        RegionReplicator regionReplicator = RegionReplicator.newBuilder()
            .setStatus("NOT_DEPLOYED") // this is not required
            .setAppName("")
            .setProperties(new HashMap<>())
            .build();

        Schema keySchema = Schema.newBuilder()
                .setId("1759")
                .setSubjectId(1)
                .setSchemaString(AvroStreamKey.SCHEMA$.toString())
                .build();

        Schema valueSchema = Schema.newBuilder()
                .setId("1758")
                .setSubjectId(1)
                .setSchemaString(AvroStream.SCHEMA$.toString())
                .build();

        Tags tags = Tags.newBuilder().setProductId(1234).setPortfolioId(1343).build();

        /**
         * build the stream object
         * */
        log.trace("Schema for the Avro Stream:" + AvroStream.SCHEMA$);

        return AvroStream.newBuilder()
            .setName(streamName)
            .setOperationType(operationType)
            .setProducers(Collections.singletonList(producer1))
            .setConsumers(Collections.singletonList(consumer1))
            .setRegionReplicatorList(Collections.singletonList(regionReplicator))
            .setCreated(System.currentTimeMillis())
            .setUpdated(System.currentTimeMillis())
            .setLatestKeySchema(keySchema)
            .setLatestValueSchema(valueSchema)
            .setOwner("user-1")
            .setTags(tags)
            .setIsDataNeededAtRest(false)
            .setIsAutomationNeeded(false)
            .setVpcList(Collections.singletonList(US_EAST_REGION))
            .build();
    }
}
