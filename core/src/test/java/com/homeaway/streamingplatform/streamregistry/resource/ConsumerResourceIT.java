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
package com.homeaway.streamingplatform.streamregistry.resource;

import static com.homeaway.streamingplatform.streamregistry.utils.JsonModelBuilder.TEST_COMPONENT_ID;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.core.Response;

import lombok.extern.slf4j.Slf4j;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.Assert;
import org.junit.Test;

import com.homeaway.streamingplatform.streamregistry.model.Consumer;
import com.homeaway.streamingplatform.streamregistry.model.RegionStreamConfig;
import com.homeaway.streamingplatform.streamregistry.model.Stream;
import com.homeaway.streamingplatform.streamregistry.utils.JsonModelBuilder;

@SuppressWarnings("WeakerAccess")
@Slf4j
public class ConsumerResourceIT extends BaseResourceIT {
    public static final String TEST_CONSUMER_CLUSTER = "us-east-1_clustergeneral_other_consumer";

    @Test
    public void test_put_consumer_without_stream() {
        String streamName = "junit-stream-put-consumer-without-stream";
        String consumerName = "C2";

        // delete stream if already exists.
        streamResource.deleteStream(streamName);

        // try to add consumer to a non-available stream, and expect NotFoundException
        Response response = consumerResource.upsertConsumer(streamName, consumerName, US_EAST_REGION);
        Assert.assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    public void test_put_consumer() throws InterruptedException {
        String streamName = "junit-stream-put-consumer";
        String consumerName = "C2";
        Stream stream = JsonModelBuilder.buildJsonStream(streamName);

        Response streamResponse = streamResource.upsertStream(streamName, stream);
        assertThat(streamResponse.getStatus(), is(202));
        Thread.sleep(TEST_SLEEP_WAIT_MS); // wait for operation to propagate

        consumerResource.upsertConsumer(streamName, consumerName, US_EAST_REGION);
        Thread.sleep(TEST_SLEEP_WAIT_MS); // wait for operation to propagate

        Response consumerResponse = consumerResource.getConsumer(streamName, consumerName);

        Map<String, String> streamConfigMap = new HashMap<>();
        streamConfigMap.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        streamConfigMap.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryURL);

        RegionStreamConfig regionStreamConfig1 = RegionStreamConfig
            .builder()
            .region(US_EAST_REGION)
            .cluster(US_EAST_CLUSTER_NAME)
            .topics(Arrays.asList(streamName, streamName+".global"))
            .streamConfiguration(streamConfigMap)
            .build();
        Consumer consumerModel = JsonModelBuilder.buildJsonConsumer(consumerName, Collections.singletonList(regionStreamConfig1));

        Assert.assertEquals(consumerModel.toString(), consumerResponse.getEntity().toString());
    }

    @Test
    public void test_consumer_registration_response() throws InterruptedException {
        String streamName = "junit-stream-consumer-registration-response";
        String consumerName = "C2";
        Stream stream = JsonModelBuilder.buildJsonStream(streamName);

        streamResource.upsertStream(streamName, stream);
        Thread.sleep(TEST_SLEEP_WAIT_MS); // wait for operation to propagate

        consumerResource.upsertConsumer(streamName, consumerName, US_EAST_REGION);

        Response streamResponse = streamResource.getStream(streamName);
        if(streamResponse.getEntity() instanceof String) {
            throw new IllegalStateException("Was expecting a Stream, got a string: " + streamResponse.getEntity());
        }
        Assert.assertEquals(streamName, ((Stream) streamResponse.getEntity()).getName());

        Thread.sleep(TEST_SLEEP_WAIT_MS); // wait for operation to propagate
        Response consumerResponse = consumerResource.getConsumer(streamName, consumerName);
        Assert.assertEquals(1, ((Consumer) consumerResponse.getEntity()).getRegionStreamConfigList().size());

        // double registration
        consumerResource.upsertConsumer(streamName, consumerName, US_EAST_REGION);

        Thread.sleep(TEST_SLEEP_WAIT_MS); // wait for operation to propagate
        consumerResponse = consumerResource.getConsumer(streamName, consumerName);
        Assert.assertEquals(1, ((Consumer) consumerResponse.getEntity()).getRegionStreamConfigList().size());
    }

    @Test
    public void test_other_consumer_registration_response() throws InterruptedException {
        String streamName = "junit-stream-other-consumer-registration-response";
        String consumerName = "C2";
        Stream stream = JsonModelBuilder.buildJsonStream(streamName, JsonModelBuilder.TEST_PRODUCT_ID, Optional.of(TEST_COMPONENT_ID), OTHER_HINT);

        Response streamResponse = streamResource.upsertStream(streamName, stream);
        assertThat(streamResponse.getStatus(), is(202));

        Thread.sleep(TEST_SLEEP_WAIT_MS); // wait for operation to propagate
        consumerResource.upsertConsumer(streamName, consumerName, US_EAST_REGION);

        Thread.sleep(TEST_SLEEP_WAIT_MS); // wait for operation to propagate
        streamResponse = streamResource.getStream(streamName);
        Assert.assertEquals(streamName, ((Stream) streamResponse.getEntity()).getName());
        Response consumerResponse = consumerResource.getConsumer(streamName, consumerName);
        Assert.assertEquals(1, ((Consumer) consumerResponse.getEntity()).getRegionStreamConfigList().size());
        Assert.assertEquals(TEST_CONSUMER_CLUSTER, ((Consumer) consumerResponse.getEntity()).getRegionStreamConfigList().get(0).getCluster());
    }

    @Test
    public void test_put_consumer_invalid_region() throws InterruptedException {
        String streamName = "junit-stream-put-invalid-region";
        String consumerName = "C2";
        Stream stream = JsonModelBuilder.buildJsonStream(streamName);

        streamResource.upsertStream(streamName, stream);
        Thread.sleep(TEST_SLEEP_WAIT_MS); // wait for operation to propagate

        String invalidRegion="invalid-region";

        Response response = consumerResource.upsertConsumer(streamName, consumerName, invalidRegion);

        Assert.assertEquals(Response.Status.PRECONDITION_FAILED.getStatusCode(), response.getStatus());
    }

    @Test
    public void test_put_consumer_with_no_stream() {
        String streamName = "junit-stream-consumer-with-no-stream";
        String consumerName = "C1";

        Response response = consumerResource.upsertConsumer(streamName, consumerName, US_EAST_REGION);
        Assert.assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        Assert.assertEquals("Stream not found " + streamName, (response.getEntity()));
    }

    @Test
    public void test_put_consumer_with_unsupported_region() throws InterruptedException {
        String streamName = "junit-stream-put-consumer-with-unsupported-region";
        String consumerName = "C2";
        String unsupported_region = "ap-southeast-1-vpc-3f07915b";

        Stream stream = JsonModelBuilder.buildJsonStream(streamName);
        streamResource.upsertStream(streamName, stream);

        Thread.sleep(TEST_SLEEP_WAIT_MS); // wait for operation to propagate
        Response response = consumerResource.upsertConsumer(streamName, consumerName, unsupported_region);

        Assert.assertEquals(Response.Status.PRECONDITION_FAILED.getStatusCode(), response.getStatus());
    }

    @Test
    public void test_delete_consumer() throws InterruptedException {
        String streamName = "junit-stream-delete-consumer";
        String consumerName = "C1";
        Stream stream = JsonModelBuilder.buildJsonStream(streamName);

        Response response = streamResource.upsertStream(streamName, stream);
        assertThat(response.getStatus(), is(202));

        Thread.sleep(TEST_SLEEP_WAIT_MS); // wait for operation to propagate
        consumerResource.upsertConsumer(streamName, consumerName, US_EAST_REGION);

        Thread.sleep(TEST_SLEEP_WAIT_MS); // wait for operation to propagate
        response = consumerResource.deleteConsumer(streamName, consumerName);
        Assert.assertEquals("Consumer deleted " + consumerName, (response.getEntity()));

        Thread.sleep(TEST_SLEEP_WAIT_MS); // wait for operation to propagate
        response = consumerResource.getConsumer(streamName, consumerName);
        Assert.assertEquals("consumer not found: " + consumerName, (response.getEntity()));
    }

    @Test
    public void test_delete_consumer_with_no_stream() {
        String streamName = "junit-delete-consumer-with-no-stream";
        String consumerName = "C1";

        Response response = consumerResource.deleteConsumer(streamName, consumerName);
        Assert.assertEquals("Stream not found "+streamName, response.getEntity());
    }

    @Test
    public void test_delete_invalid_consumer_in_valid_stream() throws InterruptedException {
        String streamName = "junit-delete-invalid-consumer-invalid-stream";
        String consumerName = "C1";
        String invalidConsumerName = "invalid-consumer";

        Stream stream = JsonModelBuilder.buildJsonStream(streamName);

        streamResource.upsertStream(streamName, stream);
        Thread.sleep(TEST_SLEEP_WAIT_MS); // wait for operation to propagate

        consumerResource.upsertConsumer(streamName, consumerName, US_EAST_REGION);
        Thread.sleep(TEST_SLEEP_WAIT_MS); // wait for operation to propagate

        Response response = consumerResource.deleteConsumer(streamName, invalidConsumerName);

        Assert.assertEquals("Consumer not found: "+invalidConsumerName, response.getEntity());
    }
}