/**
 * Copyright (C) 2018-2019 Expedia, Inc.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import lombok.extern.slf4j.Slf4j;

import org.apache.avro.Schema;

import com.expediagroup.streamplatform.streamregistry.core.exception.ClusterNotFoundException;
import com.expediagroup.streamplatform.streamregistry.core.exception.InvalidStreamException;
import com.expediagroup.streamplatform.streamregistry.core.exception.SchemaManagerException;
import com.expediagroup.streamplatform.streamregistry.core.exception.StreamCreationException;
import com.expediagroup.streamplatform.streamregistry.core.exception.StreamNotFoundException;
import com.expediagroup.streamplatform.streamregistry.core.extension.validation.StreamValidator;
import com.expediagroup.streamplatform.streamregistry.core.schema.SchemaManager;
import com.expediagroup.streamplatform.streamregistry.core.schema.SchemaReference;
import com.expediagroup.streamplatform.streamregistry.model.Cluster;
import com.expediagroup.streamplatform.streamregistry.model.Stream;
import com.expediagroup.streamplatform.streamregistry.model.Tags;
import com.expediagroup.streamplatform.streamregistry.service.ClusterService;
import com.expediagroup.streamplatform.streamregistry.service.RegionService;
import com.expediagroup.streamplatform.streamregistry.service.StreamService;
import com.expediagroup.streamplatform.streamregistry.repository.InfraManager;
import com.expediagroup.streamplatform.streamregistry.repository.StreamDao;


@Slf4j
public class StreamServiceImpl extends AbstractService implements StreamService {

  private static final String ZOOKEEPER_QUORUM = "zookeeper_quorum";

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
    Schema keySchema = new Schema.Parser().parse(stream.getLatestKeySchema().getSchemaString());
    SchemaReference keyReference = schemaManager.registerSchema(keySubject, keySchema);
    stream.getLatestKeySchema().setId(String.valueOf(keyReference.getId()));
    stream.getLatestKeySchema().setVersion(keyReference.getVersion());
    boolean isNewStream = false;

    String valueSubject = stream.getName() + "-value";
    Schema valueSchema = new Schema.Parser().parse(stream.getLatestValueSchema().getSchemaString());
    SchemaReference valueReference = schemaManager.registerSchema(valueSubject, valueSchema);
    stream.getLatestValueSchema().setId(String.valueOf(valueReference.getId()));
    stream.getLatestValueSchema().setVersion(valueReference.getVersion());

    Optional<Stream> existingStream = streamDao.get(stream.getName());

    if (existingStream.isPresent()) {
      // ensure valid partition counts and replication factors are provided
      if (stream.getPartitions() != existingStream.get().getPartitions()) {
        throw new UnsupportedOperationException(String.format(
            "Stream registry does not currently support modifying the initial partition count. " +
                "Requested: %s. Current count: %s", stream.getPartitions(),
            existingStream.get().getPartitions()));
      } else if (stream.getReplicationFactor() != existingStream.get().getReplicationFactor()) {
        throw new UnsupportedOperationException(String.format(
            "Stream registry does not currently support modifying the initial replication factor. " +
                "Requested: %s. Current replication factor: %s", stream.getReplicationFactor(),
            existingStream.get().getReplicationFactor()));
      }
    } else {
      isNewStream = true;
    }

    verifyAndUpsertTopics(stream, isNewStream);
    streamDao.upsert(stream);
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
    Cluster cluster = clusterService.getCluster(vpc, env, stream.getTags().getHint(), "producer");
    Properties topicConfig = new Properties();
    if (stream.getTopicConfig() != null) {
      topicConfig.putAll(stream.getTopicConfig());
    }

    String bootstrapServer = cluster.getClusterValue().getBootstrapServers();
    String zkConnect = cluster.getClusterValue().getZookeeperQuorum();
    topicConfig.put(BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
    //TODO find out why we need the zkConnect
    topicConfig.put(ZOOKEEPER_QUORUM, zkConnect);

    infraManager.upsertTopics(Collections.singleton(stream.getName()), stream.getPartitions(), stream.getReplicationFactor(), topicConfig, isNewStream);
    log.info("Topic {} created/updated at {}", stream.getName(), bootstrapServer);
  }

  private void applyDefaultHint(Stream stream) {
    Tags tags = stream.getTags();

    String hint = tags.getHint();

    if (hint == null || hint.trim().matches("(?i:string)?")) {
      hint = PRIMARY_HINT;
    }

    tags.setHint(hint);
  }

  @Override
  public Stream getStream(String streamName) throws StreamNotFoundException {
    log.info("Pulling stream information from global state-store for streamName={}", streamName);
    Optional<Stream> streamValue = streamDao.get(streamName);
    if (!streamValue.isPresent()) {
      throw new StreamNotFoundException(String.format("Stream=%s not found.", streamName));
    }
    return streamValue.get();
  }

  @Override
  public void deleteStream(String streamName) throws StreamNotFoundException {
    if (!streamDao.get(streamName).isPresent()) {
      throw new StreamNotFoundException(String.format("Stream=%s not found.", streamName));
    }

    // TODO: issue#156. Delete the underlying Topic when a Stream is deleted.
    // It is dangerous to delete the topic in this case without enough authorization, monitoring and alerting in place
    // should logically delete the stream in this case to continue to track it until the infrastructure is finally deleted.
    streamDao.delete(streamName);
  }

  @Override
  public List<Stream> getAllStreams() {
    List<Stream> streamList = new ArrayList<>();
    log.info("Pulling stream information from local instance's state-store");
    streamDao.getAll().forEach(streamList::add);
    return streamList;
  }

  @Override
  public boolean validateSchemaCompatibility(Stream stream) {
    String keySubject = stream.getName() + "-key";
    String valueSubject = stream.getName() + "-value";

    Schema keySchema = new Schema.Parser().parse(stream.getLatestKeySchema().getSchemaString());
    Schema valueSchema = new Schema.Parser().parse(stream.getLatestValueSchema().getSchemaString());

    return schemaManager.checkCompatibility(keySubject, keySchema) && schemaManager.checkCompatibility(valueSubject, valueSchema);
  }

}
