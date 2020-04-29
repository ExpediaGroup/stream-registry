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

import static com.expediagroup.streamplatform.streamregistry.core.events.ObjectNodeMapper.serialise;
import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import org.apache.avro.specific.SpecificRecord;

import com.expediagroup.streamplatform.streamregistry.avro.AvroEvent;
import com.expediagroup.streamplatform.streamregistry.avro.AvroKey;
import com.expediagroup.streamplatform.streamregistry.avro.AvroKeyType;
import com.expediagroup.streamplatform.streamregistry.avro.AvroSchema;
import com.expediagroup.streamplatform.streamregistry.avro.AvroStream;
import com.expediagroup.streamplatform.streamregistry.model.Schema;
import com.expediagroup.streamplatform.streamregistry.model.Stream;
import com.expediagroup.streamplatform.streamregistry.model.Tag;
import com.expediagroup.streamplatform.streamregistry.model.keys.SchemaKey;

@Slf4j
public class NotificationEventUtils {
  public static AvroKey toAvroKeyRecord(Schema schema) {
    validateSchemaKey(schema);

    val key = schema.getKey();
    val name = key.getName();
    val domainName = key.getDomain();

    var domain = AvroKey.newBuilder()
        .setId(domainName)
        .setType(AvroKeyType.DOMAIN)
        .build();

    return AvroKey.newBuilder()
        .setId(name)
        .setParent(domain)
        .setType(AvroKeyType.SCHEMA)
        .build();
  }

  public static AvroKey toAvroKeyRecord(SchemaKey schemaKey) {
    validateSchemaKey(schemaKey);

    var domain = AvroKey.newBuilder()
        .setId(schemaKey.getDomain())
        .setType(AvroKeyType.DOMAIN)
        .build();

    return AvroKey.newBuilder()
        .setId(schemaKey.getName())
        .setParent(domain)
        .setType(AvroKeyType.SCHEMA)
        .build();
  }

  public static AvroEvent toAvroValueRecord(Schema schema) {
    validateSchemaValue(schema);

    val key = schema.getKey();
    val specification = schema.getSpecification();

    val name = key.getName();
    val domain = key.getDomain();
    val description = specification.getDescription();

    val tags = specification.getTags()
        .stream()
        .map(NotificationEventUtils::toAvroTag)
        .collect(Collectors.toList());

    val type = specification.getType();
    val config = specification.getConfiguration();

    val status = Optional.ofNullable(schema.getStatus()).orElse(null);

    val avroSchema = AvroSchema.newBuilder()
        .setDomain(domain)
        .setName(name)
        .setDescription(description)
        .setTags(tags)
        .setType(type)
        .setConfigurationString(serialise(config))
        .setStatusString(serialise(status.getObjectNode()))
        .build();

    return AvroEvent.newBuilder()
        .setSchemaEntity(avroSchema)
        .build();
  }

  public static AvroKey toAvroKeyRecord(Stream stream) {
    validateStreamKey(stream);

    val key = stream.getKey();
    val name = key.getName();
    val version = key.getVersion();
    val domainName = key.getDomain();

    var domainKey = AvroKey.newBuilder()
        .setId(domainName)
        .setType(AvroKeyType.DOMAIN)
        .build();

    var streamKey = AvroKey.newBuilder()
        .setId(name)
        .setParent(domainKey)
        .setType(AvroKeyType.STREAM)
        .build();

    return AvroKey.newBuilder()
        .setId(version.toString())
        .setParent(streamKey)
        .setType(AvroKeyType.STREAM_VERSION)
        .build();
  }

  public static AvroEvent toAvroValueRecord(Stream stream) {
    validateStreamValue(stream);

    val key = stream.getKey();
    val specification = stream.getSpecification();
    val name = key.getName();
    val domain = key.getDomain();
    val version = key.getVersion();
    val description = specification.getDescription();
    val avroSchema = toAvroKeyRecord(stream.getSchemaKey());

    val tags = specification.getTags()
        .stream()
        .map(NotificationEventUtils::toAvroTag)
        .collect(Collectors.toList());

    val type = specification.getType();
    val config = specification.getConfiguration();

    val status = Optional.ofNullable(stream.getStatus()).orElse(null);

    val avroStream = AvroStream.newBuilder()
        .setVersion(version)
        .setDomain(domain)
        .setName(name)
        .setDescription(description)
        .setTags(tags)
        .setType(type)
        .setConfigurationString(serialise(config))
        .setStatusString(serialise(status.getObjectNode()))
        .setSchemaKey(avroSchema)
        .build();

    return AvroEvent.newBuilder()
        .setStreamEntity(avroStream)
        .build();
  }

  public static com.expediagroup.streamplatform.streamregistry.avro.Tag toAvroTag(Tag tag) {
    return com.expediagroup.streamplatform.streamregistry.avro.Tag.newBuilder()
        .setName(tag.getName())
        .setValue(tag.getValue())
        .build();
  }

  private static void validateSchemaKey(Schema schema) {
    requireNonNull(schema, canNotBeNull("schema"));
    requireNonNull(schema.getKey(), canNotBeNull("schema key"));
    requireNonNull(schema.getKey().getName(), canNotBeNull("key's name"));
    requireNonNull(schema.getKey().getDomain(), canNotBeNull("key's domain"));
  }

  private static void validateSchemaKey(SchemaKey schemaKey) {
    requireNonNull(schemaKey, canNotBeNull("schema key"));
    requireNonNull(schemaKey.getName(), canNotBeNull("schema key name"));
    requireNonNull(schemaKey.getDomain(), canNotBeNull("schema key domain"));
  }

  private static void validateSchemaValue(Schema schema) {
    validateSchemaKey(schema);
    requireNonNull(schema.getSpecification(), canNotBeNull("schema spec"));
    requireNonNull(schema.getSpecification().getDescription(), canNotBeNull("spec's description"));
    requireNonNull(schema.getSpecification().getTags(), canNotBeNull("spec's tags"));
    requireNonNull(schema.getSpecification().getType(), canNotBeNull("spec's type"));
    requireNonNull(schema.getSpecification().getConfiguration(), canNotBeNull("spec's config json"));
  }

  private static void validateStreamKey(Stream stream) {
    requireNonNull(stream, canNotBeNull("stream"));
    requireNonNull(stream.getKey(), canNotBeNull("stream key"));
    requireNonNull(stream.getKey().getName(), canNotBeNull("key's name"));
    requireNonNull(stream.getKey().getDomain(), canNotBeNull("key's domain"));
    requireNonNull(stream.getKey().getVersion(), canNotBeNull("key's version"));
  }

  private static void validateStreamValue(Stream stream) {
    validateStreamKey(stream);

    requireNonNull(stream.getSpecification(), canNotBeNull("stream spec"));
    requireNonNull(stream.getSpecification().getDescription(), canNotBeNull("spec's description"));
    requireNonNull(stream.getSpecification().getTags(), canNotBeNull("spec's tags"));
    requireNonNull(stream.getSpecification().getType(), canNotBeNull("spec's type"));
    requireNonNull(stream.getSpecification().getConfiguration(), canNotBeNull("spec's config json"));
  }

  private static String canNotBeNull(String target) {
    return String.format("%s can not be null", target);
  }

  public static <W, R extends SpecificRecord> Function<W, R> loadToAvroStaticMethod(String clazz, String methodName, Class<W> argType) throws ClassNotFoundException, NoSuchMethodException {
    val method = Class.forName(clazz)
        .getDeclaredMethod(methodName, argType);

    Function<W, R> toAvroFn = obj -> {
      try {
        // We set null as first argument, since we're expecting an static method
        return (R) method.invoke(null, obj);
      } catch (Exception e) {
        log.error("There was an error in {}.{} (toAvro) method: {}", clazz, methodName, e.getMessage(), e);
        throw new RuntimeException(e);
      }
    };

    return toAvroFn;
  }

  public static String getWarningMessageOnNotDefinedProp(String component, String property) {
    return String.format("%s prop must be configured on %s", property, component);
  }
}
