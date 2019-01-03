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
package com.homeaway.streamingplatform.validator.streams;

import java.util.Map;

import com.homeaway.streamingplatform.api.exception.InvalidStreamException;
import com.homeaway.streamingplatform.api.model.Stream;

/**
 * A {@code StreamValidator} is an interface that validates a {@link Stream}.
 */
public interface StreamValidator {

    /**
     * Validates a {@link Stream} under a criteria defined by the implementer.
     *
     * @param stream the stream
     * @return True if stream is valid and should be inserted into the Stream Registry
     * @throws InvalidStreamException Thrown when a stream is considered invalid and a reasoning needs to be provided for user-feedback.
     *                                The exception message can be provided by {@link StreamValidator#getValidationAssertion()}
     */
    boolean isStreamValid(Stream stream) throws InvalidStreamException;

    /**
     * Defines the exception message that is returned when an {@link InvalidStreamException} is thrown.
     * <p>
     * Example: "Stream should not contain special characters"
     * </p>
     *
     * @return An assertion statement that ideally contains "should" or "must" phrasing.
     */
    String getValidationAssertion();

    /**
     * Load extra properties from the Stream Registry configuration.
     *
     * @param configs The user-provided runtime configurations
     */
    void configure(Map<String, ?> configs);
}
