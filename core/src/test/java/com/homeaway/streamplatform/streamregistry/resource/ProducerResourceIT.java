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

import static com.homeaway.streamplatform.streamregistry.utils.JsonModelBuilder.TEST_COMPONENT_ID;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.core.Response;

import lombok.extern.slf4j.Slf4j;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.Assert;
import org.junit.Test;

import com.homeaway.streamplatform.streamregistry.model.Producer;
import com.homeaway.streamplatform.streamregistry.model.RegionStreamConfig;
import com.homeaway.streamplatform.streamregistry.model.Stream;
import com.homeaway.streamplatform.streamregistry.utils.JsonModelBuilder;
import com.homeaway.streamplatform.streamregistry.utils.StreamRegistryUtils.EntriesPage;

@SuppressWarnings("WeakerAccess")
@Slf4j
public class ProducerResourceIT extends BaseResourceIT {
    public static final String PRODUCER_CLUSTER = "us-east-1_clustergeneral_other_producer";

    @Test
    public void test_put_producer() throws InterruptedException {
        String streamName = "junit-stream-put-producer-1";
        String producerName = "newProducer";
        Stream stream = JsonModelBuilder.buildJsonStream(streamName);

        // Delete the stream before starting the test-case
        streamResource.deleteStream(streamName);
        Thread.sleep(TEST_SLEEP_WAIT_MS); // wait so that the Topology can process it and materialize to KV-Store

        //Step 1: PUT a stream
        streamResource.upsertStream(streamName, stream);
        Thread.sleep(TEST_SLEEP_WAIT_MS); // wait so that the Topology can process it and materialize to KV-Store

        // Step 2: Validate "Producer Not Found" for a Stream.
        Response producer = producerResource.getProducer(streamName, producerName);
        Assert.assertEquals("Producer not found " + producerName, (producer.getEntity()));

        // Step 3: PUT a producer
        producerResource.upsertProducer(streamName, producerName, US_EAST_REGION);

        // Step 4: Verify the producer
        Thread.sleep(TEST_SLEEP_WAIT_MS); // wait so that the Topology can process it and materialize to KV-Store
        producer = producerResource.getProducer(streamName, producerName);

        Map<String, String> streamConfigMap = new HashMap<>();
        streamConfigMap.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        streamConfigMap.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryURL);

        RegionStreamConfig regionStreamConfig = RegionStreamConfig
            .builder().region(US_EAST_REGION)
            .streamConfiguration(streamConfigMap)
            .topics(Collections.singletonList(streamName))
            .cluster(US_EAST_CLUSTER_NAME)
            .build();
        Producer producerModel = JsonModelBuilder.buildJsonProducer(producerName, Collections.singletonList(regionStreamConfig));

        Producer producerEntity = (Producer) producer.getEntity();

        Assert.assertEquals(producerModel.toString(), producerEntity.toString());
    }

    @Test
    public void test_producer_registration_response() throws InterruptedException {
        String streamName = "junit-stream-producer-registration-response-1";
        String producerName = "P1";
        Stream stream = JsonModelBuilder.buildJsonStream(streamName);

        streamResource.upsertStream(streamName, stream);
        Thread.sleep(TEST_SLEEP_WAIT_MS);

        producerResource.upsertProducer(streamName, producerName, US_EAST_REGION);
        Thread.sleep(TEST_SLEEP_WAIT_MS);
        Response producerResponse = producerResource.getProducer(streamName, producerName);

        Assert.assertEquals(streamName, ((Stream) streamResource.getStream(streamName).getEntity()).getName());
        Assert.assertEquals(1, ((Producer) producerResponse.getEntity()).getRegionStreamConfigList().size());

        // double registration
        streamResource.upsertStream(streamName, stream);
        Thread.sleep(TEST_SLEEP_WAIT_MS);
        producerResource.upsertProducer(streamName, producerName, US_EAST_REGION);
        Thread.sleep(TEST_SLEEP_WAIT_MS);
        producerResponse = producerResource.getProducer(streamName, producerName);

        Assert.assertEquals(1, ((Producer) producerResponse.getEntity()).getRegionStreamConfigList().size());
    }

    @Test
    public void test_other_producer_registration_response() throws InterruptedException {
        String streamName = "junit-stream-other-producer-registration-response";
        String producerName = "OTHER1";

        Stream stream = JsonModelBuilder.buildJsonStream(streamName, JsonModelBuilder.TEST_PRODUCT_ID, Optional.of(TEST_COMPONENT_ID), OTHER_HINT);

        //Step 1: PUT a stream
        streamResource.upsertStream(streamName, stream);
        Thread.sleep(TEST_SLEEP_WAIT_MS);

        // Step 2: Upsert a producer
        producerResource.upsertProducer(streamName, producerName, US_EAST_REGION);
        Thread.sleep(TEST_SLEEP_WAIT_MS);

        Assert.assertEquals(streamName, ((Stream) streamResource.getStream(streamName).getEntity()).getName());
        // Step 3: Verify the producer (OTHER)
        Response producerResponse = producerResource.getProducer(streamName, producerName);

        Assert.assertEquals(1, ((Producer) producerResponse.getEntity()).getRegionStreamConfigList().size());
        Assert.assertEquals(PRODUCER_CLUSTER, ((Producer) producerResponse.getEntity()).getRegionStreamConfigList().get(0).getCluster());
        Assert.assertEquals(streamName, ((Producer) producerResponse.getEntity()).getRegionStreamConfigList().get(0).getTopics().get(0));
        Assert.assertEquals(bootstrapServers, ((Producer) producerResponse.getEntity()).getRegionStreamConfigList().get(0).getStreamConfiguration().get("bootstrap.servers"));
        Assert.assertEquals(schemaRegistryURL, ((Producer) producerResponse.getEntity()).getRegionStreamConfigList().get(0).getStreamConfiguration().get("schema.registry.url"));
    }

    @Test
    public void test_put_producer_invalid_region() throws InterruptedException {
        String streamName = "junit-stream-invalid-region-1";
        String producerName = "P2";
        Stream stream = JsonModelBuilder.buildJsonStream(streamName);

        streamResource.upsertStream(streamName, stream);
        Thread.sleep(TEST_SLEEP_WAIT_MS*2);

        Assert.assertEquals(streamName, ((Stream) streamResource.getStream(streamName).getEntity()).getName());

        String invalidRegion = "invalid-region";
        Response response = producerResource.upsertProducer(streamName, producerName, invalidRegion);

        Assert.assertEquals(Response.Status.PRECONDITION_FAILED.getStatusCode(), response.getStatus());
    }

    @Test
    public void test_put_producer_with_no_stream() {
        String streamName = "junit-stream-put-producer-with-no-stream";
        String producerName = "P1";

        Response response = producerResource.upsertProducer(streamName, producerName, US_EAST_REGION);
        Assert.assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        Assert.assertEquals("Stream not found " + streamName, (response.getEntity()));
    }

    @Test
    public void test_put_producer_with_unsupported_region() throws InterruptedException {
        String streamName = "junit-stream-unsupported-region-1";
        String producerName = "P1";
        String unsupported_region = "ap-southeast-1-vpc-3f07915b";

        Stream stream = JsonModelBuilder.buildJsonStream(streamName);
        streamResource.upsertStream(streamName, stream);
        Thread.sleep(TEST_SLEEP_WAIT_MS);

        Response response = producerResource.upsertProducer(streamName, producerName, unsupported_region);
        Thread.sleep(TEST_SLEEP_WAIT_MS);

        Assert.assertEquals(Response.Status.PRECONDITION_FAILED.getStatusCode(), response.getStatus());
    }

    @Test
    public void test_delete_producer() throws InterruptedException {
        String streamName = "junit-stream-delete-producer";
        String producerName = "P1";
        Stream stream = JsonModelBuilder.buildJsonStream(streamName);

        streamResource.upsertStream(streamName, stream);
        Thread.sleep(TEST_SLEEP_WAIT_MS); // wait so that the Topology can process it and materialize to KV-Store

        producerResource.upsertProducer(streamName, producerName, US_EAST_REGION);
        Thread.sleep(TEST_SLEEP_WAIT_MS);

        Response response = producerResource.deleteProducer(streamName, producerName);
        Assert.assertEquals("Producer deleted " + producerName, (response.getEntity()));

        Thread.sleep(TEST_SLEEP_WAIT_MS);
        response = producerResource.getProducer(streamName, producerName);
        Assert.assertEquals("Producer not found " + producerName, (response.getEntity()));
    }

    @Test
    public void test_delete_producer_with_no_stream() {
        String streamName = "junit-stream-delete-producer-with-no-stream";
        String producerName = "P1";

        Response response = producerResource.deleteProducer(streamName, producerName);
        Assert.assertEquals("Stream not found "+streamName, response.getEntity());
    }

    @Test
    public void test_delete_invalid_producer_in_valid_stream() throws InterruptedException {
        String streamName = "junit-stream-delete-invalid-producer-invalid-stream";
        String producerName = "P1";
        String invalidProducerName = "invalid-producer";

        Stream stream = JsonModelBuilder.buildJsonStream(streamName);

        Response response = streamResource.upsertStream(streamName, stream);
        assertThat(response.getStatus(), is(202));
        Thread.sleep(TEST_SLEEP_WAIT_MS);

        response = producerResource.upsertProducer(streamName, producerName, US_EAST_REGION);
        assertThat(response.getStatus(), is(200));
        Thread.sleep(TEST_SLEEP_WAIT_MS);

        response = producerResource.deleteProducer(streamName, invalidProducerName);
        assertThat(response.getStatus(), is(404));
        Assert.assertEquals("Producer not found "+invalidProducerName, response.getEntity());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void test_getAllStreams_withMoreProducers() throws InterruptedException {
        String streamName1 = "junit-all-stream-more-producer-1";
        String streamName2 = "junit-all-stream-more-producer-2";
        String producerName1 = "P1";
        String producerName2 = "P2";

        Stream stream1 = JsonModelBuilder.buildJsonStream(streamName1);
        Stream stream2 = JsonModelBuilder.buildJsonStream(streamName2);

        streamResource.upsertStream(streamName1, stream1);
        streamResource.upsertStream(streamName2, stream2);
        Thread.sleep(TEST_SLEEP_WAIT_MS);

        producerResource.upsertProducer(streamName1, producerName1, US_EAST_REGION);
        producerResource.upsertProducer(streamName2, producerName2, US_EAST_REGION);
        Thread.sleep(TEST_SLEEP_WAIT_MS);

        // Two streams should be in the first 10 elements of page 0
        Response response = streamResource.getAllStreams(Optional.of(0), Optional.of(10));

        List<Stream> page = ((EntriesPage<Stream>) response.getEntity()).getEntries();
        page.forEach(stream -> log.debug("Stream name={}", stream.getName()));
        Assert.assertTrue(2 <= page.size());

        response = producerResource.getAllProducers(streamName1);

        Map<String, String> streamConfigMap = new HashMap<>();
        streamConfigMap.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        // SchemaReg URL should be always pointing to Austin Region.
        streamConfigMap.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryURL);

        RegionStreamConfig regionStreamConfig1 = RegionStreamConfig
            .builder().region(US_EAST_REGION)
            .cluster(US_EAST_CLUSTER_NAME)
            .streamConfiguration(streamConfigMap)
            .topics(Collections.singletonList(streamName1))
            .build();
        Producer producerModel1 = JsonModelBuilder.buildJsonProducer(producerName1, Collections.singletonList(regionStreamConfig1));

        Assert.assertTrue(1 <= ((List<Producer>) response.getEntity()).size());
        Assert.assertEquals(producerModel1.toString(), ((List<Producer>)response.getEntity()).get(0).toString());

        response = producerResource.getAllProducers(streamName2);

        RegionStreamConfig regionStreamConfig2 = RegionStreamConfig
            .builder().region(US_EAST_REGION)
            .cluster(US_EAST_CLUSTER_NAME)
            .streamConfiguration(streamConfigMap)
            .topics(Collections.singletonList(streamName2))
            .build();
        Producer producerModel2 = JsonModelBuilder.buildJsonProducer(producerName2, Collections.singletonList(regionStreamConfig2));
        Assert.assertTrue(1 <= ((List<Producer>) response.getEntity()).size());
        Assert.assertEquals(producerModel2.toString(), ((List<Producer>)response.getEntity()).get(0).toString());
    }

}