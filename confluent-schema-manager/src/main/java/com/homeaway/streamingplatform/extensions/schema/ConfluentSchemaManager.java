package com.homeaway.streamingplatform.extensions.schema;

import com.google.common.base.Preconditions;
import com.homeaway.streamingplatform.exceptions.SchemaException;
import com.homeaway.streamingplatform.exceptions.SchemaManagerException;
import com.homeaway.streamingplatform.extensions.schema.SchemaManager;
import com.homeaway.streamingplatform.extensions.schema.SchemaReference;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Map;

import static com.homeaway.streamingplatform.configuration.SchemaManagerConfig.SCHEMA_REGISTRY_URL;

@Slf4j
public class ConfluentSchemaManager implements SchemaManager {

    private SchemaRegistryClient schemaRegistryClient;

    @Override
    public SchemaReference registerSchema(String subject, String schema) throws SchemaManagerException {
        org.apache.avro.Schema avroSchema = new org.apache.avro.Schema.Parser().parse(schema);

        SchemaReference schemaReference;
        try {
            long id = schemaRegistryClient.register(subject, avroSchema);
            long version = schemaRegistryClient.getLatestSchemaMetadata(subject).getVersion();
            schemaReference = new SchemaReference(subject, id, version);
        } catch (IOException e) {
            log.error("caught an IOException while registering a new schema for subject '{}'", subject);
            throw new SchemaManagerException(e);
        } catch (RestClientException e) {
            log.error("caught a RestClientException while registering a new schema for subject '{}'", subject);
            throw new SchemaManagerException(e);
        }

        return schemaReference;
    }

    @Override
    public boolean checkCompatibility(String subject, String schema) throws SchemaException {
        org.apache.avro.Schema avroSchema = new org.apache.avro.Schema.Parser().parse(schema);
        try {
            return schemaRegistryClient.testCompatibility(subject, avroSchema);
        } catch (IOException e) {
            log.error("caught an IOException while checking compatibility for subject '{}'", subject);
            throw new SchemaException(e);
        } catch (RestClientException e) {
            log.error("caught a RestClientException while checking compatibility for subject '{}'", subject);
            throw new SchemaException(e);
        }
    }

    @Override
    public void configure(Map<String, Object> configs) {
        Preconditions.checkState(configs.containsKey(SCHEMA_REGISTRY_URL));
        String schemaRegistryUrl = (String) configs.get(SCHEMA_REGISTRY_URL);

        schemaRegistryClient = new CachedSchemaRegistryClient(schemaRegistryUrl, 100);
    }
}
