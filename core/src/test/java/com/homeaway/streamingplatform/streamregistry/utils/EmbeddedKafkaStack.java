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
package com.homeaway.streamingplatform.streamregistry.utils;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import kafka.server.KafkaConfig;
import kafka.server.KafkaConfig$;
import kafka.server.KafkaServer;
import kafka.utils.TestUtils;
import lombok.extern.slf4j.Slf4j;

import io.confluent.kafka.schemaregistry.avro.AvroCompatibilityLevel;
import io.confluent.kafka.schemaregistry.client.rest.RestService;
import io.confluent.kafka.schemaregistry.exceptions.SchemaRegistryException;
import io.confluent.kafka.schemaregistry.rest.SchemaRegistryConfig;
import io.confluent.kafka.schemaregistry.rest.SchemaRegistryRestApplication;
import io.confluent.kafka.schemaregistry.storage.SchemaRegistry;
import io.confluent.kafka.schemaregistry.zookeeper.SchemaRegistryIdentity;

import org.apache.curator.test.InstanceSpec;
import org.apache.curator.test.TestingServer;
import org.apache.kafka.common.network.ListenerName;
import org.apache.kafka.common.protocol.SecurityProtocol;
import org.apache.kafka.common.utils.Time;
import org.eclipse.jetty.server.Server;
import org.junit.rules.ExternalResource;
import org.junit.rules.TemporaryFolder;

@Slf4j
public class EmbeddedKafkaStack extends ExternalResource {

    private static final int DEFAULT_BROKER_PORT = 0; // 0 results in a random
                                                      // port being selected

    private static final String KAFKA_SCHEMAS_TOPIC = "_schemas";

    private static final String AVRO_COMPATIBILITY_TYPE = AvroCompatibilityLevel.BACKWARD_TRANSITIVE.name;

    private static final String LISTENERS_CONFIG = "listeners";

    private ZooKeeperEmbedded zookeeper;

    private KafkaEmbedded broker;

    private SchemaRegistryEmbedded schemaRegistry;

    private final Properties brokerConfig;

    public EmbeddedKafkaStack() {
        this(new Properties());
    }

    @SuppressWarnings("WeakerAccess")
    public EmbeddedKafkaStack(Properties brokerConfig) {
        this.brokerConfig = new Properties();
        this.brokerConfig.putAll(brokerConfig);
    }

    @SuppressWarnings("WeakerAccess")
    public void start() throws Exception {
        log.debug("Initiating embedded Kafka cluster startup");
        log.debug("Starting a ZooKeeper instance...");
        zookeeper = startZooKeeper();
        log.debug("ZooKeeper instance is running at {}", zookeeper.connectString());

        Properties effectiveBrokerConfig = effectiveBrokerConfigFrom(brokerConfig, zookeeper);
        log.debug("Starting a Kafka instance on port {} ...", effectiveBrokerConfig.getProperty(KafkaConfig$.MODULE$.PortProp()));
        broker = new KafkaEmbedded(effectiveBrokerConfig);
        log.debug("Kafka instance is running at {}, connected to ZooKeeper at {}", broker.brokerList(), broker.zookeeperConnect());

        Properties props = new Properties();
        int port = InstanceSpec.getRandomPort();

        props.put(LISTENERS_CONFIG, "http://127.0.0.1:" + port);

        schemaRegistry = new SchemaRegistryEmbedded(port, zookeeperConnect(), KAFKA_SCHEMAS_TOPIC, AVRO_COMPATIBILITY_TYPE, props);

        schemaRegistry.start();
    }

    private ZooKeeperEmbedded startZooKeeper() {
        try {
            String zkTestPortConfig = brokerConfig.getProperty("zookeeper.test.port");
            int zkTestPort = zkTestPortConfig != null ? Integer.valueOf(zkTestPortConfig) : -1;
            return new ZooKeeperEmbedded(zkTestPort);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not start Zookeeper", exception);
        }
    }

    private Properties effectiveBrokerConfigFrom(Properties brokerConfig, ZooKeeperEmbedded zookeeper) {
        Properties effectiveConfig = new Properties();
        effectiveConfig.putAll(brokerConfig);
        effectiveConfig.put(KafkaConfig$.MODULE$.ZkConnectProp(), zookeeper.connectString());
        effectiveConfig.put(KafkaConfig$.MODULE$.PortProp(), DEFAULT_BROKER_PORT);

        effectiveConfig.put(KafkaConfig$.MODULE$.DeleteTopicEnableProp(), true);
        effectiveConfig.put(KafkaConfig$.MODULE$.LogCleanerDedupeBufferSizeProp(), 2 * 1024 * 1024L);
        effectiveConfig.put(KafkaConfig$.MODULE$.GroupMinSessionTimeoutMsProp(), 0);
        effectiveConfig.put(KafkaConfig$.MODULE$.OffsetsTopicReplicationFactorProp(), (short) 1);
        effectiveConfig.put(KafkaConfig$.MODULE$.AutoCreateTopicsEnableProp(), true);
        return effectiveConfig;
    }

    @Override
    protected void before() throws Exception {
        start();
    }

    @Override
    protected void after() {
        stop();
    }

    @SuppressWarnings("WeakerAccess")
    public void stop() {
        try {
            if (schemaRegistry != null) {
                schemaRegistry.stop();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (broker != null) {
            broker.stop();
        }
        try {
            if (zookeeper != null) {
                zookeeper.stop();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String bootstrapServers() {
        return broker.brokerList();
    }

    public String zookeeperConnect() {
        return zookeeper.connectString();
    }

    public String schemaRegistryUrl() {
        return schemaRegistry.restConnect;
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    @Slf4j
    public static final class ZooKeeperEmbedded {

        private final TestingServer server;

        public ZooKeeperEmbedded() throws Exception {
            this(-1);
        }

        public ZooKeeperEmbedded(int port) throws Exception {
            log.debug("Starting embedded ZooKeeper server...");
            this.server = new TestingServer(port);
            log.debug("Embedded ZooKeeper server at {} uses the temp directory at {}", server.getConnectString(),
                    server.getTempDirectory());
        }

        public void stop() throws IOException {
            log.debug("Shutting down embedded ZooKeeper server at {} ...", server.getConnectString());
            server.close();
            log.debug("Shutdown of embedded ZooKeeper server at {} completed", server.getConnectString());
        }

        public String connectString() {
            return server.getConnectString();
        }

        @SuppressWarnings("unused")
        public String hostname() {
            return connectString().substring(0, connectString().lastIndexOf(':'));
        }
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    @Slf4j
    public static final class SchemaRegistryEmbedded {

        public final Properties prop;

        public RestService restClient;

        public SchemaRegistryRestApplication restApp;

        public Server restServer;

        public String restConnect;

        public SchemaRegistryEmbedded(int port, String zkConnect, String kafkaTopic) {
            this(port, zkConnect, kafkaTopic, AvroCompatibilityLevel.NONE.name, null);
        }

        public SchemaRegistryEmbedded(int port, String zkConnect, String kafkaTopic, String compatibilityType,
            Properties schemaRegistryProps) {
            this(port, zkConnect, null, kafkaTopic, compatibilityType, true, schemaRegistryProps);
        }

        public SchemaRegistryEmbedded(int port, String zkConnect, String kafkaTopic, String compatibilityType, boolean masterEligibility,
            Properties schemaRegistryProps) {
            this(port, zkConnect, null, kafkaTopic, compatibilityType, masterEligibility, schemaRegistryProps);
        }

        public SchemaRegistryEmbedded(int port, String zkConnect, String bootstrapBrokers, String kafkaTopic, String compatibilityType,
            boolean masterEligibility, Properties schemaRegistryProps) {
            prop = new Properties();
            if (schemaRegistryProps != null) {
                prop.putAll(schemaRegistryProps);
            }
            prop.setProperty(SchemaRegistryConfig.PORT_CONFIG, Integer.toString(port));
            if (zkConnect != null) {
                prop.setProperty(SchemaRegistryConfig.KAFKASTORE_CONNECTION_URL_CONFIG, zkConnect);
            }
            if (bootstrapBrokers != null) {
                prop.setProperty(SchemaRegistryConfig.KAFKASTORE_BOOTSTRAP_SERVERS_CONFIG, bootstrapBrokers);
            }
            prop.put(SchemaRegistryConfig.KAFKASTORE_TOPIC_CONFIG, kafkaTopic);
            prop.put(SchemaRegistryConfig.COMPATIBILITY_CONFIG, compatibilityType);
            prop.put(SchemaRegistryConfig.MASTER_ELIGIBILITY, masterEligibility);
        }

        public void start() throws Exception {
            log.debug("Starting embedded schema registry...");

            restApp = new SchemaRegistryRestApplication(prop);
            restServer = restApp.createServer();
            restServer.start();
            restConnect = restServer.getURI().toString();
            if (restConnect.endsWith("/"))
                restConnect = restConnect.substring(0, restConnect.length() - 1);
            restClient = new RestService(restConnect);
            log.debug("Embedded schema registry running at {}", restConnect);
        }

        public void stop() throws Exception {
            log.debug("Stopping embedded schema registry...");

            restClient = null;
            if (restServer != null) {
                restServer.stop();
                restServer.join();
            }
        }

        public void addConfigs(Properties props) {
            prop.putAll(props);
        }

        public boolean isMaster() {
            return restApp.schemaRegistry().isMaster();
        }

        public void setMaster(SchemaRegistryIdentity schemaRegistryIdentity) throws SchemaRegistryException {
            restApp.schemaRegistry().setMaster(schemaRegistryIdentity);
        }

        public SchemaRegistryIdentity myIdentity() {
            return restApp.schemaRegistry().myIdentity();
        }

        public SchemaRegistryIdentity masterIdentity() {
            return restApp.schemaRegistry().masterIdentity();
        }

        public SchemaRegistry schemaRegistry() {
            return restApp.schemaRegistry();
        }
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    @Slf4j
    public static final class KafkaEmbedded {

        private static final String DEFAULT_ZK_CONNECT = "127.0.0.1:2181";

        private static final int DEFAULT_ZK_SESSION_TIMEOUT_MS = 10 * 1000;

        private static final int DEFAULT_ZK_CONNECTION_TIMEOUT_MS = 8 * 1000;

        private final Properties effectiveConfig;

        private final File logDir;

        private final TemporaryFolder tmpFolder;

        private final KafkaServer kafka;

        public KafkaEmbedded(Properties config) throws IOException {
            tmpFolder = new TemporaryFolder();
            tmpFolder.create();
            logDir = tmpFolder.newFolder();
            effectiveConfig = effectiveConfigFrom(config);

            KafkaConfig kafkaConfig = new KafkaConfig(effectiveConfig, true);
            log.debug("Starting embedded Kafka broker (with log.dirs={} and ZK ensemble at {}) ...", logDir, zookeeperConnect());
            kafka = TestUtils.createServer(kafkaConfig, Time.SYSTEM);
            log.debug("Startup of embedded Kafka broker at {} completed (with ZK ensemble at {}) ...", brokerList(), zookeeperConnect());
        }

        private Properties effectiveConfigFrom(Properties initialConfig) {
            Properties effectiveConfig = new Properties();
            effectiveConfig.put(KafkaConfig$.MODULE$.BrokerIdProp(), 0);
            effectiveConfig.put(KafkaConfig$.MODULE$.HostNameProp(), "127.0.0.1");
            effectiveConfig.put(KafkaConfig$.MODULE$.PortProp(), "9092");
            effectiveConfig.put(KafkaConfig$.MODULE$.NumPartitionsProp(), 1);
            effectiveConfig.put(KafkaConfig$.MODULE$.AutoCreateTopicsEnableProp(), true);
            effectiveConfig.put(KafkaConfig$.MODULE$.MessageMaxBytesProp(), 1000000);
            effectiveConfig.put(KafkaConfig$.MODULE$.ControlledShutdownEnableProp(), true);

            effectiveConfig.putAll(initialConfig);
            effectiveConfig.setProperty(KafkaConfig$.MODULE$.LogDirProp(), logDir.getAbsolutePath());
            return effectiveConfig;
        }

        public String brokerList() {
            return String.join(":", kafka.config().hostName(),
                Integer.toString(kafka.boundPort(ListenerName.forSecurityProtocol(SecurityProtocol.PLAINTEXT))));
        }

        public String zookeeperConnect() {
            return effectiveConfig.getProperty("zookeeper.connect", DEFAULT_ZK_CONNECT);
        }

        public void stop() {
            log.debug("Shutting down embedded Kafka broker at {} (with ZK ensemble at {}) ...", brokerList(), zookeeperConnect());
            kafka.shutdown();
            kafka.awaitShutdown();
            log.debug("Removing temp folder {} with logs.dir at {} ...", tmpFolder, logDir);
            tmpFolder.delete();
            log.debug("Shutdown of embedded Kafka broker at {} completed (with ZK ensemble at {}) ...", brokerList(), zookeeperConnect());
        }
    }
}