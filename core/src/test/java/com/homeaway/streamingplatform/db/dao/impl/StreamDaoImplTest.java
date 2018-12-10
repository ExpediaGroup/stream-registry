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
package com.homeaway.streamingplatform.db.dao.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import com.homeaway.digitalplatform.streamregistry.*;
import com.homeaway.streamingplatform.db.dao.KafkaManager;
import com.homeaway.streamingplatform.db.dao.RegionDao;
import com.homeaway.streamingplatform.db.dao.StreamDao;
import com.homeaway.streamingplatform.extensions.validation.SchemaRegistrar;
import com.homeaway.streamingplatform.extensions.validation.StreamValidator;
import com.homeaway.streamingplatform.model.Stream;
import com.homeaway.streamingplatform.provider.InfraManager;
import com.homeaway.streamingplatform.streams.ManagedKStreams;
import com.homeaway.streamingplatform.streams.ManagedKafkaProducer;

public class StreamDaoImplTest {

    private static final String TEST_ENV = "test";

    private static final AvroStreamKey TEST_STREAM_KEY = new AvroStreamKey("test_stream");

    private ManagedKafkaProducer managedKafkaProducer = mock(ManagedKafkaProducer.class);
    private ManagedKStreams managedKStreams = mock(ManagedKStreams.class);
    private RegionDao regionDao = mock(RegionDao.class);
    private InfraManager infraManager = mock(InfraManager.class);
    private KafkaManager kafkaManager = mock(KafkaManager.class);
    private StreamValidator streamValidator = mock(StreamValidator.class);
    private SchemaRegistrar schemaRegistrar = mock(SchemaRegistrar.class);

    private StreamDao streamDao;

    @Before
    public void setup() {
        streamDao = new StreamDaoImpl(managedKafkaProducer, managedKStreams, TEST_ENV, regionDao, infraManager, kafkaManager, streamValidator, schemaRegistrar);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpsertStreamChangePartitionCountFails() {
        AvroStream originalStream = buildTestAvroStream();
        when(managedKStreams.getAvroStreamForKey(TEST_STREAM_KEY)).thenReturn(Optional.of(originalStream));

        Stream newStream = buildTestStream();
        newStream.setPartitions(newStream.getPartitions() + 1);
        when(streamValidator.isStreamValid(newStream)).thenReturn(true);

        streamDao.upsertStream(newStream);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpsertStreamChangeReplicationFactorFails() {
        AvroStream originalStream = buildTestAvroStream();
        when(managedKStreams.getAvroStreamForKey(TEST_STREAM_KEY)).thenReturn(Optional.of(originalStream));

        Stream newStream = buildTestStream();
        newStream.setReplicationFactor(newStream.getReplicationFactor() + 1);
        when(streamValidator.isStreamValid(newStream)).thenReturn(true);

        streamDao.upsertStream(newStream);
    }

    private AvroStream buildTestAvroStream() {
        return AvroStream.newBuilder()
                .setName(TEST_STREAM_KEY.getStreamName())
                .setOwner("owner")
                .setTags(new Tags(1, 2, "HomeAway", "99", "test-component-id", "primary"))
                .setPartitions(1)
                .setReplicationFactor(1)
                .setOperationType(OperationType.UPSERT)
                .setLatestKeySchema(new Schema("1", 1, "", null, null))
                .setLatestValueSchema(new Schema("2", 1, "", null, null))
                .setVpcList(Collections.singletonList("us-aus-1-dts"))
                .build();
    }

    private Stream buildTestStream() {
        AvroStream avroStream = buildTestAvroStream();
        com.homeaway.streamingplatform.model.Tags tags = com.homeaway.streamingplatform.model.Tags.builder()
                .componentId(avroStream.getTags().getComponentId())
                .productId(avroStream.getTags().getProductId())
                .hint(avroStream.getTags().getHint())
                .assetProtectionLevel(avroStream.getTags().getAssetProtectionLevel())
                .brand(avroStream.getTags().getBrand())
                .portfolioId(avroStream.getTags().getPortfolioId())
                .build();
        com.homeaway.streamingplatform.model.Schema latestKeySchema = com.homeaway.streamingplatform.model.Schema.builder()
                .id(avroStream.getLatestKeySchema().getId())
                .schemaString(avroStream.getLatestKeySchema().getSchemaString())
                .version(1)
                .created(avroStream.getLatestKeySchema().getCreated())
                .updated(avroStream.getLatestKeySchema().getUpdated())
                .build();
        com.homeaway.streamingplatform.model.Schema latestValueSchema = com.homeaway.streamingplatform.model.Schema.builder()
                .id(avroStream.getLatestValueSchema().getId())
                .schemaString(avroStream.getLatestValueSchema().getSchemaString())
                .version(1)
                .created(avroStream.getLatestValueSchema().getCreated())
                .updated(avroStream.getLatestValueSchema().getUpdated())
                .build();
        return Stream.builder()
                .name(avroStream.getName())
                .owner(avroStream.getOwner())
                .tags(tags)
                .partitions(avroStream.getPartitions())
                .replicationFactor(avroStream.getReplicationFactor())
                .vpcList(avroStream.getVpcList())
                .latestKeySchema(latestKeySchema)
                .latestValueSchema(latestValueSchema)
                .build();
    }
}
