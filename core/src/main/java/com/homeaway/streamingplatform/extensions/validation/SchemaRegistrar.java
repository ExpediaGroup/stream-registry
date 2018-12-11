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
package com.homeaway.streamingplatform.extensions.validation;

import java.util.Map;

import com.homeaway.streamingplatform.exceptions.SchemaRegistrationException;
import com.homeaway.streamingplatform.model.Stream;

/**
 * Interface for managing schema validation, registration
 */
public interface SchemaRegistrar {

    /**
     * Used to validate the stream's key and value schemas
     *
     * @param stream object
     * @return whether schemas are valid or not
     */
    boolean isSchemaValid(Stream stream);

    /**
     * Perform schema registration
     *
     * @param stream object
     * @return same stream object with key/value schemas updated
     * @throws SchemaRegistrationException
     */
    Stream registerSchema(Stream stream) throws SchemaRegistrationException;

    /**
     *
     * @return validation assertion to be displayed in case of validation errors
     */
    String getValidationAssertion();

    /**
     * Load extra properties from Stream Registry configuration.
     *
     * @param configs the configs
     */
    void configure(Map<String, ?> configs);
}
