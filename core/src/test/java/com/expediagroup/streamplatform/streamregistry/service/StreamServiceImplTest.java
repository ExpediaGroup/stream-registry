/* Copyright (C) 2018-2019 Expedia, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.expediagroup.streamplatform.streamregistry.service;

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

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.expediagroup.streamplatform.streamregistry.AvroStream;
import com.expediagroup.streamplatform.streamregistry.AvroStreamKey;
import com.expediagroup.streamplatform.streamregistry.ClusterValue;
import com.expediagroup.streamplatform.streamregistry.OperationType;
import com.expediagroup.streamplatform.streamregistry.Schema;
import com.expediagroup.streamplatform.streamregistry.Tags;
import com.expediagroup.streamplatform.streamregistry.configuration.KafkaProducerConfig;
import com.expediagroup.streamplatform.streamregistry.db.dao.StreamDao;
import com.expediagroup.streamplatform.streamregistry.exceptions.*;
import com.expediagroup.streamplatform.streamregistry.exceptions.ClusterNotFoundException;
import com.expediagroup.streamplatform.streamregistry.exceptions.InvalidStreamException;
import com.expediagroup.streamplatform.streamregistry.exceptions.SchemaManagerException;
import com.expediagroup.streamplatform.streamregistry.exceptions.StreamCreationException;
import com.expediagroup.streamplatform.streamregistry.extensions.schema.SchemaManager;
import com.expediagroup.streamplatform.streamregistry.extensions.schema.SchemaReference;
import com.expediagroup.streamplatform.streamregistry.extensions.validation.StreamValidator;
import com.expediagroup.streamplatform.streamregistry.model.Stream;
import com.expediagroup.streamplatform.streamregistry.provider.InfraManager;
import com.expediagroup.streamplatform.streamregistry.service.impl.StreamServiceImpl;

public class StreamServiceImplTest {

    private static final String TEST_ENV = "test";

    private static final String TEST_STREAM_KEY = "test_stream";

    private final StreamDao streamDao = mock(StreamDao.class);
    private final RegionService regionService = mock(RegionService.class);
    private final InfraManager infraManager = mock(InfraManager.class);
    private final ClusterService clusterService = mock(ClusterService.class);
    private final StreamValidator streamValidator = mock(StreamValidator.class);
    private final SchemaManager schemaManager = mock(SchemaManager.class);

    private StreamService streamService;

    @Before
    public void setup() {
        streamService = new StreamServiceImpl(streamDao, TEST_ENV, regionService, clusterService, infraManager, streamValidator, schemaManager);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUpsertStreamChangePartitionCountFails() throws InvalidStreamException, SchemaManagerException, StreamCreationException, ClusterNotFoundException {
        when(streamDao.getStream(TEST_STREAM_KEY)).thenReturn(buildTestAvroStream());

        Stream newStream = buildTestStream();
        newStream.setPartitions(newStream.getPartitions() + 1);
        when(streamValidator.isStreamValid(newStream)).thenReturn(true);
        when(schemaManager.checkCompatibility(anyString(), anyString())).thenReturn(true);
        when(schemaManager.registerSchema(anyString(), anyString())).thenReturn(new SchemaReference("subject", 0, 0));

        streamService.upsertStream(newStream);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUpsertStreamChangeReplicationFactorFails() throws InvalidStreamException, SchemaManagerException, StreamCreationException, ClusterNotFoundException {
        when(streamDao.getStream(TEST_STREAM_KEY)).thenReturn(buildTestAvroStream());

        Stream newStream = buildTestStream();
        newStream.setReplicationFactor(newStream.getReplicationFactor() + 1);
        when(streamValidator.isStreamValid(newStream)).thenReturn(true);
        when(schemaManager.registerSchema(anyString(), anyString())).thenReturn(new SchemaReference("subject", 0, 0));

        streamService.upsertStream(newStream);
    }

    @Test
    public void testUpsertStreamPushesUpdatedSchemaMetadata() throws SchemaManagerException, InvalidStreamException, StreamCreationException, ClusterNotFoundException {
        when(streamDao.getStream(TEST_STREAM_KEY)).thenReturn(buildTestAvroStream());

        Stream newStream = buildTestStream();
        SchemaReference newKeySchemaReference = new SchemaReference(TEST_STREAM_KEY + "-key", 2, 3);
        when(schemaManager.registerSchema(newStream.getLatestKeySchema().getId(), newStream.getLatestKeySchema().getSchemaString()))
                .thenReturn(newKeySchemaReference);

        SchemaReference newValueSchemaReference = new SchemaReference(TEST_STREAM_KEY + "-value", 4, 5);
        when(schemaManager.registerSchema(newStream.getLatestValueSchema().getId(), newStream.getLatestValueSchema().getSchemaString()))
                .thenReturn(newValueSchemaReference);

        when(streamValidator.isStreamValid(newStream)).thenReturn(true);
        Map<String, String> clusterProperties = new HashMap<>();
        clusterProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        clusterProperties.put(KafkaProducerConfig.ZOOKEEPER_QUORUM, "localhost:2181");
        when(clusterService.getCluster(anyString(), anyString(), anyString(), anyString())).thenReturn(new ClusterValue(clusterProperties));

        streamService.upsertStream(newStream);

        ArgumentCaptor<AvroStream> avroStreamArgumentCaptor = ArgumentCaptor.forClass(AvroStream.class);
        verify(streamDao).upsertStream(any(AvroStreamKey.class), avroStreamArgumentCaptor.capture());

        assertEquals("2", avroStreamArgumentCaptor.getValue().getLatestKeySchema().getId());
        assertEquals((long) 3, (long) avroStreamArgumentCaptor.getValue().getLatestKeySchema().getSubjectId());
        assertEquals("4", avroStreamArgumentCaptor.getValue().getLatestValueSchema().getId());
        assertEquals((long) 5, (long) avroStreamArgumentCaptor.getValue().getLatestValueSchema().getSubjectId());
    }

    @Test(expected = SchemaManagerException.class)
    public void testIncompatibleSchemaFails() throws SchemaManagerException, InvalidStreamException, StreamCreationException, ClusterNotFoundException {
        when(streamDao.getStream(TEST_STREAM_KEY)).thenReturn(buildTestAvroStream());

        Stream newStream = buildTestStream();
        SchemaReference newKeySchemaReference = new SchemaReference(TEST_STREAM_KEY + "-key", 2, 3);
        when(schemaManager.registerSchema(newStream.getLatestKeySchema().getId(), newStream.getLatestKeySchema().getSchemaString()))
                .thenReturn(newKeySchemaReference);

        when(schemaManager.registerSchema(newStream.getLatestValueSchema().getId(), newStream.getLatestValueSchema().getSchemaString()))
                .thenThrow(new SchemaManagerException("error"));

        when(streamValidator.isStreamValid(newStream)).thenReturn(true);
        streamService.upsertStream(newStream);
    }

    private ImmutablePair<AvroStreamKey, Optional<AvroStream>> buildTestAvroStream() {
        AvroStreamKey key = AvroStreamKey.newBuilder().setStreamName(TEST_STREAM_KEY).build();
        AvroStream value = AvroStream.newBuilder()
                                        .setName(TEST_STREAM_KEY)
                                        .setOwner("owner")
                                        .setTags(new Tags(1, 2, "ExpediaGroup", "99", "test-component-id", "primary"))
                                        .setPartitions(1)
                                        .setReplicationFactor(1)
                                        .setOperationType(OperationType.UPSERT)
                                        .setLatestKeySchema(new Schema(TEST_STREAM_KEY + "-key", 1, "keySchemaString", null, null))
                                        .setLatestValueSchema(new Schema(TEST_STREAM_KEY + "-value", 1, "valueSchemaString", null, null))
                                        .setVpcList(Collections.singletonList("us-aus-1-dts"))
                                        .build();
        return new ImmutablePair<>(key, Optional.ofNullable(value));
    }

    private Stream buildTestStream() {
        if (!buildTestAvroStream().getValue().isPresent()) {
            return Stream.builder().build();
        } else {
            AvroStream avroStream = buildTestAvroStream().getValue().get();
            com.expediagroup.streamplatform.streamregistry.model.Tags tags = com.expediagroup.streamplatform.streamregistry.model.Tags.builder()
                .componentId(avroStream.getTags().getComponentId())
                .productId(avroStream.getTags().getProductId())
                .hint(avroStream.getTags().getHint())
                .assetProtectionLevel(avroStream.getTags().getAssetProtectionLevel())
                .brand(avroStream.getTags().getBrand())
                .portfolioId(avroStream.getTags().getPortfolioId())
                .build();
            com.expediagroup.streamplatform.streamregistry.model.Schema latestKeySchema = com.expediagroup.streamplatform.streamregistry.model.Schema.builder()
                .id(avroStream.getLatestKeySchema().getId())
                .schemaString(avroStream.getLatestKeySchema().getSchemaString())
                .version(1)
                .created(avroStream.getLatestKeySchema().getCreated())
                .updated(avroStream.getLatestKeySchema().getUpdated())
                .build();
            com.expediagroup.streamplatform.streamregistry.model.Schema latestValueSchema = com.expediagroup.streamplatform.streamregistry.model.Schema.builder()
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
}
