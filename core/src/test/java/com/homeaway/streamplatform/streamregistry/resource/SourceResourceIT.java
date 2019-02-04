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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.junit.Assert;
import org.junit.Test;

import com.homeaway.streamplatform.streamregistry.model.Source;
import com.homeaway.streamplatform.streamregistry.model.Stream;
import com.homeaway.streamplatform.streamregistry.utils.JsonModelBuilder;

public class SourceResourceIT extends BaseResourceIT {

    @Test
    public void testSourceCRUD() throws InterruptedException {

        Map<String, String> configuration = new HashMap<>();
        configuration.put("foo", "bar");

        Source sourceA = Source.builder()
                .streamName("streamA")
                .sourceName("sourceA")
                .sourceType("kinesis")
                .streamSourceConfiguration(configuration)
                .build();

        Stream stream = JsonModelBuilder.buildJsonStream("streamA");
        Response response = streamResource.upsertStream("streamA", stream);
        Assert.assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatus());


        sourceResource.putSource("streamA", sourceA);

        Thread.sleep(TEST_SLEEP_WAIT_MS); // wait so that the Topology can process it and materialize to KV-Store

        Response sourceResponse = sourceResource.getSource("streamA", sourceA.getSourceName());
        Assert.assertEquals(Response.Status.OK.getStatusCode(), sourceResponse.getStatus());

        Source sourceB = Source.builder()
                .streamName("streamA")
                .sourceName("sourceB")
                .sourceType("kafka")
                .streamSourceConfiguration(configuration)
                .build();

        sourceResource.putSource("streamA", sourceB);

        Thread.sleep(TEST_SLEEP_WAIT_MS); // wait so that the Topology can process it and materialize to KV-Store

        List<Source> sources = (List<Source>) sourceResource.getAllSourcesByStream("streamA").getEntity();
        Assert.assertEquals(2, sources.size());

        Map<String, String> updatedConfiguration = new HashMap<>(configuration);
        updatedConfiguration.put("username", "x");

        Source updatedSourceA = Source.builder()
                .streamName("streamA")
                .sourceName("sourceA")
                .sourceType("kinesis")
                .streamSourceConfiguration(updatedConfiguration)
                .build();

        sourceResource.putSource("streamA", updatedSourceA);

        Thread.sleep(TEST_SLEEP_WAIT_MS); // wait so that the Topology can process it and materialize to KV-Store

        Source updateSourceEntity = (Source) sourceResource.getSource("streamA", updatedSourceA.getSourceName()).getEntity();

        Assert.assertEquals(2, updateSourceEntity.getStreamSourceConfiguration().size());

        sourceResource.deleteSource("streamA", "sourceA");

        Thread.sleep(TEST_SLEEP_WAIT_MS); // wait so that the Topology can process it and materialize to KV-Store

        List<Source> streamWithoutSourceA = (List<Source>) sourceResource.getAllSourcesByStream("streamA").getEntity();

        Assert.assertEquals(1, streamWithoutSourceA.size());
    }
}
