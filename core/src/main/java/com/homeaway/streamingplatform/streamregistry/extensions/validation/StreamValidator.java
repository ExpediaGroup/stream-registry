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
package com.homeaway.streamingplatform.streamregistry.extensions.validation;

import java.util.Map;

import com.homeaway.streamingplatform.streamregistry.exceptions.InvalidStreamException;
import com.homeaway.streamingplatform.streamregistry.model.Stream;

/**
 * The interface Stream validator.
 */
public interface StreamValidator {
    // TODO: Move this interface to a standalone 'stream-registry-validators' module

    /**
     * Validates a {@link Stream} under a criteria defined by the implementer.
     *
     * @param stream the stream
     * @return true if stream should be inserted into the registry
     * @throws InvalidStreamException the invalid stream exception
     */
    boolean isStreamValid(Stream stream) throws InvalidStreamException;

    /**
     * Defines the exception message that is returned with an {@link InvalidStreamException} is thrown.
     * <p>
     * Example: "Stream should not contain special characters"
     * </p>
     *
     * @return An assertion statement that ideally contains "should" or "must" phrasing.
     */
    String getValidationAssertion();

    /**
     * Load extra properties from Stream Registry configuration.
     *
     * @param configs the configs
     */
    void configure(Map<String, ?> configs);
}
