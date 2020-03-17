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

import static com.google.common.base.Preconditions.checkNotNull;

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
import com.expediagroup.streamplatform.streamregistry.model.Status;
import com.expediagroup.streamplatform.streamregistry.model.Stream;
import com.expediagroup.streamplatform.streamregistry.model.Tag;
import com.expediagroup.streamplatform.streamregistry.model.keys.SchemaKey;

@Slf4j
public class NotificationEventUtils {
  public static AvroKey toAvroKeyRecord(Schema schema) {
    validateSchemaKey(schema);

    val name = schema.getKey().getName();
    val domainName = schema.getKey().getDomain();

    var domain = AvroKey.newBuilder()
        .setId(domainName)
        .setType(AvroKeyType.DOMAIN)
        .setParent(null)
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
        .setParent(null)
        .build();

    return AvroKey.newBuilder()
        .setId(schemaKey.getName())
        .setParent(domain)
        .setType(AvroKeyType.SCHEMA)
        .build();
  }

  public static AvroEvent toAvroValueRecord(Schema schema) {
    validateSchemaValue(schema);

    val name = schema.getKey().getName();
    val domain = schema.getKey().getDomain();
    val description = schema.getSpecification().getDescription();

    val tags = schema.getSpecification()
        .getTags()
        .stream()
        .map(NotificationEventUtils::toAvroTag)
        .collect(Collectors.toList());

    val type = schema.getSpecification().getType();
    val configJson = schema.getSpecification().getConfigJson();

    val statusJson = Optional.ofNullable(schema.getStatus())
        .map(Status::getStatusJson)
        .orElse(null);

    val avroSchema = AvroSchema.newBuilder()
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

  public static AvroKey toAvroKeyRecord(Stream stream) {
    validateStreamKey(stream);

    val name = stream.getKey().getName();
    val version = stream.getKey().getVersion();
    val domainName = stream.getKey().getDomain();

    var domainKey = AvroKey.newBuilder()
        .setId(domainName)
        .setType(AvroKeyType.DOMAIN)
        .setParent(null)
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

    val name = stream.getKey().getName();
    val domain = stream.getKey().getDomain();
    val version = stream.getKey().getVersion();
    val description = stream.getSpecification().getDescription();
    val avroSchema = toAvroKeyRecord(stream.getSchemaKey());

    val tags = stream.getSpecification()
        .getTags()
        .stream()
        .map(NotificationEventUtils::toAvroTag)
        .collect(Collectors.toList());

    val type = stream.getSpecification().getType();
    val configJson = stream.getSpecification().getConfigJson();

    val statusJson = Optional.ofNullable(stream.getStatus())
        .map(Status::getStatusJson)
        .orElse(null);

    val avroStream = AvroStream.newBuilder()
        .setVersion(version)
        .setDomain(domain)
        .setName(name)
        .setDescription(description)
        .setTags(tags)
        .setType(type)
        .setConfigurationString(configJson)
        .setStatusString(statusJson)
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
    checkNotNull(schema, canNotBeNull("schema"));
    checkNotNull(schema.getKey(), canNotBeNull("schema key"));
    checkNotNull(schema.getKey().getName(), canNotBeNull("key's name"));
    checkNotNull(schema.getKey().getDomain(), canNotBeNull("key's domain"));
  }

  private static void validateSchemaKey(SchemaKey schemaKey) {
    checkNotNull(schemaKey, canNotBeNull("schema key"));
    checkNotNull(schemaKey.getName(), canNotBeNull("schema key name"));
    checkNotNull(schemaKey.getDomain(), canNotBeNull("schema key domain"));
  }

  private static void validateSchemaValue(Schema schema) {
    validateSchemaKey(schema);
    checkNotNull(schema.getSpecification(), canNotBeNull("schema spec"));
    checkNotNull(schema.getSpecification().getDescription(), canNotBeNull("spec's description"));
    checkNotNull(schema.getSpecification().getTags(), canNotBeNull("spec's tags"));
    checkNotNull(schema.getSpecification().getType(), canNotBeNull("spec's type"));
    checkNotNull(schema.getSpecification().getConfigJson(), canNotBeNull("spec's config json"));
  }

  private static void validateStreamKey(Stream stream) {
    checkNotNull(stream, canNotBeNull("stream"));
    checkNotNull(stream.getKey(), canNotBeNull("stream key"));
    checkNotNull(stream.getKey().getName(), canNotBeNull("key's name"));
    checkNotNull(stream.getKey().getDomain(), canNotBeNull("key's domain"));
    checkNotNull(stream.getKey().getVersion(), canNotBeNull("key's version"));
  }

  private static void validateStreamValue(Stream stream) {
    validateStreamKey(stream);

    checkNotNull(stream.getSpecification(), canNotBeNull("stream spec"));
    checkNotNull(stream.getSpecification().getDescription(), canNotBeNull("spec's description"));
    checkNotNull(stream.getSpecification().getTags(), canNotBeNull("spec's tags"));
    checkNotNull(stream.getSpecification().getType(), canNotBeNull("spec's type"));
    checkNotNull(stream.getSpecification().getConfigJson(), canNotBeNull("spec's config json"));
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