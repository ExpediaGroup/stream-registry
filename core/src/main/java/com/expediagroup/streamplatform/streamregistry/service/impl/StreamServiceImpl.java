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
package com.expediagroup.streamplatform.streamregistry.service.impl;

import java.util.*;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.clients.producer.ProducerConfig;

import com.expediagroup.streamplatform.streamregistry.AvroStream;
import com.expediagroup.streamplatform.streamregistry.AvroStreamKey;
import com.expediagroup.streamplatform.streamregistry.ClusterValue;
import com.expediagroup.streamplatform.streamregistry.OperationType;
import com.expediagroup.streamplatform.streamregistry.configuration.KafkaProducerConfig;
import com.expediagroup.streamplatform.streamregistry.db.dao.StreamDao;
import com.expediagroup.streamplatform.streamregistry.dto.AvroToJsonDTO;
import com.expediagroup.streamplatform.streamregistry.dto.JsonToAvroDTO;
import com.expediagroup.streamplatform.streamregistry.exceptions.ClusterNotFoundException;
import com.expediagroup.streamplatform.streamregistry.exceptions.InvalidStreamException;
import com.expediagroup.streamplatform.streamregistry.exceptions.SchemaManagerException;
import com.expediagroup.streamplatform.streamregistry.exceptions.StreamCreationException;
import com.expediagroup.streamplatform.streamregistry.exceptions.StreamNotFoundException;
import com.expediagroup.streamplatform.streamregistry.extensions.schema.SchemaManager;
import com.expediagroup.streamplatform.streamregistry.extensions.schema.SchemaReference;
import com.expediagroup.streamplatform.streamregistry.extensions.validation.StreamValidator;
import com.expediagroup.streamplatform.streamregistry.model.Stream;
import com.expediagroup.streamplatform.streamregistry.model.Tags;
import com.expediagroup.streamplatform.streamregistry.provider.InfraManager;
import com.expediagroup.streamplatform.streamregistry.service.AbstractService;
import com.expediagroup.streamplatform.streamregistry.service.ClusterService;
import com.expediagroup.streamplatform.streamregistry.service.RegionService;
import com.expediagroup.streamplatform.streamregistry.service.StreamService;


@Slf4j
public class StreamServiceImpl extends AbstractService implements StreamService {

    private final StreamValidator streamValidator;
    private final SchemaManager schemaManager;
    private final StreamDao streamDao;

    public StreamServiceImpl(StreamDao streamDao,
                             String env,
                             RegionService regionService,
                             ClusterService clusterService,
                             InfraManager infraManager,
                             StreamValidator validator,
                             SchemaManager schemaManager) {
        super(env, regionService, clusterService, infraManager);
        this.streamDao = streamDao;
        this.streamValidator = validator;
        this.schemaManager = schemaManager;
    }

    @Override
    public void upsertStream(Stream stream) throws SchemaManagerException, InvalidStreamException, StreamCreationException, ClusterNotFoundException {
        applyDefaultHint(stream);
        applyDefaultPartition(stream);
        applyDefaultReplicationFactor(stream);

        if (!streamValidator.isStreamValid(stream)) {
            throw new InvalidStreamException(String.format("Stream is not valid - failed validation test in %s", streamValidator.getClass().toString()));
        }

        // TODO: modify to support multiple schema 'types' per stream (#55)
        // register schemas
        String keySubject = stream.getName() + "-key";
        SchemaReference keyReference = schemaManager.registerSchema(keySubject, stream.getLatestKeySchema().getSchemaString());
        stream.getLatestKeySchema().setId(String.valueOf(keyReference.getId()));
        stream.getLatestKeySchema().setVersion(keyReference.getVersion());
        boolean isNewStream = false;

        String valueSubject = stream.getName() + "-value";
        SchemaReference valueReference = schemaManager.registerSchema(valueSubject, stream.getLatestValueSchema().getSchemaString());
        stream.getLatestValueSchema().setId(String.valueOf(valueReference.getId()));
        stream.getLatestValueSchema().setVersion(valueReference.getVersion());

        Pair<AvroStreamKey, Optional<AvroStream>> keyValue = streamDao.getStream(stream.getName());
        AvroStreamKey key;
        AvroStream avroStream =
                JsonToAvroDTO.convertJsonToAvro(stream, OperationType.UPSERT);

        Optional<AvroStream> value = keyValue.getValue();
        if (value.isPresent()) {
            // ensure valid partition counts and replication factors are provided
            if (stream.getPartitions() != value.get().getPartitions()) {
                throw new UnsupportedOperationException(String.format(
                        "Stream registry does not currently support modifying the initial partition count. " +
                                "Requested: %s. Current count: %s", stream.getPartitions(),
                        value.get().getPartitions()));
            } else if (stream.getReplicationFactor() != value.get().getReplicationFactor()) {
                throw new UnsupportedOperationException(String.format(
                        "Stream registry does not currently support modifying the initial replication factor. " +
                                "Requested: %s. Current replication factor: %s", stream.getReplicationFactor(),
                        value.get().getReplicationFactor()));
            }
            key = keyValue.getKey();
            log.info("key={} available for the stream-name={}", key, stream.getName());
            // Copy the earlier stream properties
            avroStream.setCreated(value.get().getCreated());
            avroStream.setProducers(value.get().getProducers());
            avroStream.setConsumers(value.get().getConsumers());
            avroStream.setRegionReplicatorList(value.get().getRegionReplicatorList());
            avroStream.setS3ConnectorList(value.get().getS3ConnectorList());
        } else {
            log.info("key NOT available for the stream-name={}", stream.getName());
            isNewStream = true;
            avroStream.setCreated(System.currentTimeMillis());
            key = AvroStreamKey.newBuilder().setStreamName(avroStream.getName()).build();
        }

        verifyAndUpsertTopics(stream, isNewStream);
        streamDao.upsertStream(key, avroStream);
    }

    /**
     * Setting default replication factor in case if its set to 0 or a negative number
     *
     * @param stream the stream that will be given a default replication factor
     */
    private void applyDefaultReplicationFactor(Stream stream) {
        if (stream.getReplicationFactor() <= 0) {
            stream.setReplicationFactor(3);
        }
    }

    /**
     * Setting default partition in case if its set to 0 or a negative number
     *
     * @param stream the stream that will be given a default number of partitions
     */
    private void applyDefaultPartition(Stream stream) {
        if (stream.getPartitions() <= 0) {
            stream.setPartitions(1);
        }
    }

    /**
     * Verify if the Topic exists If not, create Topics in the Cluster
     *
     * @param stream the stream that will be used to verify and/or upsert topics to
     */
    private void verifyAndUpsertTopics(Stream stream, boolean isNewStream) throws StreamCreationException, ClusterNotFoundException {
        List<String> vpcList = stream.getVpcList();
        List<String> replicatedVpcList = stream.getReplicatedVpcList();
        log.info("creating topics for vpcList: {}", vpcList);
        for (String vpc : vpcList) {
            upsertTopics(stream, vpc, isNewStream);
        }
        if (replicatedVpcList != null && !replicatedVpcList.isEmpty()) {
            log.info("creating topics for replicatedVpcList: {}", replicatedVpcList);
            for (String vpc : replicatedVpcList) {
                upsertTopics(stream, vpc, isNewStream);
            }
        }
    }

    private void upsertTopics(Stream stream, String vpc, boolean isNewStream) throws StreamCreationException, ClusterNotFoundException {
        ClusterValue clusterValue = clusterService.getCluster(vpc, env, stream.getTags().getHint(), "producer");
        Properties topicConfig = new Properties();
        if (stream.getTopicConfig() != null) {
            topicConfig.putAll(stream.getTopicConfig());
        }

        String bootstrapServer = clusterValue.getClusterProperties().get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG);
        String zkConnect = clusterValue.getClusterProperties().get(KafkaProducerConfig.ZOOKEEPER_QUORUM);
        topicConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        topicConfig.put(KafkaProducerConfig.ZOOKEEPER_QUORUM, zkConnect);

        infraManager.upsertTopics(Collections.singleton(stream.getName()), stream.getPartitions(), stream.getReplicationFactor(), topicConfig, isNewStream);
        log.info("Topic {} created/updated at {}", stream.getName(), bootstrapServer);
    }

    private void applyDefaultHint(Stream stream) {
        Tags tags = stream.getTags();

        String hint = tags.getHint();

        if (hint == null || hint.trim().matches("(?i:string)?")) {
            hint = AbstractService.PRIMARY_HINT;
        }

        tags.setHint(hint);
    }

    @Override
    public Stream getStream(String streamName) throws StreamNotFoundException {
        log.info("Pulling stream information from global state-store for streamName={}", streamName);
        Optional<AvroStream> streamValue = streamDao.getStream(streamName).getValue();
        if (!streamValue.isPresent()) {
            throw new StreamNotFoundException(String.format("Stream=%s not found.", streamName));
        }

        return AvroToJsonDTO.convertAvroToJson(streamValue.get());
    }

    @Override
    public void deleteStream(String streamName) throws StreamNotFoundException{
        Pair<AvroStreamKey, Optional<AvroStream>> keyValue = streamDao.getStream(streamName);
        if (!keyValue.getValue().isPresent()) {
            throw new StreamNotFoundException(String.format("Stream=%s not found.", streamName));
        }

        // TODO: issue#156. Delete the underlying Topic when a Stream is deleted.
        // It is dangerous to delete the topic in this case without enough authorization, monitoring and alerting in place
        streamDao.upsertStream(keyValue.getKey(), null);
    }

    @Override
    public List<Stream> getAllStreams() {
        List<Stream> streamList = new ArrayList<>();
        log.info("Pulling stream information from local instance's state-store");
        streamDao.getAllStreams().forEachRemaining(avroStream -> streamList.add(AvroToJsonDTO.convertAvroToJson(avroStream.value)));
        return streamList;
    }

    @Override
    public boolean validateSchemaCompatibility(Stream stream) {
        String keySubject = stream.getName() + "-key";
        String valueSubject = stream.getName() + "-value";

        String keySchema = stream.getLatestKeySchema().getSchemaString();
        String valueSchema = stream.getLatestValueSchema().getSchemaString();

        return schemaManager.checkCompatibility(keySubject, keySchema) && schemaManager.checkCompatibility(valueSubject, valueSchema);
    }

}
