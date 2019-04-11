/* Copyright (c) 2018-Present Expedia Group.
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
package com.homeaway.streamplatform.streamregistry;

import static com.homeaway.streamplatform.streamregistry.extensions.schema.SchemaManager.MAX_SCHEMA_VERSIONS_CAPACITY;
import static io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.streams.StreamsConfig.ROCKSDB_CONFIG_SETTER_CLASS_CONFIG;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.client.Client;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseFilter;

import lombok.extern.slf4j.Slf4j;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.servlets.PingServlet;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.base.Preconditions;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.dropwizard.Application;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.federecio.dropwizard.swagger.SwaggerBundle;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.utils.Utils;
import org.apache.kafka.streams.state.RocksDBConfigSetter;
import org.rocksdb.BlockBasedTableConfig;
import org.rocksdb.Options;

import com.homeaway.streamplatform.streamregistry.configuration.EventStoreTopic;
import com.homeaway.streamplatform.streamregistry.configuration.InfraManagerConfig;
import com.homeaway.streamplatform.streamregistry.configuration.SchemaManagerConfig;
import com.homeaway.streamplatform.streamregistry.configuration.StreamRegistryConfiguration;
import com.homeaway.streamplatform.streamregistry.configuration.StreamValidatorConfig;
import com.homeaway.streamplatform.streamregistry.configuration.TopicsConfig;
import com.homeaway.streamplatform.streamregistry.db.dao.StreamDao;
import com.homeaway.streamplatform.streamregistry.db.dao.impl.StreamDaoImpl;
import com.homeaway.streamplatform.streamregistry.extensions.schema.SchemaManager;
import com.homeaway.streamplatform.streamregistry.extensions.validation.StreamValidator;
import com.homeaway.streamplatform.streamregistry.health.EventStoreConfigHealthCheck;
import com.homeaway.streamplatform.streamregistry.health.StreamRegistryHealthCheck;
import com.homeaway.streamplatform.streamregistry.model.Consumer;
import com.homeaway.streamplatform.streamregistry.model.Producer;
import com.homeaway.streamplatform.streamregistry.provider.InfraManager;
import com.homeaway.streamplatform.streamregistry.resource.ClusterResource;
import com.homeaway.streamplatform.streamregistry.resource.RegionResource;
import com.homeaway.streamplatform.streamregistry.resource.StreamResource;
import com.homeaway.streamplatform.streamregistry.service.ClusterService;
import com.homeaway.streamplatform.streamregistry.service.RegionService;
import com.homeaway.streamplatform.streamregistry.service.StreamClientService;
import com.homeaway.streamplatform.streamregistry.service.StreamService;
import com.homeaway.streamplatform.streamregistry.service.impl.ClusterServiceImpl;
import com.homeaway.streamplatform.streamregistry.service.impl.ConsumerServiceImpl;
import com.homeaway.streamplatform.streamregistry.service.impl.ProducerServiceImpl;
import com.homeaway.streamplatform.streamregistry.service.impl.RegionServiceImpl;
import com.homeaway.streamplatform.streamregistry.service.impl.StreamServiceImpl;
import com.homeaway.streamplatform.streamregistry.streams.ManagedInfraManager;
import com.homeaway.streamplatform.streamregistry.streams.ManagedKStreams;
import com.homeaway.streamplatform.streamregistry.streams.ManagedKafkaProducer;
import com.homeaway.streamplatform.streamregistry.utils.KafkaTopicClient;

/**
 * This is the main DropWizard application that bootstraps DropWizard, wires up the app,
 * and sets up the various lifecycle managed resources that will run during the lifecycle of the app.
 */
@Slf4j
public class StreamRegistryApplication extends Application<StreamRegistryConfiguration> {

    private MetricRegistry metricRegistry;

    private InfraManager infraManager;

    private RegionService regionService;
    private StreamService streamService;
    private StreamClientService<Producer> producerDao;
    private StreamClientService<Consumer> consumerDao;
    private ClusterService clusterService;

    private StreamResource streamResource;

    public static void main(final String[] args) throws Exception {
        new StreamRegistryApplication().run(args);
    }

    @Override
    public String getName() {
        return this.getClass().getName();
    }

    @Override
    public void initialize(final Bootstrap<StreamRegistryConfiguration> bootstrap) {
        // EnvironmentVariableSubstitutor enables EnvVariables to be substituted into the configuration before initialization
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(false)
                )
        );
        bootstrap.addBundle(new SwaggerBundle<StreamRegistryConfiguration>() {
            @Override
            protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(StreamRegistryConfiguration configuration) {
                return configuration.getSwaggerBundleConfiguration();
            }
        });

        metricRegistry = bootstrap.getMetricRegistry();
    }

    @Override
    public void run(final StreamRegistryConfiguration configuration, final Environment environment) {
        // Initialize managed beans
        ManagedKStreams managedKStreams = createManagedKStreams(configuration);
        ManagedInfraManager managedInfraManager = createManagedInfraManager(configuration);
        Preconditions.checkState(managedInfraManager != null, "managedInfraManager cannot be null.");
        ManagedKafkaProducer managedKafkaProducer = createManagedKafkaProducer(configuration);

        // Create the Kafka topic used as EventStore
        createEventStoreKafkaTopicIfNotExists(configuration);

        // Initialize the DAO's
        initServiceAndDao(configuration, environment, managedKStreams, managedKafkaProducer);

        // Initialize and register the SR resources to JerseyEnvironment
        initAndRegisterResource(environment);

        // Initialize and register monitoring and health check hooks
        environment.getApplicationContext().addServlet(PingServlet.class, "/ping");
        initAndRegisterHealthCheck(configuration, environment, managedKStreams);

        // Centralize initialization and configuration of SR server Object Mapper's
        registerServiceMapper(environment);

        // In order to avoid muddle up with HK2 and Jersey lifecycle dependency we wrap up all managed beans in a
        // centralized container to manage server components start and stop ordering
        registerManagedContainer(environment, managedKStreams, managedInfraManager, managedKafkaProducer);

        // Register list of Jersey Filters.
        registerFilters(environment, configuration);

        // TODO: Make project completely based on unit tests (integration should be a separate project) (#100)
    }

    private void createEventStoreKafkaTopicIfNotExists(StreamRegistryConfiguration configuration) {
        EventStoreTopic eventStoreTopic = configuration.getTopicsConfig().getEventStoreTopic();
        String topicName = eventStoreTopic.getName();
        String kafkaBootstrapURI = configuration.getKafkaProducerConfig().getKafkaProducerProperties().get(BOOTSTRAP_SERVERS_CONFIG);
        KafkaTopicClient kafkaTopicClient = new KafkaTopicClient(kafkaBootstrapURI);
        try {
            if (kafkaTopicClient.isKafkaTopicPresent(topicName)) {
                log.debug("Topic {} present. Topic Details = {}", topicName);
                kafkaTopicClient.validateTopicConfigs(eventStoreTopic.getName(), eventStoreTopic.getProperties());
                log.debug("Stream Registry underlying EventStore kafka topic config {} validation passed", eventStoreTopic.getProperties());
            } else {
                kafkaTopicClient.createTopic(eventStoreTopic.getName(),
                        eventStoreTopic.getPartitions(),
                        eventStoreTopic.getReplicationFactor(),
                        eventStoreTopic.getProperties());
                log.info("Stream Registry underlying kafka event store created : {}", eventStoreTopic.getName());
            }
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(String.format("Error while communication with the underlying kafka data-store." +
                    " Topic={} BootstrapURI={}", topicName, kafkaBootstrapURI), e);
        }
    }

    private void registerFilters(Environment environment, StreamRegistryConfiguration configuration) {
        try {
            if (configuration.getRequestFilterClassNames() != null) {
                for (String requestFilterClassName : configuration.getRequestFilterClassNames()) {
                    environment.jersey().register(Utils.newInstance(requestFilterClassName, ContainerRequestFilter.class));
                }
            }

            if (configuration.getResponseFilterClassNames() != null) {
                for (String responseFilterClassName : configuration.getResponseFilterClassNames()) {
                    environment.jersey().register(Utils.newInstance(responseFilterClassName, ContainerResponseFilter.class));
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException(String.format("Error loading Jersey Filter from configuration %s",configuration),  e);
        }
    }

    public static StreamValidator loadValidator(StreamRegistryConfiguration configuration,
                                                Client httpClient,
                                                RegionService regionService) {
        StreamValidatorConfig streamValidatorConfig = configuration.getStreamValidatorConfig();

        if (streamValidatorConfig != null) {
            String validatorClass = streamValidatorConfig.getClassName();

            if (validatorClass != null && !validatorClass.isEmpty()) {
                try {
                    StreamValidator streamValidator = Utils.newInstance(validatorClass, StreamValidator.class);

                    Map<String, ?> validatorProperties = streamValidatorConfig.getProperties();
                    Map<String, Object> validatorConfig = new HashMap<>();
                    if (validatorProperties != null) {
                        validatorConfig.putAll(validatorProperties);
                    }

                    // HTTP Client is not pre-loaded with a URL. The validator needs to set a target.
                    validatorConfig.put(StreamValidatorConfig.STREAM_REGISTRY_HTTP_CLIENT, httpClient);
                    // RegionService allows us to load a dynamic list of supported regions for a Stream.
                    validatorConfig.put(StreamValidatorConfig.STREAM_REGISTRY_REGION_SUPPLIER, regionService);
                    streamValidator.configure(validatorConfig);
                    return streamValidator;
                } catch (ClassNotFoundException e) {
                    throw new IllegalStateException(String.format("Error loading streamValidator from configuration %s",validatorClass),  e);
                }
            }
        }
        return null;
    }

    public static SchemaManager loadSchemaManager(StreamRegistryConfiguration configuration) {
        SchemaManagerConfig schemaManagerConfig = getSchemaManagerConfig(configuration);

        String schemaManagerClass = schemaManagerConfig.getClassName();

        try {
            SchemaManager schemaManager = Utils.newInstance(schemaManagerClass, SchemaManager.class);
            schemaManagerConfig.getProperties().put(MAX_SCHEMA_VERSIONS_CAPACITY, 100);
            schemaManager.configure(schemaManagerConfig.getProperties());

            return schemaManager;
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Error loading SchemaManager from configuration", e);
        }
    }

    public static class CustomRocksDBConfig implements RocksDBConfigSetter {
        @Override
        public void setConfig(final String storeName, final Options options, final Map<String, Object> configs) {
            BlockBasedTableConfig tableConfig = new org.rocksdb.BlockBasedTableConfig();
            tableConfig.setBlockCacheSize(2 * 1024 * 1024L);
            tableConfig.setBlockSize(2 * 1024L);
            tableConfig.setCacheIndexAndFilterBlocks(true);
            options.setTableFormatConfig(tableConfig);
            options.setMaxWriteBufferNumber(2);
            options.optimizeFiltersForHits();
        }
    }

    private ManagedKStreams createManagedKStreams(final StreamRegistryConfiguration configuration) {
        Properties kstreamsProperties = new Properties();
        configuration.getKafkaStreamsConfig().getKstreamsProperties().forEach(kstreamsProperties::put);
        kstreamsProperties.put(ROCKSDB_CONFIG_SETTER_CLASS_CONFIG, CustomRocksDBConfig.class);
        TopicsConfig topicsConfig = configuration.getTopicsConfig();
        SchemaManagerConfig schemaManagerConfig = getSchemaManagerConfig(configuration);

        return new ManagedKStreams(kstreamsProperties, topicsConfig, schemaManagerConfig);
    }

    private static SchemaManagerConfig getSchemaManagerConfig(StreamRegistryConfiguration configuration) {
        SchemaManagerConfig schemaManagerConfig = configuration.getSchemaManagerConfig();

        Preconditions.checkNotNull(schemaManagerConfig, "schema manager config cannot be null");
        String schemaManagerClass = schemaManagerConfig.getClassName();

        Preconditions.checkState(schemaManagerClass != null && !schemaManagerClass.isEmpty(),
            "schema manager class must be defined");

        Preconditions.checkState(schemaManagerConfig.getProperties() != null
                && schemaManagerConfig.getProperties().containsKey(SCHEMA_REGISTRY_URL_CONFIG),
            "schemaManagerConfig properties must define "+ SCHEMA_REGISTRY_URL_CONFIG);
        return schemaManagerConfig;
    }

    private ManagedInfraManager createManagedInfraManager(final StreamRegistryConfiguration configuration) {
        InfraManagerConfig infraManagerConfig = configuration.getInfraManagerConfig();
        String infraManagerClassName = infraManagerConfig.getClassName();

        Preconditions.checkState(!infraManagerClassName.isEmpty(), "infraManagerClassName cannot be empty");

        try {
            infraManager = Utils.newInstance(infraManagerClassName, InfraManager.class);
            Map<String, Object> infraConfig = infraManagerConfig.getConfig();
            SchemaManagerConfig schemaManagerConfig = getSchemaManagerConfig(configuration);

            infraConfig.put(SCHEMA_REGISTRY_URL_CONFIG, schemaManagerConfig.getProperties().get(SCHEMA_REGISTRY_URL_CONFIG));

            infraManager.configure(infraConfig);
            return new ManagedInfraManager(infraManager);
        } catch (Exception e) {
            log.error("Error loading/configuring Infra Manager Class", e);
            throw new IllegalStateException("Could not load/configure Infra Manager class", e);
        }
    }

    private ManagedKafkaProducer createManagedKafkaProducer(final StreamRegistryConfiguration configuration) {
        Properties producerProperties = new Properties();
        producerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        producerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        TopicsConfig topicsConfig = configuration.getTopicsConfig();

        configuration.getKafkaProducerConfig().getKafkaProducerProperties().forEach(producerProperties::put);

        return new ManagedKafkaProducer(producerProperties, topicsConfig);
    }

    private StreamValidator createStreamValidator(final StreamRegistryConfiguration configuration, final Environment environment) {
        Preconditions.checkState(regionService != null, "regionService cannot be null.");

        final Client httpClient = new JerseyClientBuilder(environment).using(configuration.getHttpClient()).using(environment)
            .build("remoteStateStoreHttpClient");

        log.info("Connection Timeout:{}", configuration.getHttpClient().getConnectionTimeout());
        log.info("Connection Request Timeout:{}", configuration.getHttpClient().getConnectionRequestTimeout());

        return loadValidator(configuration, httpClient, regionService);
    }

    private void initServiceAndDao(final StreamRegistryConfiguration configuration, final Environment environment, ManagedKStreams managedKStreams,
                                   ManagedKafkaProducer managedKafkaProducer) {

        Preconditions.checkState(managedKStreams != null, "managedKStreams cannot be null.");
        Preconditions.checkState(managedKafkaProducer != null, "managedKafkaProducer cannot be null.");
        Preconditions.checkState(infraManager != null, "infraManager cannot be null.");

        String env = configuration.getEnv();

        regionService = new RegionServiceImpl(env, infraManager);
        clusterService = new ClusterServiceImpl(infraManager);

        SchemaManager schemaManager = loadSchemaManager(configuration);
        StreamValidator streamValidator = createStreamValidator(configuration, environment);

        StreamDao streamDao = new StreamDaoImpl(managedKafkaProducer, managedKStreams);
        streamService = new StreamServiceImpl(streamDao, env, regionService, clusterService, infraManager, streamValidator, schemaManager);
        producerDao = new ProducerServiceImpl(streamDao, env, regionService, clusterService, infraManager);
        consumerDao = new ConsumerServiceImpl(streamDao, env, regionService, clusterService, infraManager);
    }

    private void initAndRegisterResource(final Environment environment) {

        Preconditions.checkState(streamService != null, "streamService cannot be null.");
        Preconditions.checkState(producerDao != null, "producerDao cannot be null.");
        Preconditions.checkState(consumerDao != null, "consumerDao cannot be null.");
        Preconditions.checkState(regionService != null, "regionService cannot be null.");

        streamResource = new StreamResource(streamService, producerDao, consumerDao);
        RegionResource regionResource = new RegionResource(regionService);
        ClusterResource clusterResource = new ClusterResource(clusterService);

        environment.jersey().register(streamResource);
        environment.jersey().register(regionResource);
        environment.jersey().register(clusterResource);
    }

    private void initAndRegisterHealthCheck(final StreamRegistryConfiguration configuration, final Environment environment,
        ManagedKStreams managedKStreams) {

        Preconditions.checkState(managedKStreams != null, "managedKStreams cannot be null.");
        Preconditions.checkState(streamResource != null, "streamResource cannot be null.");

        EventStoreConfigHealthCheck eventStoreConfigHealthCheck = new EventStoreConfigHealthCheck(configuration, metricRegistry);
        environment.healthChecks().register("eventStoreConfigHealthCheck", eventStoreConfigHealthCheck);

        StreamRegistryHealthCheck streamRegistryHealthCheck =
            new StreamRegistryHealthCheck(managedKStreams, streamResource, metricRegistry, configuration.getHealthCheckStreamConfig());
        environment.healthChecks().register("streamRegistryHealthCheck", streamRegistryHealthCheck);
    }

    private void registerServiceMapper(final Environment environment) {
        environment.getObjectMapper().setSerializationInclusion(JsonInclude.Include.ALWAYS);
        environment.getObjectMapper().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        GuavaModule guavaModule = new GuavaModule();
        guavaModule.configureAbsentsAsNulls(true);
        environment.getObjectMapper().registerModule(new GuavaModule().configureAbsentsAsNulls(true));
    }

    private void registerManagedContainer(final Environment environment, ManagedKStreams managedKStreams,
        ManagedInfraManager managedInfraManager, ManagedKafkaProducer managedKafkaProducer) {

        Preconditions.checkState(managedKStreams != null, "managedKStreams cannot be null.");
        Preconditions.checkState(managedInfraManager != null, "managedInfraManager cannot be null.");
        Preconditions.checkState(managedKafkaProducer != null, "managedKafkaProducer cannot be null.");

        StreamRegistryManagedContainer managedContainer =
            new StreamRegistryManagedContainer(managedKStreams, managedInfraManager, managedKafkaProducer);
        environment.lifecycle().manage(managedContainer);
    }

}
