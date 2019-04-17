/* Copyright (c) 2018-Present Expedia Group.
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
package com.homeaway.streamplatform.streamregistry.model;

import java.util.Map;

import javax.validation.constraints.NotNull;

import lombok.Builder;
import lombok.Data;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonDeserialize(builder = ConnectorConfig.ConnectorConfigBuilder.class)
@Builder
@Data
public class ConnectorConfig {

    @NotNull
    String name;

    @NotNull
    String type;

    @NotNull
    Map<String, String> properties;

    public ConnectorConfig(String name, String type, Map<String, String> properties) {
        this.name = name;
        this.type = type;
        this.properties = properties;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class ConnectorConfigBuilder {}
}