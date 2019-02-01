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

import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import lombok.extern.slf4j.Slf4j;

import com.google.common.base.Preconditions;

import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;

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
        try {
            if(cachedSchemaIdMap.containsKey(schema)) {
                SchemaMetadata schemaMetadata = schemaRegistryClient.getLatestSchemaMetadata(subject);
                log.info("Schema registration cached. Subject={} ; id={} ; version={}", subject, schemaMetadata.getId(), schemaMetadata.getVersion());
                return new SchemaReference(subject, schemaMetadata.getId(), schemaMetadata.getVersion());
            }

            org.apache.avro.Schema avroSchema = new org.apache.avro.Schema.Parser().parse(schema);
            int id = schemaRegistryClient.register(subject, avroSchema);
            // TODO ... do we really want latest... shouldn't we be looking up metadata by the id that was just returned?
            int version = schemaRegistryClient.getLatestSchemaMetadata(subject).getVersion();
            log.info("Schema registration successful. Subject={} ; id={} ; version={}", subject, id, version);
            return new SchemaReference(subject, id, version);
        } catch (IOException | RestClientException | RuntimeException e) {
            log.error("caught an exception while registering a new schema={} for subject='{}'", schema, subject, e);
            throw new SchemaManagerException(e);
        }
    }

    @Override
    public boolean checkCompatibility(String subject, String schema) throws SchemaException {
        try {
            org.apache.avro.Schema avroSchema = new org.apache.avro.Schema.Parser().parse(schema);
            return !schemaRegistryClient.getAllSubjects().contains(subject)
                    || schemaRegistryClient.testCompatibility(subject, avroSchema);
        } catch (IOException | RestClientException e) {
            String message = String.format("caught an exception while checking compatibility for subject '%s'", subject);
            log.error(message);
            throw new SchemaException(message, e);
        }
    }

    @Override
    public void configure(Map<String, Object> configs) {
        Preconditions.checkState(configs.containsKey(SCHEMA_REGISTRY_URL_CONFIG));
        String schemaRegistryUrl = (String) configs.get(SCHEMA_REGISTRY_URL_CONFIG);

        schemaRegistryClient = new CachedSchemaRegistryClient(schemaRegistryUrl, 100);
    }
}
