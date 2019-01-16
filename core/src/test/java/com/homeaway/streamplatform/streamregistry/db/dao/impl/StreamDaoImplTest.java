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
package com.homeaway.streamplatform.streamregistry.db.dao.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.homeaway.digitalplatform.streamregistry.AvroStream;
import com.homeaway.digitalplatform.streamregistry.AvroStreamKey;
import com.homeaway.digitalplatform.streamregistry.ClusterKey;
import com.homeaway.digitalplatform.streamregistry.ClusterValue;
import com.homeaway.digitalplatform.streamregistry.OperationType;
import com.homeaway.digitalplatform.streamregistry.Schema;
import com.homeaway.digitalplatform.streamregistry.Tags;
import com.homeaway.streamplatform.streamregistry.configuration.KafkaProducerConfig;
import com.homeaway.streamplatform.streamregistry.db.dao.KafkaManager;
import com.homeaway.streamplatform.streamregistry.db.dao.RegionDao;
import com.homeaway.streamplatform.streamregistry.db.dao.StreamDao;
import com.homeaway.streamplatform.streamregistry.exceptions.SchemaManagerException;
import com.homeaway.streamplatform.streamregistry.exceptions.StreamCreationException;
import com.homeaway.streamplatform.streamregistry.extensions.schema.SchemaManager;
import com.homeaway.streamplatform.streamregistry.extensions.schema.SchemaReference;
import com.homeaway.streamplatform.streamregistry.extensions.validation.StreamValidator;
import com.homeaway.streamplatform.streamregistry.model.Stream;
import com.homeaway.streamplatform.streamregistry.provider.InfraManager;
import com.homeaway.streamplatform.streamregistry.streams.ManagedKStreams;
import com.homeaway.streamplatform.streamregistry.streams.ManagedKafkaProducer;

public class StreamDaoImplTest {

    private static final String TEST_ENV = "test";

    private static final AvroStreamKey TEST_STREAM_KEY = new AvroStreamKey("test_stream");

    private ManagedKafkaProducer managedKafkaProducer = mock(ManagedKafkaProducer.class);
    private ManagedKStreams managedKStreams = mock(ManagedKStreams.class);
    private RegionDao regionDao = mock(RegionDao.class);
    private InfraManager infraManager = mock(InfraManager.class);
    private KafkaManager kafkaManager = mock(KafkaManager.class);
    private StreamValidator streamValidator = mock(StreamValidator.class);
    private SchemaManager schemaManager = mock(SchemaManager.class);

    private StreamDao streamDao;

    @Before
    public void setup() {
        streamDao = new StreamDaoImpl(managedKafkaProducer, managedKStreams, TEST_ENV, regionDao, infraManager, kafkaManager, streamValidator, schemaManager);
    }

    @Test(expected = StreamCreationException.class)
    public void testUpsertStreamChangePartitionCountFails() {
        AvroStream originalStream = buildTestAvroStream();
        when(managedKStreams.getAvroStreamForKey(TEST_STREAM_KEY)).thenReturn(Optional.of(originalStream));

        Stream newStream = buildTestStream();
        newStream.setPartitions(newStream.getPartitions() + 1);
        when(streamValidator.isStreamValid(newStream)).thenReturn(true);
        when(schemaManager.checkCompatibility(anyString(), anyString())).thenReturn(true);
        when(schemaManager.registerSchema(anyString(), anyString())).thenReturn(new SchemaReference("subject", 0, 0));

        streamDao.upsertStream(newStream);
    }

    @Test(expected = StreamCreationException.class)
    public void testUpsertStreamChangeReplicationFactorFails() {
        AvroStream originalStream = buildTestAvroStream();
        when(managedKStreams.getAvroStreamForKey(TEST_STREAM_KEY)).thenReturn(Optional.of(originalStream));

        Stream newStream = buildTestStream();
        newStream.setReplicationFactor(newStream.getReplicationFactor() + 1);
        when(streamValidator.isStreamValid(newStream)).thenReturn(true);
        when(schemaManager.registerSchema(anyString(), anyString())).thenReturn(new SchemaReference("subject", 0, 0));

        streamDao.upsertStream(newStream);
    }

    @Test
    public void testUpsertStreamPushesUpdatedSchemaMetadata() {
        AvroStream originalStream = buildTestAvroStream();
        when(managedKStreams.getAvroStreamForKey(TEST_STREAM_KEY)).thenReturn(Optional.of(originalStream));

        Stream newStream = buildTestStream();
        SchemaReference newKeySchemaReference = new SchemaReference(TEST_STREAM_KEY.getStreamName() + "-key", 2, 3);
        when(schemaManager.registerSchema(newStream.getLatestKeySchema().getId(), newStream.getLatestKeySchema().getSchemaString()))
                .thenReturn(newKeySchemaReference);

        SchemaReference newValueSchemaReference = new SchemaReference(TEST_STREAM_KEY.getStreamName() + "-value", 4, 5);
        when(schemaManager.registerSchema(newStream.getLatestValueSchema().getId(), newStream.getLatestValueSchema().getSchemaString()))
                .thenReturn(newValueSchemaReference);

        when(streamValidator.isStreamValid(newStream)).thenReturn(true);
        Map<String, String> clusterProperties = new HashMap<>();
        clusterProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        clusterProperties.put(KafkaProducerConfig.ZOOKEEPER_QUORUM, "localhost:2181");
        when(infraManager.getClusterByKey(any(ClusterKey.class))).thenReturn(Optional.of(new ClusterValue(clusterProperties)));

        streamDao.upsertStream(newStream);

        ArgumentCaptor<AvroStream> avroStreamArgumentCaptor = ArgumentCaptor.forClass(AvroStream.class);
        verify(managedKafkaProducer).log(any(AvroStreamKey.class), avroStreamArgumentCaptor.capture());

        assertEquals("2", avroStreamArgumentCaptor.getValue().getLatestKeySchema().getId());
        assertEquals((long) 3, (long) avroStreamArgumentCaptor.getValue().getLatestKeySchema().getSubjectId());
        assertEquals("4", avroStreamArgumentCaptor.getValue().getLatestValueSchema().getId());
        assertEquals((long) 5, (long) avroStreamArgumentCaptor.getValue().getLatestValueSchema().getSubjectId());
    }

    @Test(expected = StreamCreationException.class)
    public void testIncompatibleSchemaFails() {
        AvroStream originalStream = buildTestAvroStream();
        when(managedKStreams.getAvroStreamForKey(TEST_STREAM_KEY)).thenReturn(Optional.of(originalStream));

        Stream newStream = buildTestStream();
        SchemaReference newKeySchemaReference = new SchemaReference(TEST_STREAM_KEY.getStreamName() + "-key", 2, 3);
        when(schemaManager.registerSchema(newStream.getLatestKeySchema().getId(), newStream.getLatestKeySchema().getSchemaString()))
                .thenReturn(newKeySchemaReference);

        when(schemaManager.registerSchema(newStream.getLatestValueSchema().getId(), newStream.getLatestValueSchema().getSchemaString()))
                .thenThrow(new SchemaManagerException("error"));

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
                .setLatestKeySchema(new Schema(TEST_STREAM_KEY.getStreamName() + "-key", 1, "keySchemaString", null, null))
                .setLatestValueSchema(new Schema(TEST_STREAM_KEY.getStreamName() + "-value", 1, "valueSchemaString", null, null))
                .setVpcList(Collections.singletonList("us-aus-1-dts"))
                .build();
    }

    private Stream buildTestStream() {
        AvroStream avroStream = buildTestAvroStream();
        com.homeaway.streamplatform.streamregistry.model.Tags tags = com.homeaway.streamplatform.streamregistry.model.Tags.builder()
                .componentId(avroStream.getTags().getComponentId())
                .productId(avroStream.getTags().getProductId())
                .hint(avroStream.getTags().getHint())
                .assetProtectionLevel(avroStream.getTags().getAssetProtectionLevel())
                .brand(avroStream.getTags().getBrand())
                .portfolioId(avroStream.getTags().getPortfolioId())
                .build();
        com.homeaway.streamplatform.streamregistry.model.Schema latestKeySchema = com.homeaway.streamplatform.streamregistry.model.Schema.builder()
                .id(avroStream.getLatestKeySchema().getId())
                .schemaString(avroStream.getLatestKeySchema().getSchemaString())
                .version(1)
                .created(avroStream.getLatestKeySchema().getCreated())
                .updated(avroStream.getLatestKeySchema().getUpdated())
                .build();
        com.homeaway.streamplatform.streamregistry.model.Schema latestValueSchema = com.homeaway.streamplatform.streamregistry.model.Schema.builder()
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
