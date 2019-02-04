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
package com.homeaway.streamplatform.streamregistry.resource;

import java.io.File;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

import javax.validation.Validator;
import javax.ws.rs.client.Client;

import kafka.admin.AdminUtils;
import kafka.admin.RackAwareMode;
import kafka.utils.ZKStringSerializer$;
import kafka.utils.ZkUtils;
import lombok.extern.slf4j.Slf4j;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroDeserializer;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;
import org.apache.commons.io.FileUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.streams.StreamsConfig;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.mockito.Mockito;

import com.homeaway.digitalplatform.streamregistry.AvroStream;
import com.homeaway.digitalplatform.streamregistry.AvroStreamKey;
import com.homeaway.digitalplatform.streamregistry.ClusterKey;
import com.homeaway.digitalplatform.streamregistry.ClusterValue;
import com.homeaway.streamplatform.streamregistry.StreamRegistryApplication;
import com.homeaway.streamplatform.streamregistry.configuration.KafkaProducerConfig;
import com.homeaway.streamplatform.streamregistry.configuration.KafkaStreamsConfig;
import com.homeaway.streamplatform.streamregistry.configuration.StreamRegistryConfiguration;
import com.homeaway.streamplatform.streamregistry.configuration.TopicsConfig;
import com.homeaway.streamplatform.streamregistry.db.dao.AbstractDao;
import com.homeaway.streamplatform.streamregistry.db.dao.KafkaManager;
import com.homeaway.streamplatform.streamregistry.db.dao.RegionDao;
import com.homeaway.streamplatform.streamregistry.db.dao.SourceDao;
import com.homeaway.streamplatform.streamregistry.db.dao.StreamClientDao;
import com.homeaway.streamplatform.streamregistry.db.dao.StreamDao;
import com.homeaway.streamplatform.streamregistry.db.dao.impl.ConsumerDaoImpl;
import com.homeaway.streamplatform.streamregistry.db.dao.impl.KafkaManagerImpl;
import com.homeaway.streamplatform.streamregistry.db.dao.impl.ProducerDaoImpl;
import com.homeaway.streamplatform.streamregistry.db.dao.impl.RegionDaoImpl;
import com.homeaway.streamplatform.streamregistry.db.dao.impl.SourceDaoImpl;
import com.homeaway.streamplatform.streamregistry.db.dao.impl.StreamDaoImpl;
import com.homeaway.streamplatform.streamregistry.extensions.schema.SchemaManager;
import com.homeaway.streamplatform.streamregistry.extensions.validation.StreamValidator;
import com.homeaway.streamplatform.streamregistry.extensions.validator.StreamValidatorIT;
import com.homeaway.streamplatform.streamregistry.health.StreamRegistryHealthCheck;
import com.homeaway.streamplatform.streamregistry.model.Consumer;
import com.homeaway.streamplatform.streamregistry.model.Producer;
import com.homeaway.streamplatform.streamregistry.provider.InfraManager;
import com.homeaway.streamplatform.streamregistry.streams.GlobalKafkaStore;
import com.homeaway.streamplatform.streamregistry.streams.StreamRegistryProducer;

@SuppressWarnings("WeakerAccess")
@Slf4j
public class BaseResourceIT {

    public static final String US_EAST_REGION = "us-east-1-vpc-defa0000";

    public static final String US_EAST_CLUSTER_NAME = "us-east-1_cluster001";

    public static final String US_EAST_CLUSTER_GENERAL = "us-east-1_clustergeneral";

    public static final String US_WEST_REGION = "us-west-2-vpc-0000cafe";

    public static final String APPLICATIONID = "application.id";

    public static final String PRODUCER = "producer";

    public static final String CONSUMER = "consumer";

    public static final String ENV_TEST = "test";

    public static final String KEY_SERDE = "key.serde";

    public static final String VALUE_SERDE = "value.serde";

    public static final String REPLICATION_FACTOR = "replication.factor";

    public static final String NO_OF_PARTITIONS = "num.partitions";

    public static final String SOME_HINT = "some-alias";

    public static final String OTHER_HINT = "other-alias";

    /** wait for this amount before timeout of test */
    protected static final int TEST_STARTUP_TIMEOUT_MS = 10000;

    // TODO: Make resources "consistent" by having all writes (mutations)
    //       wait some timeout period for the processor to process
    // THIS IS A TEMPORARY WORKAROUND for now... centralizing here so that we can soon remove it
    protected static final int TEST_SLEEP_WAIT_MS = 80;

    protected static StreamRegistryProducer streamProducer;

    protected final static File streamsDirectory = new File("/tmp/streams");

    protected final static File sourcesDirectory = new File("/tmp/sources");

    protected static StreamRegistryProducer sourceProducer;

    protected static GlobalKafkaStore streamProcessor;

    protected static GlobalKafkaStore sourceProcessor;

    protected static StreamResource streamResource;

    protected static ConsumerResource consumerResource;

    protected static SourceResource sourceResource;

    protected static ProducerResource producerResource;

    protected static InfraManager infraManager;

    protected static StreamRegistryHealthCheck healthCheck;

    protected static RegionDao regionDao;

    protected static Client client;

    protected static TopicsConfig topicsConfig;

    protected static Properties producerConfig;

    protected static Properties consumerConfig;

    protected static Properties streamsConfig;

    @SuppressWarnings("unused")
    protected static Properties infraStreamsConfig;

    protected static String zookeeperQuorum;

    protected static String bootstrapServers;

    protected static String schemaRegistryURL;

    public static StreamRegistryConfiguration configuration;

    private static ZkClient ZKCLIENT;

    private static final int DEFAULT_ZK_SESSION_TIMEOUT_MS = 10 * 1000;

    private static final int DEFAULT_ZK_CONNECTION_TIMEOUT_MS = 8 * 1000;

    public static void createTopic(String topic, int partitions, int replication, Properties topicConfig) {
        log.debug("Creating topic { name: {}, partitions: {}, replication: {}, config: {} }",
                topic, partitions, replication, topicConfig);
        ZkUtils zkUtils = new ZkUtils(ZKCLIENT, new ZkConnection(zookeeperQuorum), false);
        if (!AdminUtils.topicExists(zkUtils, topic)) {
            AdminUtils.createTopic(zkUtils, topic, partitions, replication, topicConfig, RackAwareMode.Enforced$.MODULE$);
        }
    }

    // TODO - consider catching checked exceptions and throwing appropriate unchecked exceptions
    // TODO - Why do we start and stop kstreams on each integration test ? Shouldn't this be
    //      - performed during the pre-integration-test phase ONCE?
    //      - Doing so saves on build time.
    @BeforeClass
    public static void setupApplication() throws Exception {

        FileUtils.deleteDirectory(streamsDirectory);
        FileUtils.deleteDirectory(sourcesDirectory);

        // static test config setup during pre-integration-test mvn phase
        zookeeperQuorum = "127.0.0.1:21810";
        initializeZkClient();
        bootstrapServers = getTestUrl("/homeaway/test/kafka-key");
        schemaRegistryURL = getTestUrl("/homeaway/test/schema-registry-key");

        loadConfig("config-dev.yaml");
        topicsConfig = configuration.getTopicsConfig();
        String producerTopic = topicsConfig.getProducerTopic();
        String streamSourceTopic = topicsConfig.getStreamSourceTopic();

        try {
            createTopic(producerTopic, 1, 1, new Properties());
            createTopic(streamSourceTopic, 1, 1, new Properties());
        } catch (Exception exception) {
            throw new IllegalStateException("Could not create topic " + producerTopic, exception);
        }

        infraManager = buildInfraManager();

        producerConfig = new Properties();

        producerConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        producerConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        producerConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        producerConfig.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryURL);
        
        streamProducer = new StreamRegistryProducer<>(producerConfig, BaseResourceIT.topicsConfig.getProducerTopic());
        sourceProducer = new StreamRegistryProducer<>(producerConfig, BaseResourceIT.topicsConfig.getStreamSourceTopic());

        streamsConfig = new Properties();
        KafkaStreamsConfig kafkaStreamsConfig = configuration.getKafkaStreamsConfig();

        streamsConfig.put(StreamsConfig.APPLICATION_ID_CONFIG, kafkaStreamsConfig.getKstreamsProperties().get(APPLICATIONID));
        streamsConfig.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        streamsConfig.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, kafkaStreamsConfig.getKstreamsProperties().get(KEY_SERDE));
        streamsConfig.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, kafkaStreamsConfig.getKstreamsProperties().get(VALUE_SERDE));
        streamsConfig.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryURL);
        CompletableFuture<Boolean> initialized = new CompletableFuture<>();

        Properties streamProperties = new Properties();
        streamProperties.putAll(streamsConfig);
        streamProperties.put(StreamsConfig.STATE_DIR_CONFIG, streamsDirectory.getPath());

        streamProcessor = new GlobalKafkaStore<>(streamProperties, BaseResourceIT.topicsConfig.getProducerTopic(),
                topicsConfig.getProducerStateStore(), () -> initialized.complete(true));


        Properties sourceProperties = new Properties();
        sourceProperties.putAll(streamsConfig);
        sourceProperties.put("application.id", "source-kstreams-application.id");
        sourceProperties.put(StreamsConfig.STATE_DIR_CONFIG, sourcesDirectory.getPath());

        sourceProcessor = new GlobalKafkaStore<>(sourceProperties, BaseResourceIT.topicsConfig.getStreamSourceTopic(),
                topicsConfig.getStreamSourceStateStore(), () -> initialized.complete(true));

        consumerConfig = new Properties();
        consumerConfig.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        consumerConfig.put(ConsumerConfig.GROUP_ID_CONFIG, "schemaregistry-test-consumer");
        consumerConfig.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, SpecificAvroDeserializer.class);
        consumerConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, SpecificAvroDeserializer.class);
        consumerConfig.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryURL);

        log.info(
            "sleep started. Sleep needed so that the processor's init method is called (KV store created) before servicing the HTTP requests.");
        long timeoutTimestamp = System.currentTimeMillis() + TEST_STARTUP_TIMEOUT_MS;
        while (!initialized.isDone() && System.currentTimeMillis() <= timeoutTimestamp) {
            Thread.sleep(10); // wait some cycles before checking again
        }
        Preconditions.checkState(System.currentTimeMillis() <= timeoutTimestamp,
            "Did not receive state store initialized signal, aborting.");
        log.info("sleep completed.");

        KafkaManager kafkaManager = new KafkaManagerImpl();
        String env = configuration.getEnv();
        regionDao = new RegionDaoImpl(env, infraManager);

        client = Mockito.mock(Client.class);
        StreamValidatorIT.mockHttpClientSuccess(client);

        StreamValidator streamValidator = StreamRegistryApplication.loadValidator(configuration, client, regionDao);

        configuration.getSchemaManagerConfig().getProperties().put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryURL);
        SchemaManager schemaManager = StreamRegistryApplication.loadSchemaManager(configuration);

        StreamDao streamDao = new StreamDaoImpl(streamProducer, streamProcessor, env, regionDao,
            infraManager, kafkaManager, streamValidator, schemaManager);
        StreamClientDao<Producer> producerDao = new ProducerDaoImpl(streamProducer, streamProcessor, env, regionDao,
            infraManager, kafkaManager);
        StreamClientDao<Consumer> consumerDao = new ConsumerDaoImpl(streamProducer, streamProcessor, env, regionDao,
            infraManager, kafkaManager);
        SourceDao sourceDao = new SourceDaoImpl(sourceProducer, sourceProcessor);

        streamResource = new StreamResource(streamDao, producerDao, consumerDao, sourceDao);
        producerResource = new ProducerResource(streamDao, producerDao);
        consumerResource = new ConsumerResource(streamDao, consumerDao);
        sourceResource = new SourceResource(sourceDao);

        SchemaRegistryClient schemaRegistryClient = new CachedSchemaRegistryClient(schemaRegistryURL, 1);
        schemaRegistryClient.register(producerTopic + "-key", AvroStreamKey.SCHEMA$);
        schemaRegistryClient.register(producerTopic + "-value", AvroStream.SCHEMA$);

        healthCheck = new StreamRegistryHealthCheck(streamProcessor, streamResource, new MetricRegistry(), 1, US_EAST_REGION);
    }

    /** initializes the zkClient to load up the test urls */
    private static void initializeZkClient() {
        ZKCLIENT = new ZkClient(zookeeperQuorum, DEFAULT_ZK_SESSION_TIMEOUT_MS,
                DEFAULT_ZK_CONNECTION_TIMEOUT_MS, ZKStringSerializer$.MODULE$);
    }

    /** gets the pre-configured test url for this IT test. This is required in pre-integration-test suite. */
    private static String getTestUrl(String testUrlKeyPath) {
        return ZKCLIENT.readData(testUrlKeyPath);
    }

    /**
     * Populate the Infra Manager with cluster key and value
     *
     */
    private static InfraManagerImplStub buildInfraManager() {
        InfraManagerImplStub infraManagerImplStub = new InfraManagerImplStub();
        // Inserting the Primary Cluster
        ClusterKey clusterKey = new ClusterKey(US_EAST_REGION, ENV_TEST, AbstractDao.PRIMARY_HINT, null);
        final ImmutableMap<String, String> clusterPropertiesMap = new ImmutableMap.Builder<String, String>()
            .put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
            .put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryURL)
            .put(AbstractDao.CLUSTER_NAME, US_EAST_CLUSTER_NAME)
            .put(KafkaProducerConfig.ZOOKEEPER_QUORUM, zookeeperQuorum)
            .build();
        infraManagerImplStub.addCluster(clusterKey,  new ClusterValue(clusterPropertiesMap));

        // inserting another cluster with SOME_HINT
        ClusterKey motClusterKey = new ClusterKey(US_WEST_REGION, ENV_TEST, SOME_HINT, null);
        final ImmutableMap<String, String> motclusterPropertiesMap = new ImmutableMap.Builder<String, String>()
            .put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
            .put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryURL)
            .put(AbstractDao.CLUSTER_NAME, US_EAST_CLUSTER_GENERAL)
            .put(KafkaProducerConfig.ZOOKEEPER_QUORUM, zookeeperQuorum)
            .build();
        infraManagerImplStub.addCluster(motClusterKey,  new ClusterValue(motclusterPropertiesMap));

        // Inserting third cluster with OTHER_HINT "producer cluster"
        ClusterKey otherProducerClusterKey = new ClusterKey(US_EAST_REGION, ENV_TEST, OTHER_HINT, PRODUCER);
        final ImmutableMap<String, String> otherProducerClusterPropertiesMap = new ImmutableMap.Builder<String, String>()
            .put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
            .put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryURL)
            .put(AbstractDao.CLUSTER_NAME, US_EAST_CLUSTER_GENERAL+"_other_producer")
            .put(KafkaProducerConfig.ZOOKEEPER_QUORUM, zookeeperQuorum)
            .build();
        infraManagerImplStub.addCluster(otherProducerClusterKey,  new ClusterValue(otherProducerClusterPropertiesMap));

        // Inserting fourth cluster with OTHER_HINT "consumer cluster"
        ClusterKey otherConsumerClusterKey = new ClusterKey(US_EAST_REGION, ENV_TEST, OTHER_HINT, CONSUMER);
        final ImmutableMap<String, String> otherConsumerClusterPropertiesMap = new ImmutableMap.Builder<String, String>()
            .put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
            .put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryURL)
            .put(AbstractDao.CLUSTER_NAME, US_EAST_CLUSTER_GENERAL+"_other_consumer")
            .put(KafkaProducerConfig.ZOOKEEPER_QUORUM, zookeeperQuorum)
            .build();
        infraManagerImplStub.addCluster(otherConsumerClusterKey,  new ClusterValue(otherConsumerClusterPropertiesMap));

        return infraManagerImplStub;
    }

    @SuppressWarnings("SameParameterValue")
    private static void loadConfig(String filename) throws java.io.IOException,
            io.dropwizard.configuration.ConfigurationException {
        final ObjectMapper objectMapper = Jackson.newObjectMapper();
        final Validator validator = Validators.newValidator();
        final YamlConfigurationFactory<StreamRegistryConfiguration> factory =
            new YamlConfigurationFactory<>(StreamRegistryConfiguration.class, validator, objectMapper, "dw");

        final File yaml = new File(Objects.requireNonNull(Thread.currentThread()
                .getContextClassLoader()
                .getResource(filename))
                .getPath());
        configuration = factory.build(yaml);
    }

    // TODO Why do we start and stop kstreams on each integration test ? Shouldn't this be part of the server
    @AfterClass
    public static void tearDown() throws Exception {
        streamProcessor.stop();
        streamProcessor.getStreams().cleanUp();
        sourceProcessor.stop();
        streamProcessor.getStreams().cleanUp();
        streamProducer.stop();
        sourceProducer.stop();
        infraManager.stop();
        ZKCLIENT.close();

        FileUtils.deleteDirectory(streamsDirectory);
        FileUtils.deleteDirectory(sourcesDirectory);


    }
}
