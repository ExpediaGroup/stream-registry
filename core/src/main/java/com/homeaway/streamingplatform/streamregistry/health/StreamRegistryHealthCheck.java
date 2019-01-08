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
package com.homeaway.streamingplatform.streamregistry.health;

import static com.homeaway.streamingplatform.streamregistry.db.dao.AbstractDao.PRIMARY_HINT;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.core.Response;

import lombok.extern.slf4j.Slf4j;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheck;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;

import org.apache.commons.lang3.Validate;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.streams.KafkaStreams;

import com.homeaway.digitalplatform.streamregistry.AvroStream;
import com.homeaway.digitalplatform.streamregistry.AvroStreamKey;
import com.homeaway.digitalplatform.streamregistry.SchemaCompatibility;
import com.homeaway.streamingplatform.streamregistry.model.Consumer;
import com.homeaway.streamingplatform.streamregistry.model.Producer;
import com.homeaway.streamingplatform.streamregistry.model.RegionStreamConfig;
import com.homeaway.streamingplatform.streamregistry.model.Schema;
import com.homeaway.streamingplatform.streamregistry.model.Stream;
import com.homeaway.streamingplatform.streamregistry.model.Tags;
import com.homeaway.streamingplatform.streamregistry.resource.ConsumerResource;
import com.homeaway.streamingplatform.streamregistry.resource.ProducerResource;
import com.homeaway.streamingplatform.streamregistry.resource.StreamResource;
import com.homeaway.streamingplatform.streamregistry.streams.ManagedKStreams;

@Slf4j
public class StreamRegistryHealthCheck extends HealthCheck {

    public static final Integer PRODUCT_ID = 126845;
    public static final String COMPONENT_ID = "986bef24-0e0d-43aa-adc8-bd39702edd9a";
    public static final String APP_NAME = "StreamRegistryApplication";
    private static final String HEALTH_CHECK_STREAM_NAME = "StreamRegistryHealthCheck";

    private final ManagedKStreams managedKStreams;
    private final StreamResource streamResource;

    private boolean isStreamCreationHealthy;
    private boolean isStateStoreHealthy;
    private boolean isKStreamInValidState;
    private boolean isProducerRegistrationHealthy;
    private boolean isConsumerRegistrationHealthy;

    // TODO - This needs to move to a /namespace approach vs an environment variable - see #29
    private final String region = System.getenv("MPAAS_REGION");

    public StreamRegistryHealthCheck(ManagedKStreams managedKStreams, StreamResource streamResource, MetricRegistry metricRegistry) {
        super();

        Validate.notNull(managedKStreams, "managedKStreams cannot be null");
        Validate.notNull(managedKStreams.getStreams(), "managedKStreams.getStreams() cannot be null");
        Validate.notNull(streamResource, "streamResource cannot be null");

        this.managedKStreams = managedKStreams;
        this.streamResource = streamResource;

        metricRegistry.register(Metrics.STREAM_CREATION_HEALTH.getName(), (Gauge<Integer>)() -> isStreamCreationHealthy() ? 1 : 2);
        metricRegistry.register(Metrics.STATE_STORE_HEALTH.getName(), (Gauge<Integer>)() -> isStateStoreHealthy() ? 1 : 2);
        metricRegistry.register(Metrics.STATE_STORE_STATE_HEALTH.getName(), (Gauge<Integer>)() -> isKStreamInValidState() ? 1 : 2);
        metricRegistry.register(Metrics.PRODUCER_REGISTRATION_HEALTH.getName(), (Gauge<Integer>)() -> isProducerRegistrationHealthy() ? 1 : 2);
        metricRegistry.register(Metrics.CONSUMER_REGISTRATION_HEALTH.getName(), (Gauge<Integer>)() -> isConsumerRegistrationHealthy() ? 1 : 2);
        metricRegistry.register(Metrics.STATE_STORE_STATE.getName(), (Gauge<String>)() -> getKstreamsState().toString());
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
            validateStateStore();
            validateKStreamState();
            validateProducerRegistration();
            validateConsumerRegistration();
        } catch (Exception exception) {
            log.error("Exception during HealthCheck. ", exception);
            return Result.unhealthy(exception.getMessage());
        }

        return Result.healthy();
    }

    private void validateCreateStream() {
        try {
            Stream streamRegHealthCheckStream = createCanaryStream();
            Response response = streamResource.upsertStream(HEALTH_CHECK_STREAM_NAME, streamRegHealthCheckStream);
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
                .name(HEALTH_CHECK_STREAM_NAME)
                .schemaCompatibility(SchemaCompatibility.TRANSITIVE_BACKWARD)
                .latestKeySchema(createSampleSchema())
                .latestValueSchema(createSampleSchema())
                .owner(APP_NAME)
                .isAutomationNeeded(false)
                .isDataNeededAtRest(false)
                .tags(createSampleTags())
                .vpcList(Collections.singletonList(region))
                .build();
    }

    private Schema createSampleSchema() {
        return com.homeaway.streamingplatform.streamregistry.model.Schema.builder()
                .id("1")
                .schemaString("-")
                .version(1).build();
    }

    private Tags createSampleTags() {
        return com.homeaway.streamingplatform.streamregistry.model.Tags.builder()
                .productId(PRODUCT_ID)
                .componentId(COMPONENT_ID)
                .hint(PRIMARY_HINT)
                .build();
    }

    private void validateStateStore() {
        try {
            AvroStreamKey avroStreamKey = AvroStreamKey.newBuilder().setStreamName(HEALTH_CHECK_STREAM_NAME).build();
            Optional<AvroStream> avroStreamValue = managedKStreams.getAvroStreamForKey(avroStreamKey);
            if(!avroStreamValue.isPresent() || ! avroStreamValue.get().getName().equals(HEALTH_CHECK_STREAM_NAME)) {
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
            ProducerResource producerResource = streamResource.getProducerResource();
            String producerName = "P1";
            Response response = producerResource.upsertProducer(HEALTH_CHECK_STREAM_NAME, producerName, region);
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
            ConsumerResource consumerResource = streamResource.getConsumerResource();
            String consumerName = "C1";
            Response response = consumerResource.upsertConsumer(HEALTH_CHECK_STREAM_NAME, consumerName, region);
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