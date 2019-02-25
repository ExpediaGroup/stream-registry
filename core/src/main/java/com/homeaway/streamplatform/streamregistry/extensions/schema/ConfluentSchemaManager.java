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
package com.homeaway.streamplatform.streamregistry.extensions.schema;

import static io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import com.google.common.base.Preconditions;

import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;

import org.apache.avro.Schema;

import com.homeaway.streamplatform.streamregistry.exceptions.SchemaException;
import com.homeaway.streamplatform.streamregistry.exceptions.SchemaManagerException;

@Slf4j
public class ConfluentSchemaManager implements SchemaManager {

    private SchemaRegistryClient schemaRegistryClient;

    // TODO Workaround until https://github.com/confluentinc/schema-registry/pull/827 is FIXED.
    //      Keep this around until ^^^^ that bug is fixed.
    private Map<String, Integer> cachedSchemaIdMap = new HashMap<>();

    @Override
    public SchemaReference registerSchema(String subject, String schema) throws SchemaManagerException {
        Preconditions.checkNotNull(subject, "Subject should not be null");
        Preconditions.checkNotNull(schema, "Schema should not be null");
        try {
            // TODO Workaround until https://github.com/confluentinc/schema-registry/pull/827 is FIXED.
            //      Keep this around until ^^^^ that bug is fixed.
            String subjectSchemaCacheKey = subject + "-" + schema;
            Integer cachedId = cachedSchemaIdMap.get(subjectSchemaCacheKey);
            if(cachedId != null) {
                Schema confluentSchema = schemaRegistryClient.getById(cachedId);
                SchemaMetadata schemaMetadata = schemaRegistryClient.getSchemaMetadata(subject,
                        schemaRegistryClient.getVersion(subject, confluentSchema));
                log.info("Schema registration cached. Subject={} ; id={} ; version={}", subject, schemaMetadata.getId(), schemaMetadata.getVersion());
                return new SchemaReference(subject, schemaMetadata.getId(), schemaMetadata.getVersion());
            }

            // TODO Workaround until https://github.com/confluentinc/schema-registry/pull/827 is FIXED.
            //      until ^^bug fixed, need to ensure we call register only when its not cached.
            Schema avroSchema = new Schema.Parser().parse(schema);
            int id = schemaRegistryClient.register(subject, avroSchema);
            int version = schemaRegistryClient.getVersion(subject, avroSchema);

            // TODO Workaround until https://github.com/confluentinc/schema-registry/pull/827 is FIXED.
            //      Keep this around until ^^^^ that bug is fixed. Cache the id.
            cachedSchemaIdMap.put(subjectSchemaCacheKey, id);

            log.info("Schema registration successful. Subject={} ; id={} ; version={}", subject, id, version);
            return new SchemaReference(subject, id, version);
        } catch (IOException | RestClientException | RuntimeException e) {
            throw new SchemaManagerException(String.format("Could not register new schema=%s for subject=%s", schema, subject), e);
        }
    }

    @Override
    public boolean checkCompatibility(String subject, String schema) throws SchemaException {
        try {
            org.apache.avro.Schema avroSchema = new org.apache.avro.Schema.Parser().parse(schema);
            return schemaRegistryClient.testCompatibility(subject, avroSchema);
        } catch (RestClientException restClientException) {
            if (restClientException.getErrorCode()==40401) {
                log.debug("Subject '{}' does not exist in schema-registry", subject);
                return true;
            }
            String message = String.format("Could not check compatibility for subject '%s'", subject);
            throw new SchemaException(message, restClientException);
        } catch (IOException e) {
            String message = String.format("Could not check compatibility for subject '%s'", subject);
            throw new SchemaException(message, e);
        }
    }

    @Override
    public void configure(Map<String, Object> configs) {
        Preconditions.checkState(configs.containsKey(SCHEMA_REGISTRY_URL_CONFIG));
        Preconditions.checkState(configs.containsKey(MAX_SCHEMA_VERSIONS_CAPACITY));

        String schemaRegistryUrl = (String) configs.get(SCHEMA_REGISTRY_URL_CONFIG);
        int identityMapCapacity = (int)configs.get(MAX_SCHEMA_VERSIONS_CAPACITY);

        schemaRegistryClient = new CachedSchemaRegistryClient(schemaRegistryUrl, identityMapCapacity);
    }
}
