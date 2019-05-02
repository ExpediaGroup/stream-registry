/* Copyright (c) 2018-2019 Expedia, Inc.
 * All rights reserved.  http://www.expediagroup.com

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
package com.expediagroup.streamplatform.streamregistry.extensions.schema;

import java.util.Map;

import com.expediagroup.streamplatform.streamregistry.exceptions.SchemaManagerException;

/**
 * This interface is implemented by a stream provider to provide
 * organization specific requirement to the underlying schema registry.
 */
public interface SchemaManager {

    String MAX_SCHEMA_VERSIONS_CAPACITY = "max.schema.version.capacity";

    /**
     * Register a new schema under the specified subject. If successfully
     * registered, this returns the unique identifier of this schema in
     * the registry. The returned identifier should be used to retrieve
     * this schema from the schemas resource and is different from the
     * schema’s version which is associated with the subject. If the
     * same schema is registered under a different subject, the same
     * identifier will be returned. However, the version of the schema
     * may be different under different subjects.
     *
     * A schema should be compatible with the previously registered schema
     * or schemas (if there are any) as per the configured compatibility level.
     * @param subject - Subject under which the schema will be registered
     * @param schema - The Avro schema string
     * @return a SchemaReference for the registered schema
     * @throws SchemaManagerException - when the schema fails to register
     */
    SchemaReference registerSchema(String subject, String schema) throws SchemaManagerException;

    /**
     * Test input schema against a particular version of a
     * subject’s schema for compatibility.
     * @param subject - Subject under which the comparative schema will be checked against
     * @param schema - the schema to check against a version of the subject's schema
     * @return boolean
     */
    boolean checkCompatibility(String subject, String schema);

    /**
     * Configure the SchemaManager with SchemaRegistry endpoints and properties
     * @param configs - Map containing SchemaRegistry configurations
     */
    void configure(Map<String, Object> configs);
}
