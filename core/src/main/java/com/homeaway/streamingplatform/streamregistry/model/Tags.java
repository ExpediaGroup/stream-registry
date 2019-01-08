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
package com.homeaway.streamingplatform.streamregistry.model;

import javax.validation.constraints.NotNull;

import lombok.Builder;
import lombok.Data;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

// TODO - Need to genericize Tags so that this is company agnostic
@Data
@Builder
@JsonDeserialize(builder = Tags.TagsBuilder.class)
public class Tags {

    /**
     * Product Id of the application.
     */
    @NotNull
    @Builder.Default
    private Integer productId = -1;

    /**
     * Portfolio Id of the application.
     */
    @Builder.Default
    private Integer portfolioId = -1;

    /**
     * Brand name in Expedia Inc.
     */
    @Builder.Default
    private String brand = null;

    /**
     * Asset Protection Level
     */
    @Builder.Default
    private String assetProtectionLevel = null;

    /**
     * ComponentId (required)
     */
    private String componentId;

    /**
     * Hint for Stream Registry to choose a cluster.
     * Example: primary, secondary, logs.
     */
    @Builder.Default
    private String hint = "primary";

    @JsonPOJOBuilder(withPrefix = "")
    public static final class TagsBuilder{

    }

}
