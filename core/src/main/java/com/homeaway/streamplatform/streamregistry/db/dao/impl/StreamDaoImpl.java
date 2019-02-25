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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.clients.producer.ProducerConfig;

import com.homeaway.digitalplatform.streamregistry.AvroStream;
import com.homeaway.digitalplatform.streamregistry.AvroStreamKey;
import com.homeaway.digitalplatform.streamregistry.ClusterValue;
import com.homeaway.digitalplatform.streamregistry.OperationType;
import com.homeaway.streamplatform.streamregistry.configuration.KafkaProducerConfig;
import com.homeaway.streamplatform.streamregistry.db.dao.AbstractDao;
import com.homeaway.streamplatform.streamregistry.db.dao.KafkaManager;
import com.homeaway.streamplatform.streamregistry.db.dao.RegionDao;
import com.homeaway.streamplatform.streamregistry.db.dao.StreamDao;
import com.homeaway.streamplatform.streamregistry.dto.AvroToJsonDTO;
import com.homeaway.streamplatform.streamregistry.dto.JsonToAvroDTO;
import com.homeaway.streamplatform.streamregistry.exceptions.*;
import com.homeaway.streamplatform.streamregistry.extensions.schema.SchemaManager;
import com.homeaway.streamplatform.streamregistry.extensions.schema.SchemaReference;
import com.homeaway.streamplatform.streamregistry.extensions.validation.StreamValidator;
import com.homeaway.streamplatform.streamregistry.model.Stream;
import com.homeaway.streamplatform.streamregistry.model.Tags;
import com.homeaway.streamplatform.streamregistry.provider.InfraManager;
import com.homeaway.streamplatform.streamregistry.streams.ManagedKStreams;
import com.homeaway.streamplatform.streamregistry.streams.ManagedKafkaProducer;

@Slf4j
public class StreamDaoImpl extends AbstractDao implements StreamDao {

    private StreamValidator streamValidator;
    private SchemaManager schemaManager;
    private KafkaManager kafkaManager;

    public StreamDaoImpl(ManagedKafkaProducer managedKafkaProducer,
                        ManagedKStreams kStreams,
                        String env,
                        RegionDao regionDao,
                        InfraManager infraManager,
                        StreamValidator validator,
                        SchemaManager schemaManager,
                        KafkaManager kafkaManager) {
        super(managedKafkaProducer, kStreams, env, regionDao, infraManager);
        this.streamValidator = validator;
        this.schemaManager = schemaManager;
        this.kafkaManager = kafkaManager;
    }

    @Override
    public void upsertStream(Stream stream) throws SchemaManagerException, InvalidStreamException, StreamCreationException, ClusterNotFoundException {
        applyDefaultHint(stream);
        applyDefaultPartition(stream);
        applyDefaultReplicationFactor(stream);

        // TODO Can isStreamValid (as currently implemented) ever be false? (#117)
        streamValidator.isStreamValid(stream);

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

        Pair<AvroStreamKey, Optional<AvroStream>> keyValue = getAvroStreamKeyValue(stream.getName());
        AvroStreamKey key;
        AvroStream avroStream =
                JsonToAvroDTO.convertJsonToAvro(stream, OperationType.UPSERT);

        Optional<AvroStream> value = keyValue.getValue();
        if (value.isPresent()) {
            // ensure valid partition counts and replication factors are provided
            if (stream.getPartitions() != value.get().getPartitions()) {
                throw new IllegalArgumentException(String.format(
                        "Stream registry does not currently support modifying the initial partition count. " +
                                "Requested: %s. Current count: %s", stream.getPartitions(),
                        value.get().getPartitions()));
            } else if (stream.getReplicationFactor() != value.get().getReplicationFactor()) {
                throw new IllegalArgumentException(String.format(
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
        kafkaProducer.log(key, avroStream);
        log.info("Stream upserted for {}", stream.getName());
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
        ClusterValue clusterValue = getClusterDetails(vpc, env, stream.getTags().getHint(), "producer");
        Properties topicConfig = new Properties();
        if (stream.getTopicConfig() != null) {
            topicConfig.putAll(stream.getTopicConfig());
        }

        String bootstrapServer = clusterValue.getClusterProperties().get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG);
        String zkConnect = clusterValue.getClusterProperties().get(KafkaProducerConfig.ZOOKEEPER_QUORUM);
        topicConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        topicConfig.put(KafkaProducerConfig.ZOOKEEPER_QUORUM, zkConnect);

        kafkaManager.upsertTopics(Collections.singleton(stream.getName()), stream.getPartitions(), stream.getReplicationFactor(), topicConfig, isNewStream);
        log.info("Topic {} created/updated at {}", stream.getName(), bootstrapServer);
    }

    private void applyDefaultHint(Stream stream) {
        Tags tags = stream.getTags();

        String hint = tags.getHint();

        if (hint == null || hint.trim().matches("(?i:string)?")) {
            hint = AbstractDao.PRIMARY_HINT;
        }

        tags.setHint(hint);
    }

    @Override
    public Stream getStream(String streamName) throws StreamNotFoundException {
        log.info("Pulling stream information from global state-store for streamName={}", streamName);
        Optional<AvroStream> streamValue = kStreams.getAvroStreamForKey(AvroStreamKey.newBuilder().setStreamName(streamName).build());

        if (!streamValue.isPresent()) {
            throw new StreamNotFoundException(streamName, String.format("Stream=%s not found.", streamName));
        } else {
            return AvroToJsonDTO.convertAvroToJson(streamValue.get());
        }
    }

    @Override
    public void deleteStream(String streamName) throws StreamNotFoundException{
        Pair<AvroStreamKey, Optional<AvroStream>> keyValue = getAvroStreamKeyValue(streamName);

        if (!keyValue.getValue().isPresent()) {
            throw new StreamNotFoundException(streamName, String.format("Stream=%s not found.", streamName));
        }
        try {
            kafkaProducer.log(keyValue.getKey(), null);
        } catch (Exception e) {
            log.error("Error deleting stream", e);
        }
    }

    @Override
    public List<Stream> getAllStreams() {
        List<Stream> streamList = new ArrayList<>();
        log.info("Pulling stream information from local instance's state-store");
        kStreams.getAllStreams().forEachRemaining(avroStream -> streamList.add(AvroToJsonDTO.convertAvroToJson(avroStream.value)));
        return streamList;
    }

    @Override
    public boolean validateStreamCompatibility(Stream stream) throws SchemaException {
        String keySubject = stream.getName() + "-key";
        String valueSubject = stream.getName() + "-value";

        String keySchema = stream.getLatestKeySchema().getSchemaString();
        String valueSchema = stream.getLatestValueSchema().getSchemaString();

        return schemaManager.checkCompatibility(keySubject, keySchema)
                && schemaManager.checkCompatibility(valueSubject, valueSchema);
    }

    public void updateAvroStream(AvroStream stream) {
        AvroStreamKey key = AvroStreamKey.newBuilder().setStreamName(stream.getName()).build();
        stream.setOperationType(OperationType.UPSERT);
        try {
            kafkaProducer.log(key, stream);
        } catch (Exception e) {
            log.error("Error logging stream - {}", stream, e);
        }
    }
}
