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
package com.homeaway.streamplatform.streamregistry.model;

import java.util.Map;

import javax.validation.constraints.NotNull;

import lombok.Builder;
import lombok.Data;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;


/**
 * Source is registered to a stream and determines the origin
 * of events for this stream.
 * A {@link Source} is a different from a {@link Producer} since a
 * source captures the CDC (Change Data Capture) of a source system.
 * It enables clients of Stream registry to easily register a new source
 * that's supported by Stream registry through {@link SourceType}
 * to an already registered stream.
 */
@JsonDeserialize(builder = Source.SourceBuilder.class)
@Builder
@Data
public class Source {


    /**
     * Stream name this source is registered to
     */
    @NotNull
    private String streamName;

    /**
     * Name of the source
     */
    @NotNull
    private String sourceName;

    /**
     * Type of the source. Refer to {@link SourceType}
     */
    @NotNull
    private String sourceType;

    /**
     * Key/Value map of any additional configuration this Source
     * needs to pass in a flexible way. This map will be used as
     * properties AS-IS and no validation will be performed on this
     * configuration.
     */
    @NotNull
    private Map<String, String> streamSourceConfiguration;

    @JsonPOJOBuilder(withPrefix = "")
    public static final class SourceBuilder {}
}
