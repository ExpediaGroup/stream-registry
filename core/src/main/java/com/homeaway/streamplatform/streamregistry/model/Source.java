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

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import com.homeaway.streamplatform.streamregistry.db.dao.impl.SourceDaoImpl;

/**
 * Source is registered to a stream and determines the origin
 * of events for this stream.
 * Source captures the CDC (Change Data Capture) of a source system.
 * It enables clients of Stream registry to easily register a new source
 * that's supported by Stream registry through {@link SourceType}
 */
@Builder
@Data
public class Source {

    /**
     * Name of source in the Source Store
     */
    @NotNull
    private String sourceName;

    /**
     * Type of the source. Refer to {@link SourceType}
     */
    @NotNull
    private String sourceType;

    /**
     * Name of the stream this source is bound to
     */
    @NotNull
    private String streamName;

    /**
     * Current status of the source
     */
    private String status = SourceDaoImpl.Status.UNASSIGNED.toString();

    /**
     * Key/Value map of the source configuration this Source
     * This map will be used as properties AS-IS to the downstream agents.
     */
    @Nullable
    private Map<String, String> configuration;

    /**
     * Tags that are metadata level configuration. If this configuration is
     * changed. The imperative managers should not be affected
     */
    @Nullable
    private Map<String, String> tags;

    /**
     * Milliseconds since the epoc for source creation
     */
    @ToString.Exclude
    @Builder.Default
    private long created = 0;

}
