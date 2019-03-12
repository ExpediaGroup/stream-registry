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
package com.homeaway.streamplatform.streamregistry.service;

import java.util.List;

import com.homeaway.streamplatform.streamregistry.exceptions.*;
import com.homeaway.streamplatform.streamregistry.model.Stream;

// TODO - Need javadoc (#107)
public interface StreamService {

    /**
     *  Insert or Update a Stream
     * @param stream
     * @throws SchemaManagerException - when the input schema registration fails against SchemaRegistry
     * @throws InvalidStreamException - When the validator that implements
     *              `com.homeaway.streamplatform.streamregistry.extensions.validation.StreamValidator` check fails on input Stream Object.
     * @throws StreamCreationException - When streams could not be created in the underlying streaming infrastructure.
     * @throws ClusterNotFoundException - When cluster is not found for the input VPC and Hint
     */
    void upsertStream(Stream stream) throws SchemaManagerException, InvalidStreamException, StreamCreationException, ClusterNotFoundException;

    /**
     * Get a Stream for the given name
     * @param streamName
     * @return
     * @throws StreamNotFoundException - when Stream is not available for the given Stream Name
     */
    Stream getStream(String streamName) throws StreamNotFoundException;

    /**
     * Delete the stream for the given name
     * @param streamName
     * @throws StreamNotFoundException - when Stream is not available for the given Stream Name
     */
    void deleteStream(String streamName) throws StreamNotFoundException;

    /**
     * Get all the streams.
     *
     * @return List<Stream>
     */
    List<Stream> getAllStreams();

    /**
     * Validate the input schema against the SchemaRegistry.
     * @param stream
     * @return
     * @throws SchemaValidationException -
     *  a) Where there is a RuntimeException while validating schema against schema-registry.
     *  b) When schema validation check fails.
     */
    boolean validateSchemaCompatibility(Stream stream) throws SchemaValidationException;

}
