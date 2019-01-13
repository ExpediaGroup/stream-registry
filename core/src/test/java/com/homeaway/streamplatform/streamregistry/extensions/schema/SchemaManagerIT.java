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
package com.homeaway.streamplatform.streamregistry.extensions.schema;

import javax.ws.rs.core.Response;

import org.junit.Assert;
import org.junit.Test;

import com.homeaway.streamplatform.streamregistry.model.Stream;
import com.homeaway.streamplatform.streamregistry.resource.BaseResourceIT;
import com.homeaway.streamplatform.streamregistry.utils.JsonModelBuilder;

public class SchemaManagerIT extends BaseResourceIT {

    @Test
    public void test_validate_stream_compatibility_new_schema_valid() {
        // test compatibility with already existing schema
        String streamName = "junit-check-stream-compatibility-new-schema-valid";
        Stream stream = JsonModelBuilder.buildJsonStream(streamName);

        // create stream/register schema
        streamResource.upsertStream(streamName, stream);

        // re-check compatibility for same schema (new field with default value)
        String newSchema = "{\"namespace\":\"com.homeaway\",\"type\":\"record\",\"name\":\"user\",\"fields\":" +
                "[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"age\",\"type\":\"int\",\"default\":1}]}";
        stream.getLatestValueSchema().setSchemaString(newSchema);
        Response response = streamResource.validateStreamCompatibility(stream, streamName, "default");
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void test_validate_stream_compatibility_new_schema_invalid() {
        String streamName = "junit-check-stream-compatibility-new-schema-invalid";
        Stream stream = JsonModelBuilder.buildJsonStream(streamName);

        // create stream/register schema
        streamResource.upsertStream(streamName, stream);

        stream.getLatestValueSchema().setSchemaString(stream.getLatestKeySchema().getSchemaString());
        Response response = streamResource.validateStreamCompatibility(stream, streamName, "default");
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void test_validate_stream_compatibility_bad_schema() {
        // provided schema string is malformed
        String streamName = "junit-check-stream-compatibility-bad-schema-failure";
        Stream stream = JsonModelBuilder.buildJsonStream(streamName);

        String schemaString = "this is not valid json";
        stream.getLatestValueSchema().setSchemaString(schemaString);
        Response response = streamResource.validateStreamCompatibility(stream, streamName, "default");
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void test_register_stream_valid() throws InterruptedException {
        // happy path
        String streamName = "junit-check-register-stream-valid";
        Stream stream = JsonModelBuilder.buildJsonStream(streamName);

        // create stream/register schema
        Response response = streamResource.upsertStream(streamName, stream);
        Assert.assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatus());

        Thread.sleep(TEST_SLEEP_WAIT_MS);

        response = streamResource.getStream(streamName);
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void test_register_stream_invalid_schema_returns_400() {
        String streamName = "junit-check-register-stream-invalid-schema";
        Stream stream = JsonModelBuilder.buildJsonStream(streamName);

        // create stream/register schema
        Response response = streamResource.upsertStream(streamName, stream);
        Assert.assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatus());

        String schemaString = "this is not valid json";
        stream.getLatestValueSchema().setSchemaString(schemaString);
        response = streamResource.upsertStream(streamName, stream);
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void test_register_stream_incompatible_schema_returns_400() throws InterruptedException {
        String streamName = "junit-check-register-stream-incompatible-schema";
        Stream stream = JsonModelBuilder.buildJsonStream(streamName);

        // create stream/register schema
        Response response = streamResource.upsertStream(streamName, stream);
        Assert.assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatus());

        Thread.sleep(TEST_SLEEP_WAIT_MS);

        // incompatible schema
        stream.getLatestValueSchema().setSchemaString(stream.getLatestKeySchema().getSchemaString());
        response = streamResource.upsertStream(streamName, stream);
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void test_validate_stream_compatibility_new_stream_valid() {
        // happy path
        String streamName = "junit-check-stream-compatibility-new-stream-valid";
        Stream stream = JsonModelBuilder.buildJsonStream(streamName);

        Response response = streamResource.validateStreamCompatibility(stream, streamName, "default");
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void test_validate_stream_compatibility_same_schema_valid() {
        // test compatibility with already existing schema
        String streamName = "junit-check-stream-compatibility-same-schema-valid";
        Stream stream = JsonModelBuilder.buildJsonStream(streamName);

        // create stream/register schema
        streamResource.upsertStream(streamName, stream);

        // re-check compatibility for same schema
        Response response = streamResource.validateStreamCompatibility(stream, streamName, "default");
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }
}
