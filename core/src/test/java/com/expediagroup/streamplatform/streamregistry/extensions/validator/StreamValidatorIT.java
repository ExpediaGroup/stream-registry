/* Copyright (c) 2018-Present Expedia Group.
 * All rights reserved.  http://www.expediagroup.com

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
package com.expediagroup.streamplatform.streamregistry.extensions.validator;

import java.util.Map;
import java.util.Optional;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import lombok.extern.slf4j.Slf4j;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;

import com.expediagroup.streamplatform.streamregistry.extensions.validation.StreamValidator;
import com.expediagroup.streamplatform.streamregistry.model.Stream;
import com.expediagroup.streamplatform.streamregistry.resource.BaseResourceIT;
import com.expediagroup.streamplatform.streamregistry.utils.JsonModelBuilder;

@Slf4j
public class StreamValidatorIT extends BaseResourceIT {

    public static final int INVALID_PRODUCT_ID = 13;

    public static class IntegrationTestStreamValidator implements StreamValidator {

        public static final String INVALID_ID_CONFIG = "validator.invalid-id";
        private int invalidProductId;

        @Override
        public boolean isStreamValid(Stream stream) {
            if (stream.getTags().getProductId().equals(INVALID_PRODUCT_ID)) {
                log.error("Validation failed. Invalid Product ID: {}", stream.getTags().getProductId());
                return false;
            }
            if (stream.getVpcList() == null || stream.getVpcList().isEmpty()) {
                log.error("Stream {} cannot be created without vpcList configuration", stream.getName());
                return false;
            }
            return true;
        }

        @Override
        public String getValidationAssertion() {
            return "Stream Product ID tag cannot be " + invalidProductId;
        }

        @Override
        public void configure(Map<String, ?> configs) {
            this.invalidProductId = (Integer) configs.get(INVALID_ID_CONFIG);
        }
    }

    @BeforeClass
    public static void setup() {
        Client mockHttpClient = Mockito.mock(Client.class);
        mockHttpClientSuccess(mockHttpClient);
    }

    @Test
    public void test_upsertStream_InvalidProductId() {
        String streamName = "junit-stream-1";
        Stream stream = JsonModelBuilder.buildJsonStream(streamName, INVALID_PRODUCT_ID, Optional.empty(), JsonModelBuilder.TEST_HINT);
        Response response = streamResource.upsertStream(streamName, stream);

        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    public static void mockHttpClientSuccess(Client mockHttpClient) {
        Mockito.when(mockHttpClient.target(Mockito.anyString()))
                .thenAnswer((InvocationOnMock invocationOnMock) -> {
                    WebTarget mockWebTarget = Mockito.mock(WebTarget.class);
                    Invocation.Builder mockBuilder = Mockito.mock(Invocation.Builder.class);
                    Mockito.when(mockWebTarget.request()).thenReturn(mockBuilder);
                    // TODO: Handle mock responses properly for negative test cases as well.
                    String url = invocationOnMock.getArgument(0);
                    Mockito.when(mockBuilder.head()).thenReturn(Response.status(200).build());
                    return mockWebTarget;
                });

    }

}
