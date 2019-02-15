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
package com.homeaway.streamplatform.streamregistry.health;

import static com.homeaway.streamplatform.streamregistry.db.dao.AbstractDao.PRIMARY_HINT;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import lombok.extern.slf4j.Slf4j;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheck;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;

import org.apache.avro.SchemaBuilder;
import org.apache.commons.lang3.Validate;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.streams.KafkaStreams;

import com.homeaway.digitalplatform.streamregistry.AvroStream;
import com.homeaway.digitalplatform.streamregistry.AvroStreamKey;
import com.homeaway.digitalplatform.streamregistry.SchemaCompatibility;
import com.homeaway.streamplatform.streamregistry.configuration.HealthCheckStreamConfig;
import com.homeaway.streamplatform.streamregistry.model.Consumer;
import com.homeaway.streamplatform.streamregistry.model.Producer;
import com.homeaway.streamplatform.streamregistry.model.RegionStreamConfig;
import com.homeaway.streamplatform.streamregistry.model.Schema;
import com.homeaway.streamplatform.streamregistry.model.Stream;
import com.homeaway.streamplatform.streamregistry.model.Tags;
import com.homeaway.streamplatform.streamregistry.resource.ConsumerResource;
import com.homeaway.streamplatform.streamregistry.resource.ProducerResource;
import com.homeaway.streamplatform.streamregistry.resource.StreamResource;
import com.homeaway.streamplatform.streamregistry.streams.ManagedKStreams;

@Slf4j
public class StreamRegistryHealthCheck extends HealthCheck {

    public static final Integer PRODUCT_ID = 126845;
    public static final String COMPONENT_ID = "986bef24-0e0d-43aa-adc8-bd39702edd9a";
    public static final String APP_NAME = "StreamRegistryApplication";

    private final ManagedKStreams managedKStreams;
    private final StreamResource streamResource;

    private boolean isStreamCreationHealthy;
    private boolean isStateStoreHealthy;
    private boolean isKStreamInValidState;
    private boolean isProducerRegistrationHealthy;
    private boolean isConsumerRegistrationHealthy;

    private String streamName;
    private String region;
    private int replicationFactor;
    private int partitions;

    private Stream streamRegHealthCheckStream;
    private ProducerResource producerResource;
    private ConsumerResource consumerResource;

    public StreamRegistryHealthCheck(ManagedKStreams managedKStreams, StreamResource streamResource, MetricRegistry metricRegistry,
                                     HealthCheckStreamConfig healthCheckStreamConfig) {
        super();

        Validate.notNull(managedKStreams, "managedKStreams cannot be null");
        Validate.notNull(managedKStreams.getStreams(), "managedKStreams.getStreams() cannot be null");
        Validate.notNull(streamResource, "streamResource cannot be null");


        this.streamName = healthCheckStreamConfig.getName();
        this.region = healthCheckStreamConfig.getClusterRegion();
        this.partitions = healthCheckStreamConfig.getPartitions();
        this.replicationFactor = healthCheckStreamConfig.getReplicationFactor();
        this.managedKStreams = managedKStreams;
        this.streamResource = streamResource;

        metricRegistry.register(Metrics.STREAM_CREATION_HEALTH.getName(), (Gauge<Integer>)() -> isStreamCreationHealthy() ? 1 : 2);
        metricRegistry.register(Metrics.STATE_STORE_HEALTH.getName(), (Gauge<Integer>)() -> isStateStoreHealthy() ? 1 : 2);
        metricRegistry.register(Metrics.STATE_STORE_STATE_HEALTH.getName(), (Gauge<Integer>)() -> isKStreamInValidState() ? 1 : 2);
        metricRegistry.register(Metrics.PRODUCER_REGISTRATION_HEALTH.getName(), (Gauge<Integer>)() -> isProducerRegistrationHealthy() ? 1 : 2);
        metricRegistry.register(Metrics.CONSUMER_REGISTRATION_HEALTH.getName(), (Gauge<Integer>)() -> isConsumerRegistrationHealthy() ? 1 : 2);
        metricRegistry.register(Metrics.STATE_STORE_STATE.getName(), (Gauge<String>)() -> getKstreamsState().toString());

        streamRegHealthCheckStream = createCanaryStream();
        streamResource.upsertStream(streamName, streamRegHealthCheckStream);

        consumerResource = streamResource.getConsumerResource();
        consumerResource.upsertConsumer(streamName, "C1", region);

        producerResource = streamResource.getProducerResource();
        producerResource.upsertProducer(streamName, "P1", region);
    }

    private synchronized boolean isStreamCreationHealthy() {
        return isStreamCreationHealthy;
    }

    private synchronized void setStreamCreationHealthy(boolean value) {
        isStreamCreationHealthy = value;
    }

    private synchronized boolean isStateStoreHealthy() {
        return isStateStoreHealthy;
    }

    private synchronized void setStateStoreHealthy(boolean value) {
        isStateStoreHealthy = value;
    }

    private synchronized boolean isKStreamInValidState() {
        return isKStreamInValidState;
    }

    private synchronized void setKStreamInValidState(boolean value) {
        isKStreamInValidState = value;
    }

    private synchronized boolean isProducerRegistrationHealthy() {
        return isProducerRegistrationHealthy;
    }

    private synchronized void setProducerRegistrationHealthy(boolean value) {
        isProducerRegistrationHealthy = value;
    }

    private synchronized boolean isConsumerRegistrationHealthy() {
        return isConsumerRegistrationHealthy;
    }

    private synchronized void setConsumerRegistrationHealthy(boolean value) {
        isConsumerRegistrationHealthy = value;
    }

    private synchronized KafkaStreams.State getKstreamsState() {
        return managedKStreams.getStreams().state();
    }

    @Override
    protected synchronized Result check() {
        try {
            validateCreateStream();
            validateKStreamState();
            validateProducerRegistration();
            validateConsumerRegistration();
            validateStateStore();
        } catch (Exception exception) {
            log.error("Exception during HealthCheck. ", exception);
            return Result.unhealthy(exception.getMessage());
        }

        return Result.healthy();
    }

    private void validateCreateStream() {
        try {
            Response response = streamResource.upsertStream(streamName, streamRegHealthCheckStream);
            if (response.getStatus() != 202) {
                setStreamCreationHealthy(false);
                throw new IllegalStateException("HealthCheck Failed: Error while upserting a Stream.");
            }
        } catch (Exception e) {
            setStreamCreationHealthy(false);
            throw e;
        }
        setStreamCreationHealthy(true);
    }

    private Stream createCanaryStream() {
        return Stream.builder()
                .name(streamName)
                .schemaCompatibility(SchemaCompatibility.TRANSITIVE_BACKWARD)
                .latestKeySchema(createSampleSchema())
                .latestValueSchema(createSampleSchema())
                .owner(APP_NAME)
                .isAutomationNeeded(false)
                .isDataNeededAtRest(false)
                .tags(createSampleTags())
                .vpcList(Collections.singletonList(region))
                .partitions(partitions)
                .replicationFactor(replicationFactor)
                .build();
    }

    private Schema createSampleSchema() {
        org.apache.avro.Schema sampleSchema = SchemaBuilder.builder("com.streamplatform.healthcheck")
                .record("Healthcheck")
                .fields()
                .requiredString("producer")
                .requiredString("consumer")
                .endRecord();

        return com.homeaway.streamplatform.streamregistry.model.Schema.builder()
                .schemaString(sampleSchema.toString())
                .build();
    }

    private Tags createSampleTags() {
        return com.homeaway.streamplatform.streamregistry.model.Tags.builder()
                .productId(PRODUCT_ID)
                .componentId(COMPONENT_ID)
                .hint(PRIMARY_HINT)
                .build();
    }

    private void validateStateStore() {
        try {
            AvroStreamKey avroStreamKey = AvroStreamKey.newBuilder().setStreamName(streamName).build();
            Optional<AvroStream> avroStreamValue = managedKStreams.getAvroStreamForKey(avroStreamKey);
            if(!avroStreamValue.isPresent() || ! avroStreamValue.get().getName().equals(streamName)) {
                setStateStoreHealthy(false);
                throw new IllegalStateException("HealthCheck Failed: StreamRegistryHealthCheck Stream not available in StateStore.");
            }
        } catch (Exception e) {
            setStateStoreHealthy(false);
            throw e;
        }

        setStateStoreHealthy(true);
    }

    private void validateKStreamState() {
        try {
            KafkaStreams.State kStreamsState = getKstreamsState();
            if (kStreamsState == KafkaStreams.State.PENDING_SHUTDOWN || kStreamsState == KafkaStreams.State.ERROR) {
                setKStreamInValidState(false);
                throw new IllegalStateException("HealthCheck Failed: KStream instance is in ERROR state. App has to be restarted.");
            }
        } catch (Exception e) {
            setKStreamInValidState(false);
            throw e;
        }

        setKStreamInValidState(true);
    }

    private void validateProducerRegistration() {
        try {
            Response response = producerResource.getProducer(streamName, "P1");

            if(response.getStatus() != Status.OK.getStatusCode()) {
                setProducerRegistrationHealthy(false);
                throw new IllegalStateException(String.format("HealthCheck Failed: Producer P1 Not Found. HEALTH_CHECK_STREAM_NAME=%s", streamName));
            }

            List<RegionStreamConfig> regionStreamConfigList = ((Producer)response.getEntity()).getRegionStreamConfigList();

            if (regionStreamConfigList != null && regionStreamConfigList.size() > 0) {
                RegionStreamConfig regionStreamConfig = regionStreamConfigList.get(0);
                String cluster = regionStreamConfig.getCluster();
                String registeredRegion = regionStreamConfig.getRegion();
                List<String> topics = regionStreamConfig.getTopics();
                Map<String, String> streamConfiguration = regionStreamConfig.getStreamConfiguration();
                String bootstrapServers = streamConfiguration.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG);
                String schemaRegServers = streamConfiguration.get(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG);

                log.debug("Producer Registration Check: cluster={} ; registeredRegion={} ; topics={} ; streamConfiguration={}",
                        cluster, registeredRegion, topics, streamConfiguration);

                if (cluster == null || !registeredRegion.equals(region) || topics.size() <= 0 || bootstrapServers == null || schemaRegServers == null) {
                    setProducerRegistrationHealthy(false);
                    throw new IllegalStateException(String.format("HealthCheck Failed: Producer Registration Failed. regionStreamConfigList=%s inputRegion=%s", regionStreamConfigList, region));
                }
            } else {
                setProducerRegistrationHealthy(false);
                throw new IllegalStateException(String.format("HealthCheck Failed: Producer Registration does not return regionConfiguration. regionStreamConfigList=%s inputRegion=%s", regionStreamConfigList, region));
            }
        } catch (Exception e) {
            setProducerRegistrationHealthy(false);
            throw e;
        }
        setProducerRegistrationHealthy(true);
    }

    private void validateConsumerRegistration() {
        try {
            Response response = consumerResource.getConsumer(streamName, "C1");

            if(response.getStatus() != Status.OK.getStatusCode()) {
                setProducerRegistrationHealthy(false);
                throw new IllegalStateException(String.format("HealthCheck Failed: Consumer C1 Not Found. HEALTH_CHECK_STREAM_NAME=%s", streamName));
            }

            List<RegionStreamConfig> regionStreamConfigList = ((Consumer)response.getEntity()).getRegionStreamConfigList();

            if (regionStreamConfigList != null && regionStreamConfigList.size() > 0) {
                RegionStreamConfig regionStreamConfig = regionStreamConfigList.get(0);
                String cluster = regionStreamConfig.getCluster();
                String registeredRegion = regionStreamConfig.getRegion();
                List<String> topics = regionStreamConfig.getTopics();
                Map<String, String> streamConfiguration = regionStreamConfig.getStreamConfiguration();
                String bootstrapServers = streamConfiguration.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG);
                String schemaRegServers = streamConfiguration.get(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG);

                log.debug("Consumer Registration Check: cluster={} ; registeredRegion={} ; topics={} ; streamConfiguration={}",
                        cluster, registeredRegion, topics, streamConfiguration);

                if (cluster == null || !registeredRegion.equals(region) || topics.size() <= 0 || bootstrapServers == null || schemaRegServers == null) {
                    setConsumerRegistrationHealthy(false);
                    throw new IllegalStateException(String.format("HealthCheck Failed: Consumer Registration Failed. regionStreamConfigList=%s inputRegion=%s", regionStreamConfigList, region));
                }
            } else {
                setConsumerRegistrationHealthy(false);
                throw new IllegalStateException(String.format("HealthCheck Failed: Consumer Registration does not return regionConfiguration. regionStreamConfigList=%s inputRegion=%s", regionStreamConfigList, region));
            }

        } catch (Exception e) {
            setConsumerRegistrationHealthy(false);
            throw e;
        }
        setConsumerRegistrationHealthy(true);
    }

    public enum Metrics {
        STREAM_CREATION_HEALTH("app.is_stream_creation_healthy"),
        PRODUCER_REGISTRATION_HEALTH("app.is_producer_registration_healthy"),
        CONSUMER_REGISTRATION_HEALTH("app.is_consumer_registration_healthy"),
        STATE_STORE_HEALTH("app.is_globaltable_statestore_healthy"),
        STATE_STORE_STATE_HEALTH("app.is_globaltable_kstreams_in_valid_state"),
        STATE_STORE_STATE("app.globaltable_kstreams_state");

        private String name;

        Metrics(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

}