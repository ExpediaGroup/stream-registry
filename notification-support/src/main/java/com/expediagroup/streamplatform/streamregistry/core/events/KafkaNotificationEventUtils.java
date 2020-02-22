/**
 * Copyright (C) 2018-2020 Expedia, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.expediagroup.streamplatform.streamregistry.core.events;

import static com.google.common.base.Preconditions.*;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import lombok.extern.slf4j.Slf4j;

import org.apache.avro.specific.SpecificRecord;

import com.expediagroup.streamplatform.streamregistry.avro.AvroEvent;
import com.expediagroup.streamplatform.streamregistry.avro.AvroKey;
import com.expediagroup.streamplatform.streamregistry.avro.AvroKeyType;
import com.expediagroup.streamplatform.streamregistry.avro.AvroSchema;
import com.expediagroup.streamplatform.streamregistry.model.Schema;
import com.expediagroup.streamplatform.streamregistry.model.Status;
import com.expediagroup.streamplatform.streamregistry.model.Tag;

@Slf4j
public class KafkaNotificationEventUtils {
    public static AvroKey toAvroKeyRecord(Schema schema) {
        validateSchemaKey(schema);
        KafkaAvroSerializer a = null;
        final String name = schema.getKey().getName();
        final String domain = schema.getKey().getDomain();

        return AvroKey.newBuilder()
                .setId(name)
                .setDomain(domain)
                .setVersion(null)
                .setParent(null)
                .setType(AvroKeyType.SCHEMA)
                .build();
    }

    public static AvroEvent toAvroValueRecord(Schema schema) {
        validateSchemaValue(schema);

        final String name = schema.getKey().getName();
        final String domain = schema.getKey().getDomain();
        final String description = schema.getSpecification().getDescription();

        final List<com.expediagroup.streamplatform.streamregistry.avro.Tag> tags = schema.getSpecification()
                .getTags()
                .stream()
                .map(KafkaNotificationEventUtils::toAvroTag)
                .collect(Collectors.toList());

        final String type = schema.getSpecification().getType();
        final String configJson = schema.getSpecification().getConfigJson();

        final String statusJson = Optional.ofNullable(schema.getStatus())
                .map(Status::getStatusJson)
                .orElse(null);

        AvroSchema avroSchema = AvroSchema.newBuilder()
                .setDomain(domain)
                .setName(name)
                .setDescription(description)
                .setTags(tags)
                .setType(type)
                .setConfigurationString(configJson)
                .setStatusString(statusJson)
                .build();

        return AvroEvent.newBuilder()
                .setSchemaEntity(avroSchema)
                .build();
    }

    public static com.expediagroup.streamplatform.streamregistry.avro.Tag toAvroTag(Tag tag) {
        return com.expediagroup.streamplatform.streamregistry.avro.Tag.newBuilder()
                .setName(tag.getName())
                .setValue(tag.getValue())
                .build();
    }

    private static void validateSchemaKey(Schema schema) {
        checkNotNull(schema, canNotBeNull("schema"));
        checkNotNull(schema.getKey(), canNotBeNull("schema key"));
        checkNotNull(schema.getKey().getName(), canNotBeNull("key's name"));
        checkNotNull(schema.getKey().getDomain(), canNotBeNull("key's domain"));
    }

    private static void validateSchemaValue(Schema schema) {
        validateSchemaKey(schema);
        checkNotNull(schema.getSpecification(), canNotBeNull("schema spec"));
        checkNotNull(schema.getSpecification().getDescription(), canNotBeNull("spec's description"));
        checkNotNull(schema.getSpecification().getTags(), canNotBeNull("spec's tags"));
        checkNotNull(schema.getSpecification().getType(), canNotBeNull("spec's type"));
        checkNotNull(schema.getSpecification().getConfigJson(), canNotBeNull("spec's config json"));
    }

    private static String canNotBeNull(String target) {
        return String.format("%s can not be null", target);
    }

    public static <W, R extends SpecificRecord> Function<W, R> loadToAvroStaticMethod(String clazz, String methodName, Class<W> argType) throws ClassNotFoundException, NoSuchMethodException {
        Method method = Class.forName(clazz)
                .getDeclaredMethod(methodName, argType);

        Function<W, R> toAvroFn = schema -> {
            try {
                // We set null as first argument, since we're expecting an static method
                return (R) method.invoke(null, schema);
            } catch (Exception e) {
                log.error("There was an error in {}.{} (toAvro) method: {}", clazz, methodName, e.getMessage(), e);
                throw new RuntimeException(e);
            }
        };

        return toAvroFn;
    }
}