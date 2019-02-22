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

import static org.hamcrest.core.Is.is;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.ws.rs.core.Response;

import lombok.extern.slf4j.Slf4j;

import com.google.common.base.Preconditions;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;

import org.apache.commons.io.FileUtils;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.homeaway.streamplatform.streamregistry.db.dao.impl.SourceDaoImpl;
import com.homeaway.streamplatform.streamregistry.model.Source;
import com.homeaway.streamplatform.streamregistry.model.SourceType;

@Slf4j
public class SourceDaoImplIT extends BaseResourceIT {


    //TODO: Document the FSM in a matrix for architecture

    // Takes longer for messages to show up in the consumer.
    public static final int SOURCE_WAIT_TIME_MS = 5000;

    public static Properties commonConfig;

    private static SourceResource sourceResource;

    @BeforeClass
    public static void setUp() throws Exception {

        // Make sure all temp dirs are cleaned first
        // This will solve a lot of the dir locked issue etc.
        FileUtils.deleteDirectory(new File(SourceDaoImpl.SOURCE_PROCESSOR_DIRNAME));

        commonConfig = new Properties();
        commonConfig.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        commonConfig.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryURL);
        commonConfig.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class.getName());
        commonConfig.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde.class.getName());


        createTopic(SourceDaoImpl.SOURCE_COMMANDS_TOPIC_NAME, 1, 1, new Properties());
        createTopic(SourceDaoImpl.SOURCE_ENTITY_TOPIC_NAME, 1, 1, new Properties());

        CompletableFuture<Boolean> initialized = new CompletableFuture<>();
        SourceDaoImpl sourceDao = new SourceDaoImpl(commonConfig, () -> initialized.complete(true));
        sourceDao.start();

        sourceResource = new SourceResource(sourceDao);

        log.info(
                "Waiting for processor's init method to be called (KV store created) before servicing the HTTP requests.");
        long timeoutTimestamp = System.currentTimeMillis() + TEST_STARTUP_TIMEOUT_MS;
        while (!initialized.isDone() && System.currentTimeMillis() <= timeoutTimestamp) {
            Thread.sleep(10); // wait some cycles before checking again
        }
        Preconditions.checkState(initialized.isDone(), "Did not receive state store initialized signal, aborting.");
        Preconditions.checkState(sourceDao.getSourceProcessor().state().isRunning(), "State store did not starting. Aborting.");
        log.info("Processor wait completed.");
    }

    @Test
    public void testInsert() throws InterruptedException {

        String sourceName = UUID.randomUUID().toString();
        String streamName = UUID.randomUUID().toString();

        Source source = buildSource(sourceName, streamName, null);

        //FIXME: Do this at the resource layer
        sourceResource.insert(source);

        Thread.sleep(SOURCE_WAIT_TIME_MS);
        log.info("waited - {} seconds", SOURCE_WAIT_TIME_MS);

        Response response = sourceResource.get(sourceName);

        Assert.assertNotNull(response.getEntity());

        Assert.assertThat("Get source should return the source that was inserted",
                (response.getEntity()).toString(), is(buildSource(sourceName, streamName, "UNASSIGNED").toString()));
    }


    @Test
    public void testUpdate() throws InterruptedException {

        String sourceName = UUID.randomUUID().toString();
        String streamName = UUID.randomUUID().toString();

        Source source = buildSource(sourceName, streamName, "UNASSIGNED");

        sourceResource.insert(source);

        Thread.sleep(SOURCE_WAIT_TIME_MS);
        log.info("waited - {} seconds", SOURCE_WAIT_TIME_MS);

        Response response = sourceResource.get(sourceName);

        Assert.assertNotNull(response.getEntity());

        Assert.assertThat("Get source should return the source that was inserted",
                (response.getEntity()).toString(), is(buildSource(sourceName, streamName, "UNASSIGNED").toString()));

        //FIXME: Add a test for updating state and return 400
        sourceResource.update(source);


        Thread.sleep(SOURCE_WAIT_TIME_MS);
        log.info("waited - {} seconds", SOURCE_WAIT_TIME_MS);

        Assert.assertThat(sourceResource.getStatus(sourceName).getEntity(), is("UNASSIGNED"));
    }


    @Test
    public void testStart() throws InterruptedException {


        String sourceName = UUID.randomUUID().toString();
        String streamName = UUID.randomUUID().toString();

        Source source = buildSource(sourceName, streamName, null);

        sourceResource.insert(source);

        // starting
        sourceResource.start(sourceName);

        Thread.sleep(SOURCE_WAIT_TIME_MS + 5000);

        Response response = sourceResource.getStatus(sourceName);
        System.out.println(response.getEntity().toString());
        Assert.assertThat(response.getEntity().toString(), is("TRANSITIONING"));
    }

    @Test
    public void testPause() throws InterruptedException {

        String sourceName = UUID.randomUUID().toString();
        String streamName = UUID.randomUUID().toString();

        Source source = buildSource(sourceName, streamName, null);

        sourceResource.insert(source);

        // pausing
        sourceResource.pause(sourceName);

        Thread.sleep(SOURCE_WAIT_TIME_MS);
        log.info("waited - {} seconds", SOURCE_WAIT_TIME_MS);

        Assert.assertThat(sourceResource.getStatus(sourceName), is("PAUSING"));
    }

    @Test
    public void testResume() throws InterruptedException {


        String sourceName = UUID.randomUUID().toString();
        String streamName = UUID.randomUUID().toString();

        Source source = buildSource(sourceName, streamName, null);

        sourceResource.insert(source);

        sourceResource.resume(sourceName);

        Thread.sleep(SOURCE_WAIT_TIME_MS);
        log.info("waited - {} seconds", SOURCE_WAIT_TIME_MS);

        Assert.assertThat(sourceResource.getStatus(sourceName), is("RESUMING"));
    }

    @Test
    public void testStop() throws InterruptedException {


        String sourceName = UUID.randomUUID().toString();
        String streamName = UUID.randomUUID().toString();

        Source source = buildSource(sourceName, streamName, null);

        sourceResource.insert(source);

        sourceResource.stop(sourceName);

        Thread.sleep(SOURCE_WAIT_TIME_MS);
        log.info("waited - {} seconds", SOURCE_WAIT_TIME_MS);

        Assert.assertThat(sourceResource.getStatus(sourceName), is("STOPPING"));
    }

    private Source buildSource(String sourceName, String streamName, String status) {

        Map<String, String> map = new HashMap<>();
        map.put("kinesis.url", "url");

        return Source.builder()
                .sourceName(sourceName)
                .streamName(streamName)
                .sourceType(SourceType.SOURCE_TYPES.get(0))
                .configuration(map)
                .status(status)
                .tags(map)
                .build();
    }
}