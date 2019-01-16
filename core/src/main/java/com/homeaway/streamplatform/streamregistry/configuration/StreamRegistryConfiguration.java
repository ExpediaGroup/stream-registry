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
package com.homeaway.streamplatform.streamregistry.configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;

@Data
@EqualsAndHashCode(callSuper = true)
public class StreamRegistryConfiguration extends Configuration {

    @Valid
    @NonNull
    String env;

    @Valid
    StreamValidatorConfig streamValidatorConfig;

    @Valid
    SchemaManagerConfig schemaManagerConfig;

    @Valid
    @NotNull
    KafkaStreamsConfig kafkaStreamsConfig;

    @Valid
    @NotNull
    KafkaProducerConfig kafkaProducerConfig;

    @Valid
    @NotNull
    TopicsConfig topicsConfig;

    @Valid
    @NotNull
    InfraManagerConfig infraManagerConfig;

    @Valid
    @NotNull
    @JsonProperty("swagger")
    SwaggerBundleConfiguration swaggerBundleConfiguration;

    @Valid
    @NotNull
    private JerseyClientConfiguration httpClient = new JerseyClientConfiguration();
}
