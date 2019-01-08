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

import java.util.Arrays;
import java.util.Properties;

import kafka.utils.ZKStringSerializer$;
import lombok.extern.slf4j.Slf4j;

import org.I0Itec.zkclient.ZkClient;

/**
 * This class is used by maven to start kafka stack in its own JVM.
 * This kafka stack will be used by the integration tests to run.
 * This helps speed up build times because we are not waiting for an instance of kafka to start and stop
 * between each integration test.
 */
@Slf4j
public class KafkaRunner {
    private static final int DEFAULT_ZK_SESSION_TIMEOUT_MS = 10 * 1000;
    private static final int DEFAULT_ZK_CONNECTION_TIMEOUT_MS = 8 * 1000;
    private static final String ZK_TEST_PORT = "21810";
    private static final Properties ZK_TEST_PROPS = createZkTestProps();

    private static EmbeddedKafkaStack EMBEDDED_KAFKA_SERVER = new EmbeddedKafkaStack(ZK_TEST_PROPS);
    private static ZkClient ZKCLIENT;

    public static void main(String[] args) {
        // SIGINT stop gracefully
        Runtime.getRuntime().addShutdownHook(new Thread(KafkaRunner::stopRunner));

        startRunner();
    }

    private static Properties createZkTestProps() {
        Properties props = new Properties();
        props.setProperty("zookeeper.test.port", ZK_TEST_PORT);
        return props;
    }

    private static ZkClient createZkClient() {
        return new ZkClient(EMBEDDED_KAFKA_SERVER.zookeeperConnect(),
                DEFAULT_ZK_SESSION_TIMEOUT_MS, DEFAULT_ZK_CONNECTION_TIMEOUT_MS,
                ZKStringSerializer$.MODULE$);
    }

    private static void startRunner() {
        log.info("Starting Kafka...");

        try {
            EMBEDDED_KAFKA_SERVER.start();

            ZKCLIENT = createZkClient();

            zkStoreUrl("kafka", EMBEDDED_KAFKA_SERVER.bootstrapServers());
            zkStoreUrl("schema-registry", EMBEDDED_KAFKA_SERVER.schemaRegistryUrl());

            log.info("Zookeeper started at well known address {}", EMBEDDED_KAFKA_SERVER.zookeeperConnect());
            log.info("Kafka broker started at {}", EMBEDDED_KAFKA_SERVER.bootstrapServers());
            log.info("Schema Registry started at {}", EMBEDDED_KAFKA_SERVER.schemaRegistryUrl());
        } catch (Exception exception) {
            log.error("Could not start kafka. Exiting.", exception);
            System.exit(1);
        }

        log.info("Kafka running.");
    }

    private static void zkStoreUrl(String service, String url) {
        String urlZkPath="/homeaway/test/"+service+"-key";
        ensureZkDirs("/homeaway", "/homeaway/test", urlZkPath);
        ZKCLIENT.writeData(urlZkPath, url);
        log.info("zkPath={} value={}", urlZkPath, url);
    }

    private static void ensureZkDirs(String... dirs) {
        Arrays.stream(dirs).forEach(KafkaRunner::ensureZkDir);
    }

    private static void ensureZkDir(String dir) {
        if (!ZKCLIENT.exists(dir)) {
            ZKCLIENT.createPersistent(dir);
        }
    }

    private static void stopRunner() {
        log.info("Stopping Kafka...");

        try {
            ZKCLIENT.close();
        } catch (Exception exception) {
            log.error("Could not close zkclient", exception);
        }
        EMBEDDED_KAFKA_SERVER.stop();

        log.info("Kafka stopped.");
    }
}
