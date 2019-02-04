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


/**
 * StreamSource is a CDC (Change Data Capture) producer
 * It defines the basic information needed to successfully interact with a
 * Source database/Stream to create a change data capture stream
 * The *imperative* SourceManager is responsible for authenticating this source.
 */
public class StreamSource {

    //TODO: Fix this class to include authentication of StreamSource, after RBAC #23 is resolved.

    /**
     * The current source type defined for a Stream.
     * {@link SourceType}
     */
    @NotNull
    private String sourceType;

    /**
     * Name of the table/stream depending on the @link{SourceType}
     * For eg: Kinesis stream sourceName = kinesisStreamName
     *         MySQL table sourceName = MySQLTableName
     */
    @NotNull
    private String sourceName;

    /**
     * Any additional configuration that needs to be passed to SourceManager
     * Will be translated in the SourceManager to a "key=value" that's inserted
     * in the configuration json to configure the stream accordingly.
     */
    private Map<String, String> streamSourceConfiguration;

}
