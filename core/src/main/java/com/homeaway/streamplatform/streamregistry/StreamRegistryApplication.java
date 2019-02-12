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
package com.homeaway.streamplatform.streamregistry;

import static com.homeaway.streamplatform.streamregistry.extensions.schema.SchemaManager.MAX_SCHEMA_VERSIONS_CAPACITY;
import static io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;
import static org.apache.kafka.streams.StreamsConfig.ROCKSDB_CONFIG_SETTER_CLASS_CONFIG;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.inject.Singleton;
import javax.ws.rs.client.Client;

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
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.federecio.dropwizard.swagger.SwaggerBundle;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.utils.Utils;
import org.apache.kafka.streams.state.RocksDBConfigSetter;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;
import org.glassfish.jersey.servlet.ServletContainer;
import org.rocksdb.BlockBasedTableConfig;
import org.rocksdb.Options;

import com.homeaway.streamplatform.streamregistry.configuration.HealthCheckStreamConfig;
import com.homeaway.streamplatform.streamregistry.configuration.InfraManagerConfig;
import com.homeaway.streamplatform.streamregistry.configuration.SchemaManagerConfig;
import com.homeaway.streamplatform.streamregistry.configuration.StreamRegistryConfiguration;
import com.homeaway.streamplatform.streamregistry.configuration.StreamValidatorConfig;
import com.homeaway.streamplatform.streamregistry.configuration.TopicsConfig;
import com.homeaway.streamplatform.streamregistry.db.dao.RegionDao;
import com.homeaway.streamplatform.streamregistry.db.dao.StreamClientDao;
import com.homeaway.streamplatform.streamregistry.db.dao.StreamDao;
import com.homeaway.streamplatform.streamregistry.db.dao.impl.ConsumerDaoImpl;
import com.homeaway.streamplatform.streamregistry.db.dao.impl.ProducerDaoImpl;
import com.homeaway.streamplatform.streamregistry.db.dao.impl.RegionDaoImpl;
import com.homeaway.streamplatform.streamregistry.db.dao.impl.StreamDaoImpl;
import com.homeaway.streamplatform.streamregistry.extensions.schema.SchemaManager;
import com.homeaway.streamplatform.streamregistry.extensions.validation.StreamValidator;
import com.homeaway.streamplatform.streamregistry.health.StreamRegistryHealthCheck;
import com.homeaway.streamplatform.streamregistry.model.Consumer;
import com.homeaway.streamplatform.streamregistry.model.Producer;
import com.homeaway.streamplatform.streamregistry.provider.InfraManager;
import com.homeaway.streamplatform.streamregistry.resource.RegionResource;
import com.homeaway.streamplatform.streamregistry.resource.StreamResource;
import com.homeaway.streamplatform.streamregistry.streams.ManagedInfraManager;
import com.homeaway.streamplatform.streamregistry.streams.ManagedKStreams;
import com.homeaway.streamplatform.streamregistry.streams.ManagedKafkaProducer;

/**
 * This is the main DropWizard application that bootstraps DropWizard, wires up the app,
 * and sets up the various lifecycle managed resources that will run during the lifecycle of the app.
 */
@Slf4j
public class StreamRegistryApplication extends Application<StreamRegistryConfiguration> {

    private MetricRegistry metricRegistry;

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
        Properties kstreamsProperties = new Properties();
        configuration.getKafkaStreamsConfig().getKstreamsProperties().forEach(kstreamsProperties::put);
        kstreamsProperties.put(ROCKSDB_CONFIG_SETTER_CLASS_CONFIG, CustomRocksDBConfig.class);
        TopicsConfig topicsConfig = configuration.getTopicsConfig();

        ManagedKStreams managedKStreams = new ManagedKStreams(kstreamsProperties, topicsConfig);
        environment.lifecycle().manage(managedKStreams);

        InfraManagerConfig infraManagerConfig = configuration.getInfraManagerConfig();
        String infraManagerClassName = infraManagerConfig.getClassName();

        InfraManager infraManager = null;
        if (infraManagerClassName != null && !infraManagerClassName.isEmpty()) {
            try {
                infraManager = Utils.newInstance(infraManagerClassName, InfraManager.class);
                infraManager.configure(infraManagerConfig.getConfig());
            } catch (Exception e) {
                log.error("Error loading/configuring Infra Manager Class", e);
                throw new IllegalStateException("Could not load/configure Infra Manager class", e);
            }
        }
        ManagedInfraManager managedInfraManager = new ManagedInfraManager(infraManager);

        environment.lifecycle().manage(managedInfraManager);

        Properties producerProperties = new Properties();
        producerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        producerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);

        configuration.getKafkaProducerConfig().getKafkaProducerProperties().forEach(producerProperties::put);

        ManagedKafkaProducer managedProducer = new ManagedKafkaProducer(producerProperties, topicsConfig);

        environment.lifecycle().manage(managedProducer);

        // TODO: Check if State Store is Initialized (#98)
        // This may not be needed if Healthcheck is moved to ManagedKafkaProducer
        // If not healthy after timeout log error and proceed anyway
        try {
            // Sleep needed to make sure the processor's init method is called before servicing the HTTP requests.
            Thread.sleep(10000L);
        } catch (InterruptedException e) {
            log.error("Error while sleeping the main thread for 10 seconds so that Kstream topology gets initialized.", e);
        }

        final Client httpClient = new JerseyClientBuilder(environment)
                .using(configuration.getHttpClient())
                .using(environment)
                .build("remoteStateStoreHttpClient");

        log.info("Connection Timeout:{}", configuration.getHttpClient().getConnectionTimeout());
        log.info("Connection Request Timeout:{}", configuration.getHttpClient().getConnectionRequestTimeout());

        String env = configuration.getEnv();
        RegionDao regionDao = new RegionDaoImpl(env, infraManager);

        StreamValidator streamValidator = loadValidator(configuration, httpClient, regionDao);
        Preconditions.checkState(streamValidator != null, "streamValidator cannot be null.");

        SchemaManager schemaManager = loadSchemaManager(configuration);

        JerseyEnvironment jersey = environment.jersey();
        jersey.register(new AbstractBinder() {

            @Override
            protected void configure() {
                bind(managedKStreams).to(ManagedKStreams.class);
                bind(managedInfraManager).to(ManagedInfraManager.class);
                bind(managedProducer).to(ManagedKafkaProducer.class);
                bind(env).to(String.class).named("stream-registry-env");
                bind(regionDao).to(RegionDao.class);
                bind(streamValidator).to(StreamValidator.class);
                bind(schemaManager).to(SchemaManager.class);

                bind(configuration.getHealthCheckStreamConfig()).to(HealthCheckStreamConfig.class);
                bind(metricRegistry).to(MetricRegistry.class);
                bind(StreamRegistryHealthCheck.class).to(StreamRegistryHealthCheck.class);

                bind(StreamDaoImpl.class).to(StreamDao.class).in(Singleton.class);
                bind(ProducerDaoImpl.class).to(new TypeLiteral<StreamClientDao<Producer>>() {}).in(Singleton.class);
                bind(ConsumerDaoImpl.class).to(new TypeLiteral<StreamClientDao<Consumer>>() {}).in(Singleton.class);
                bind(StreamResource.class).to(StreamResource.class).in(Singleton.class);
                bind(RegionResource.class).to(RegionResource.class).in(Singleton.class);

            }
        });

        environment.jersey().register(StreamResource.class);
        environment.jersey().register(RegionResource.class);

        environment.getApplicationContext().addServlet(PingServlet.class, "/ping");

        environment.jersey().register(new ApplicationEventListener() {

            @Override
            public void onEvent(ApplicationEvent event) {
                if (event.getType() == ApplicationEvent.Type.INITIALIZATION_FINISHED) {
                    ServiceLocator serviceLocator =
                        ((ServletContainer) environment.getJerseyServletContainer()).getApplicationHandler().getServiceLocator();

                    StreamRegistryHealthCheck streamRegistryHealthCheck = serviceLocator.getService(StreamRegistryHealthCheck.class);

                    environment.healthChecks().register("streamRegistryHealthCheck", streamRegistryHealthCheck);
                }
            }

            @Override
            public RequestEventListener onRequest(RequestEvent requestEvent) {
                return null;
            }
        });

        // TODO: Make project completely based on unit tests (integration should be a separate project) (#100)

        environment.getObjectMapper().setSerializationInclusion(JsonInclude.Include.ALWAYS);
        environment.getObjectMapper()
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        GuavaModule guavaModule = new GuavaModule();
        guavaModule.configureAbsentsAsNulls(true);
        environment.getObjectMapper().registerModule(new GuavaModule().configureAbsentsAsNulls(true));
    }

    public static StreamValidator loadValidator(StreamRegistryConfiguration configuration,
                                                Client httpClient,
                                                RegionDao regionDao) {
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
                    // RegionDao allows us to load a dynamic list of supported regions for a Stream.
                    validatorConfig.put(StreamValidatorConfig.STREAM_REGISTRY_REGION_SUPPLIER, regionDao);
                    streamValidator.configure(validatorConfig);
                    return streamValidator;
                } catch (ClassNotFoundException e) {
                    throw new IllegalStateException("Error loading streamValidator from configuration", e);
                }
            }
        }
        return null;
    }

    public static SchemaManager loadSchemaManager(StreamRegistryConfiguration configuration) {
        SchemaManagerConfig schemaManagerConfig = configuration.getSchemaManagerConfig();

        Preconditions.checkNotNull(schemaManagerConfig, "schema manager config cannot be null");
        String schemaManagerClass = schemaManagerConfig.getClassName();

        Preconditions.checkState(schemaManagerClass != null && !schemaManagerClass.isEmpty(),
                "schema manager class must be defined");

        Preconditions.checkState(schemaManagerConfig.getProperties() != null
                && schemaManagerConfig.getProperties().containsKey(SCHEMA_REGISTRY_URL_CONFIG),
                "schemaManagerConfig properties must define schema.registry.url");
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
}
