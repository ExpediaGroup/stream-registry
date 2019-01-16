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
package com.homeaway.streamplatform.streamregistry.utils;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.homeaway.digitalplatform.streamregistry.SchemaCompatibility;
import com.homeaway.streamplatform.streamregistry.model.Consumer;
import com.homeaway.streamplatform.streamregistry.model.Producer;
import com.homeaway.streamplatform.streamregistry.model.RegionStreamConfig;
import com.homeaway.streamplatform.streamregistry.model.Schema;
import com.homeaway.streamplatform.streamregistry.model.Stream;
import com.homeaway.streamplatform.streamregistry.model.Tags;

@SuppressWarnings("WeakerAccess")
public class JsonModelBuilder {
    public static final Integer TEST_PRODUCT_ID = 123456;
    public static final String  TEST_COMPONENT_ID = "580fd421-c7a5-405a-a48c-0c558c91ee8e";
    public static final Integer TEST_PORTFOLIO_ID = 987456;
    public static final String  TEST_HINT = "primary";
    public static final String  TEST_GH_URL = "github.com/example.com/example-repo";
    public static final String  TEST_VPC = "us-east-1-vpc-defa0000";

    public static Stream buildJsonStream(String streamName) {
        return buildJsonStream(streamName, TEST_PRODUCT_ID, Optional.of(TEST_COMPONENT_ID), TEST_HINT, TEST_VPC);
    }

    public static Stream buildJsonStream(String streamName, Integer productId, Optional<String> componentId, String hint) {
        return buildJsonStream(streamName, productId, componentId, hint, TEST_VPC);
    }

    public static Stream buildJsonStream(String streamName, Integer productId, Optional<String> componentId, String hint, String vpc) {
        List<String> vpcList = Collections.singletonList(vpc);

        Stream stream = Stream.builder().build();
        stream.setName(streamName);
        stream.setCreated(System.currentTimeMillis());
        stream.setUpdated(System.currentTimeMillis());
        stream.setSchemaCompatibility(SchemaCompatibility.TRANSITIVE_FULL);
        stream.setLatestKeySchema(Schema.builder().id("1").version(2).schemaString("{\"type\":\"string\"}")
                .created(Calendar.getInstance().getTime().toString())
                .updated(Calendar.getInstance().getTime().toString()).build());
        stream.setLatestValueSchema(Schema.builder().id("2").version(2).schemaString("{\"namespace\":\"com.homeaway\",\"type\":\"record\",\"name\":\"user\",\"fields\":[{\"name\":\"name\",\"type\":\"string\"}]}")
                .created(Calendar.getInstance().getTime().toString())
                .updated(Calendar.getInstance().getTime().toString())
                .build());
        stream.setOwner("user-1");
        stream.setIsDataNeededAtRest(false);
        stream.setIsAutomationNeeded(false);
        stream.setGithubUrl(TEST_GH_URL);
        stream.setTags(Tags.builder()
            .productId(productId)
            .portfolioId(TEST_PORTFOLIO_ID)
            .brand("Homeaway")
            .assetProtectionLevel("critical")
            .componentId(componentId.orElse(TEST_COMPONENT_ID))
            .hint(hint)
            .build());
        stream.setVpcList(vpcList);
        stream.setPartitions(1);
        stream.setReplicationFactor(1);
        return  stream;
    }

    public static Stream buildJsonStream(String streamName, String componentId) {
        return buildJsonStream(streamName, TEST_PRODUCT_ID, Optional.of(componentId), TEST_HINT, TEST_VPC);
    }

    public static Producer buildJsonProducer(String name, List<RegionStreamConfig> regionStreamConfigs){
        return Producer.builder().name(name).regionStreamConfigList(regionStreamConfigs).build();
    }

    public static Consumer buildJsonConsumer(String name, List<RegionStreamConfig> regionStreamConfigs){
        return Consumer.builder().name(name).regionStreamConfigList(regionStreamConfigs).build();
    }

}