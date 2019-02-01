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
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import com.google.common.base.Preconditions;

import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.RestService;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;

import com.homeaway.streamplatform.streamregistry.exceptions.SchemaException;
import com.homeaway.streamplatform.streamregistry.exceptions.SchemaManagerException;

@Slf4j
public class ConfluentSchemaManager implements SchemaManager {

    private SchemaRegistryClient schemaRegistryClient;

    private RestService schemaRegistryRestService;

    @Override
    public SchemaReference registerSchema(String subject, String schema) throws SchemaManagerException {
        SchemaReference schemaReference;
        try {
            org.apache.avro.Schema avroSchema = new org.apache.avro.Schema.Parser().parse(schema);
            // TODO: CachedSchemaRegistry has a bug on registering schemas which is addressed at https://github.com/confluentinc/schema-registry/pull/827
            // more details at (#100).
            int id = this.register(subject, avroSchema);
            int version = schemaRegistryClient.getLatestSchemaMetadata(subject).getVersion();
            log.info("Schema registration successful. Subject={} ; id={} ; version={}", subject, id, version);
            schemaReference = new SchemaReference(subject, id, version);
        } catch (IOException | RestClientException | RuntimeException e) {
            log.error("caught an exception while registering a new schema={} for subject='{}'", schema, subject);
            throw new SchemaManagerException(e);
        }

        return schemaReference;
    }

    /**
     * Register the avro schema for the subject.
     *
     * @param subject
     * @param avroSchema
     * @return
     * @throws IOException
     * @throws RestClientException
     */
    private int register(String subject, org.apache.avro.Schema avroSchema) throws IOException, RestClientException {
        return schemaRegistryRestService.registerSchema(avroSchema.toString(), subject);
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

        schemaRegistryRestService = new RestService(schemaRegistryUrl);
        schemaRegistryClient = new CachedSchemaRegistryClient(schemaRegistryRestService, 100);
    }
}
