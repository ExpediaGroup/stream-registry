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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.core.Response;

import lombok.extern.slf4j.Slf4j;

import org.junit.Assert;
import org.junit.Test;

import com.homeaway.streamplatform.streamregistry.model.Stream;
import com.homeaway.streamplatform.streamregistry.utils.JsonModelBuilder;
import com.homeaway.streamplatform.streamregistry.utils.StreamRegistryUtils.EntriesPage;

@Slf4j
public class StreamResourceIT extends BaseResourceIT {

    @Test
    public void test_upsertStream_create_new_stream() throws InterruptedException {
        String streamName = "junit-stream-create-new-stream";
        Stream stream = JsonModelBuilder.buildJsonStream(streamName);

        streamResource.upsertStream(streamName, stream);
        Thread.sleep(TEST_SLEEP_WAIT_MS);

        Response streamResponse = streamResource.getStream(streamName);
        Assert.assertEquals(stream.getName(), ((Stream) streamResponse.getEntity()).getName());
        Assert.assertEquals(stream.getOwner(), ((Stream) streamResponse.getEntity()).getOwner());
        Assert.assertEquals(stream.getLatestKeySchema().getId(), ((Stream) streamResponse.getEntity()).getLatestKeySchema().getId());
        Assert.assertEquals(stream.getLatestKeySchema().getSchemaString(), ((Stream) streamResponse.getEntity()).getLatestKeySchema().getSchemaString());
        Assert.assertEquals(stream.getLatestValueSchema().getId(), ((Stream) streamResponse.getEntity()).getLatestValueSchema().getId());
        Assert.assertEquals(stream.getLatestValueSchema().getSchemaString(), ((Stream) streamResponse.getEntity()).getLatestValueSchema().getSchemaString());
        Assert.assertEquals(stream.getGithubUrl(), ((Stream) streamResponse.getEntity()).getGithubUrl());
    }

    @Test
    public void test_upsertStream_with_vpcList() throws InterruptedException {
        String streamName = "junit-stream-with-region";
        Stream stream = JsonModelBuilder.buildJsonStream(streamName);
        List<String> vpcList = Collections.singletonList(US_EAST_REGION);
        streamResource.upsertStream(streamName, stream);
        Thread.sleep(TEST_SLEEP_WAIT_MS);

        Response streamResponse = streamResource.getStream(streamName);

        Assert.assertEquals(vpcList, ((Stream) streamResponse.getEntity()).getVpcList());
        Assert.assertEquals(1, ((Stream) streamResponse.getEntity()).getReplicationFactor());
    }

    @Test
    public void test_upsertStream_with_replicatedVpcList() throws InterruptedException {
        String streamName = "junit-stream-with-region";
        Stream stream = JsonModelBuilder.buildJsonStream(streamName);
        List<String> replicatedVpcList = Collections.singletonList(US_EAST_REGION);
        stream.setReplicatedVpcList(replicatedVpcList);

        streamResource.upsertStream(streamName, stream);
        Thread.sleep(TEST_SLEEP_WAIT_MS);

        Response streamResponse = streamResource.getStream(streamName);

        Assert.assertEquals(replicatedVpcList, ((Stream) streamResponse.getEntity()).getReplicatedVpcList());
        Assert.assertEquals(1, ((Stream) streamResponse.getEntity()).getReplicationFactor());
    }

    @Test
    public void test_upsertStream_without_vpcList() {
        String streamName = "junit-stream-without-vpc";
        Stream stream = JsonModelBuilder.buildJsonStream(streamName);
        stream.setVpcList(null);

        Map<String, String> topicConfig = new HashMap<>();
        topicConfig.put(BaseResourceIT.NO_OF_PARTITIONS, String.valueOf(1));
        topicConfig.put(BaseResourceIT.REPLICATION_FACTOR, String.valueOf(1));

        stream.setTopicConfig(topicConfig);
        Response response= streamResource.upsertStream(streamName, stream);

        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void test_upsertStream_existing_stream() throws InterruptedException {
        String streamName = "junit-stream-upsert-existing-stream";
        Stream stream = JsonModelBuilder.buildJsonStream(streamName);

        streamResource.upsertStream(streamName, stream);
        Thread.sleep(TEST_SLEEP_WAIT_MS);

        Response streamResponse = streamResource.getStream(streamName);
        Assert.assertEquals(stream.getName(), ((Stream)streamResponse.getEntity()).getName());
        Assert.assertEquals(stream.getOwner(), ((Stream)streamResponse.getEntity()).getOwner());

        // Update the stream and UPSERT it.
        stream.setOwner("user-2");
        streamResource.upsertStream(streamName, stream);
        Thread.sleep(TEST_SLEEP_WAIT_MS);

        streamResponse = streamResource.getStream(streamName);
        Assert.assertEquals(stream.getName(), ((Stream) streamResponse.getEntity()).getName());
        Assert.assertEquals(stream.getOwner(), ((Stream) streamResponse.getEntity()).getOwner());
        Assert.assertEquals(stream.getLatestKeySchema().getId(), ((Stream) streamResponse.getEntity()).getLatestKeySchema().getId());
        Assert.assertEquals(stream.getLatestKeySchema().getSchemaString(), ((Stream) streamResponse.getEntity()).getLatestKeySchema().getSchemaString());
        Assert.assertEquals(stream.getLatestValueSchema().getId(), ((Stream) streamResponse.getEntity()).getLatestValueSchema().getId());
        Assert.assertEquals(stream.getLatestValueSchema().getSchemaString(), ((Stream) streamResponse.getEntity()).getLatestValueSchema().getSchemaString());
        Assert.assertEquals(stream.getGithubUrl(), ((Stream) streamResponse.getEntity()).getGithubUrl());
    }

    @Test
    public void test_upsertStream_null_stream_name() {
        Stream stream = JsonModelBuilder.buildJsonStream(null);

        Response response = streamResource.upsertStream(null, stream);

        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void test_upsertStream_get_stream() {
        Response response = streamResource.getStream("Nothing");
        Assert.assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        Assert.assertEquals("Stream not found Nothing", response.getEntity());
    }

    @Test
    public void test_getAllStreamsWithTime() throws InterruptedException {
        String streamName1 = "junit-stream-getAllStreams-with-time-1";
        String streamName2 = "junit-stream-getAllStreams-with-time-2";

        Stream stream1 = JsonModelBuilder.buildJsonStream(streamName1);
        Stream stream2 = JsonModelBuilder.buildJsonStream(streamName2);

        streamResource.upsertStream(streamName1, stream1);
        streamResource.upsertStream(streamName2, stream2);
        Thread.sleep(TEST_SLEEP_WAIT_MS);

        // Two streams should be in the first 10 elements of page 0
        Response response = streamResource.getAllStreams(Optional.of(0), Optional.of(10));
        @SuppressWarnings("unchecked")
        List<Stream> page = ((EntriesPage<Stream>) response.getEntity()).getEntries();
        page.forEach(stream -> log.debug("Stream name={}", stream.getName()));
        Assert.assertTrue(2 <= page.size());
        // These Asserts would not be equal because the Created/Updated time given by the user is ignored so the times won't match.
        Assert.assertNotEquals(stream1, page.get(0));
        Assert.assertNotEquals(stream2, page.get(1));
    }

    @Test
    public void test_deleteStream() throws InterruptedException {
        String streamName = "junit-stream-delete-stream-1";
        Stream stream = JsonModelBuilder.buildJsonStream(streamName);

        streamResource.upsertStream(streamName, stream);
        Thread.sleep(TEST_SLEEP_WAIT_MS); // wait so that the Topology can process it and materialize to KV-Store

        streamResource.deleteStream(streamName);
        Thread.sleep(TEST_SLEEP_WAIT_MS); // wait so that the Topology can process it and materialize to KV-Store

        // Make sure the getStream return 404
        Response response = streamResource.getStream(streamName);
        Assert.assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        Assert.assertEquals("Stream not found "+streamName, response.getEntity());
    }

    @Test
    public void test_create_stream_with_tags() throws InterruptedException {
        String streamName = "junit-stream-create-stream-with-tags";
        Stream stream = JsonModelBuilder.buildJsonStream(streamName);

        streamResource.upsertStream(streamName, stream);
        Thread.sleep(TEST_SLEEP_WAIT_MS);

        Response streamResponse = streamResource.getStream(streamName);
        Assert.assertEquals(stream.getName(), ((Stream) streamResponse.getEntity()).getName());
        Assert.assertEquals(stream.getOwner(), ((Stream) streamResponse.getEntity()).getOwner());
        Assert.assertEquals(stream.getLatestKeySchema().getId(), ((Stream) streamResponse.getEntity()).getLatestKeySchema().getId());
        Assert.assertEquals(stream.getLatestKeySchema().getSchemaString(), ((Stream) streamResponse.getEntity()).getLatestKeySchema().getSchemaString());
        Assert.assertEquals(stream.getLatestValueSchema().getId(), ((Stream) streamResponse.getEntity()).getLatestValueSchema().getId());
        Assert.assertEquals(stream.getLatestValueSchema().getSchemaString(), ((Stream) streamResponse.getEntity()).getLatestValueSchema().getSchemaString());
        Assert.assertEquals(stream.getGithubUrl(), ((Stream) streamResponse.getEntity()).getGithubUrl());
        Assert.assertEquals(stream.getTags().getComponentId(), ((Stream) streamResponse.getEntity()).getTags().getComponentId());
        Assert.assertEquals(stream.getTags().getProductId(), ((Stream) streamResponse.getEntity()).getTags().getProductId());
        Assert.assertEquals(stream.getTags().getPortfolioId(), ((Stream) streamResponse.getEntity()).getTags().getPortfolioId());
        Assert.assertEquals(stream.getTags().getAssetProtectionLevel(), ((Stream) streamResponse.getEntity()).getTags().getAssetProtectionLevel());
        Assert.assertEquals(stream.getTags().getBrand(), ((Stream) streamResponse.getEntity()).getTags().getBrand());
        Assert.assertEquals(stream.getTags().getHint(), ((Stream) streamResponse.getEntity()).getTags().getHint());
    }

    @Test
    public void test_create_stream_with_hint_as_string() throws InterruptedException {
        String streamName = "test-create-stream-with-hint-as-string";

        String HINT_SWAGGER = "string";
        Stream stream = JsonModelBuilder.buildJsonStream(streamName, JsonModelBuilder.TEST_PRODUCT_ID, Optional.of(TEST_COMPONENT_ID), HINT_SWAGGER);

        streamResource.upsertStream(streamName, stream);
        Thread.sleep(TEST_SLEEP_WAIT_MS);

        Response streamResponse = streamResource.getStream(streamName);
        Assert.assertEquals(stream.getName(), ((Stream) streamResponse.getEntity()).getName());
        Assert.assertEquals(stream.getTags().getHint(), ((Stream) streamResponse.getEntity()).getTags().getHint());
    }
    @Test
    public void test_tag() throws InterruptedException {
        String streamName = "junit-stream-create-stream-with-hint-tag-other";
        String OTHER_HINT = "other-alias";
        Stream stream = JsonModelBuilder.buildJsonStream(streamName, JsonModelBuilder.TEST_PRODUCT_ID, Optional.of(TEST_COMPONENT_ID), OTHER_HINT);

        streamResource.upsertStream(streamName, stream);
        Thread.sleep(TEST_SLEEP_WAIT_MS);

        Response streamResponse = streamResource.getStream(streamName);
        if(streamResponse.getEntity() instanceof String) {
            throw new IllegalStateException("Expected String entity. Instead response=" + streamResponse.getEntity());
        }
        Assert.assertEquals(stream.getName(), ((Stream) streamResponse.getEntity()).getName());
        Assert.assertEquals(stream.getOwner(), ((Stream) streamResponse.getEntity()).getOwner());
        Assert.assertEquals(stream.getLatestKeySchema().getId(), ((Stream) streamResponse.getEntity()).getLatestKeySchema().getId());
        Assert.assertEquals(stream.getLatestKeySchema().getSchemaString(), ((Stream) streamResponse.getEntity()).getLatestKeySchema().getSchemaString());
        Assert.assertEquals(stream.getLatestValueSchema().getId(), ((Stream) streamResponse.getEntity()).getLatestValueSchema().getId());
        Assert.assertEquals(stream.getLatestValueSchema().getSchemaString(), ((Stream) streamResponse.getEntity()).getLatestValueSchema().getSchemaString());
        Assert.assertEquals(stream.getGithubUrl(), ((Stream) streamResponse.getEntity()).getGithubUrl());
        Assert.assertEquals(stream.getTags().getComponentId(), ((Stream) streamResponse.getEntity()).getTags().getComponentId());
        Assert.assertEquals(stream.getTags().getProductId(), ((Stream) streamResponse.getEntity()).getTags().getProductId());
        Assert.assertEquals(stream.getTags().getPortfolioId(), ((Stream) streamResponse.getEntity()).getTags().getPortfolioId());
        Assert.assertEquals(stream.getTags().getAssetProtectionLevel(), ((Stream) streamResponse.getEntity()).getTags().getAssetProtectionLevel());
        Assert.assertEquals(stream.getTags().getBrand(), ((Stream) streamResponse.getEntity()).getTags().getBrand());
        Assert.assertEquals(stream.getTags().getHint(), ((Stream) streamResponse.getEntity()).getTags().getHint());
    }

    @Test
    public void test_upsert_stream_componentid() throws InterruptedException {
        String streamName = "junit-upsert-stream-componentid-1";
        Stream stream = JsonModelBuilder.buildJsonStream(streamName);

        streamResource.upsertStream(streamName, stream);
        Thread.sleep(TEST_SLEEP_WAIT_MS); // wait so that the Topology can process it and materialize to KV-Store

        String updatedComponentId = UUID.randomUUID().toString();

        Stream updatedStream = JsonModelBuilder.buildJsonStream(streamName, updatedComponentId);

        streamResource.upsertStream(streamName, updatedStream);

        Thread.sleep(TEST_SLEEP_WAIT_MS);

        Response streamResponse = streamResource.getStream(streamName);
        Assert.assertEquals(stream.getName(), ((Stream) streamResponse.getEntity()).getName());
        Assert.assertEquals(stream.getOwner(), ((Stream) streamResponse.getEntity()).getOwner());
        Assert.assertEquals(stream.getLatestKeySchema().getId(), ((Stream) streamResponse.getEntity()).getLatestKeySchema().getId());
        Assert.assertEquals(stream.getLatestKeySchema().getSchemaString(), ((Stream) streamResponse.getEntity()).getLatestKeySchema().getSchemaString());
        Assert.assertEquals(stream.getLatestValueSchema().getId(), ((Stream) streamResponse.getEntity()).getLatestValueSchema().getId());
        Assert.assertEquals(stream.getLatestValueSchema().getSchemaString(), ((Stream) streamResponse.getEntity()).getLatestValueSchema().getSchemaString());
        Assert.assertEquals(stream.getGithubUrl(), ((Stream) streamResponse.getEntity()).getGithubUrl());
        Assert.assertEquals(updatedComponentId, ((Stream) streamResponse.getEntity()).getTags().getComponentId());
        Assert.assertEquals(stream.getTags().getProductId(), ((Stream) streamResponse.getEntity()).getTags().getProductId());
        Assert.assertEquals(stream.getTags().getPortfolioId(), ((Stream) streamResponse.getEntity()).getTags().getPortfolioId());
        Assert.assertEquals(stream.getTags().getAssetProtectionLevel(), ((Stream) streamResponse.getEntity()).getTags().getAssetProtectionLevel());
        Assert.assertEquals(stream.getTags().getBrand(), ((Stream) streamResponse.getEntity()).getTags().getBrand());
        Assert.assertEquals(stream.getTags().getHint(), ((Stream) streamResponse.getEntity()).getTags().getHint());
    }

    @Test
    public void test_upsert_stream_automation() throws InterruptedException {
        String streamName = "junit-upsert-stream-automation-1";
        Stream stream = JsonModelBuilder.buildJsonStream(streamName);

        Response streamResponse = streamResource.upsertStream(streamName, stream);
        Thread.sleep(TEST_SLEEP_WAIT_MS);
        if(streamResponse.getEntity() instanceof String) {
            throw new IllegalStateException("Expected String entity. Instead response=" + streamResponse.getEntity());
        }

        streamResponse = streamResource.getStream(streamName);
        Assert.assertEquals(stream.getName(), ((Stream) streamResponse.getEntity()).getName());
        Assert.assertEquals(stream.getOwner(), ((Stream) streamResponse.getEntity()).getOwner());
        Assert.assertEquals(stream.getLatestKeySchema().getId(), ((Stream) streamResponse.getEntity()).getLatestKeySchema().getId());
        Assert.assertEquals(stream.getLatestKeySchema().getSchemaString(), ((Stream) streamResponse.getEntity()).getLatestKeySchema().getSchemaString());
        Assert.assertEquals(stream.getLatestValueSchema().getId(), ((Stream) streamResponse.getEntity()).getLatestValueSchema().getId());
        Assert.assertEquals(stream.getLatestValueSchema().getSchemaString(), ((Stream) streamResponse.getEntity()).getLatestValueSchema().getSchemaString());
        Assert.assertEquals(stream.getGithubUrl(), ((Stream) streamResponse.getEntity()).getGithubUrl());
        Assert.assertEquals(stream.getIsDataNeededAtRest(), ((Stream) streamResponse.getEntity()).getIsDataNeededAtRest());
        Assert.assertEquals(stream.getIsAutomationNeeded(), ((Stream) streamResponse.getEntity()).getIsAutomationNeeded());
        Assert.assertEquals(stream.getTags().getComponentId(), ((Stream) streamResponse.getEntity()).getTags().getComponentId());
        Assert.assertEquals(stream.getTags().getProductId(), ((Stream) streamResponse.getEntity()).getTags().getProductId());
        Assert.assertEquals(stream.getTags().getPortfolioId(), ((Stream) streamResponse.getEntity()).getTags().getPortfolioId());
        Assert.assertEquals(stream.getTags().getAssetProtectionLevel(), ((Stream) streamResponse.getEntity()).getTags().getAssetProtectionLevel());
        Assert.assertEquals(stream.getTags().getBrand(), ((Stream) streamResponse.getEntity()).getTags().getBrand());
        Assert.assertEquals(stream.getTags().getHint(), ((Stream) streamResponse.getEntity()).getTags().getHint());
    }

    @Test
    public void test_upsert_stream_invalid_partition_count() throws InterruptedException {
        String streamName = "junit-upsert-stream-invalid-partition-count-1";
        Stream stream = JsonModelBuilder.buildJsonStream(streamName);
        int startingPartitions = 3;
        stream.setPartitions(startingPartitions);

        streamResource.upsertStream(streamName, stream);
        Thread.sleep(TEST_SLEEP_WAIT_MS);

        int updatePartitions = 2;
        stream.setPartitions(updatePartitions);
        Response response = streamResource.upsertStream(streamName, stream);

        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void test_upsert_stream_invalid_replication_factor() throws InterruptedException {
        String streamName = "junit-upsert-stream-invalid-replication-factor-1";
        Stream stream = JsonModelBuilder.buildJsonStream(streamName);
        int startingReplicationFactor = 3;
        stream.setReplicationFactor(startingReplicationFactor);

        streamResource.upsertStream(streamName, stream);
        Thread.sleep(TEST_SLEEP_WAIT_MS);

        int updateReplicationFactor = 4;
        stream.setReplicationFactor(updateReplicationFactor);
        Response response = streamResource.upsertStream(streamName, stream);

        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }
}