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

@JsonDeserialize(builder = Schema.SchemaBuilder.class)
@Builder
@Data
public class Schema {

    /**
     * Id of the Subject in schema-registry
     */
    @NotNull
    String id;

    /**
     * version of the Subject in schema-registry
     */
    @NotNull
    int version;

    /**
     * complete schema
     */
    @NotNull
    String schemaString;

    /**
     * created timestamp in schema-registry
     */
    String created;

    /**
     * updated timestamp in schema-registry
     */

    String updated;

    @JsonPOJOBuilder(withPrefix = "")
    public static final class SchemaBuilder {}

}
