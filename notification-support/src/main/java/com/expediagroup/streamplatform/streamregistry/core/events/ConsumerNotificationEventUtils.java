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

import java.util.stream.Collectors;

import lombok.val;

import com.expediagroup.streamplatform.streamregistry.avro.AvroConsumer;
import com.expediagroup.streamplatform.streamregistry.avro.AvroEvent;
import com.expediagroup.streamplatform.streamregistry.avro.AvroKey;
import com.expediagroup.streamplatform.streamregistry.avro.AvroKeyType;
import com.expediagroup.streamplatform.streamregistry.model.Consumer;

public class ConsumerNotificationEventUtils {

  public static AvroKey toAvroKeyRecord(Consumer consumer) {
    validateConsumerKey(consumer);
    val key = consumer.getKey();
    val consumerName = key.getName();
    val streamName = key.getStreamName();
    val streamVersion = key.getStreamVersion();
    val streamDomain = key.getStreamDomain();
    val zone = key.getZone();

    var domainKey = AvroKey.newBuilder()
        .setId(streamDomain)
        .setType(AvroKeyType.DOMAIN)
        .build();

    var zoneKey = AvroKey.newBuilder()
        .setId(zone)
        .setParent(domainKey)
        .setType(AvroKeyType.ZONE)
        .build();

    var streamKey = AvroKey.newBuilder()
        .setId(streamName)
        .setParent(zoneKey)
        .setType(AvroKeyType.STREAM)
        .build();

    var streamVersionKey = AvroKey.newBuilder()
        .setId(streamVersion.toString())
        .setParent(streamKey)
        .setType(AvroKeyType.STREAM_VERSION)
        .build();

    return AvroKey.newBuilder()
        .setId(consumerName)
        .setParent(streamVersionKey)
        .setType(AvroKeyType.CONSUMER)
        .build();
  }

  public static AvroEvent toAvroValueRecord(Consumer consumer) {
    validateConsumerValue(consumer);
    val key = consumer.getKey();
    val consumerName = key.getName();
    val specification = consumer.getSpecification();
    val streamName = key.getStreamName();
    val streamVersion = key.getStreamVersion();
    val streamDomain = key.getStreamDomain();
    val zone = key.getZone();
    val description = specification.getDescription();

    val tags = specification.getTags()
        .stream()
        .map(NotificationEventUtils::toAvroTag)
        .collect(Collectors.toList());

    val type = specification.getType();
    String config = serialise(specification.getConfiguration());

    String statusJson =
        consumer == null || consumer.getStatus() == null || consumer.getStatus().getObjectNode() == null
            ? null
            : serialise(consumer.getStatus().getObjectNode());

    val avroConsumer = AvroConsumer.newBuilder()
        .setName(consumerName)
        .setStreamVersion(streamVersion)
        .setStreamDomain(streamDomain)
        .setStreamName(streamName)
        .setZone(zone)
        .setDescription(description)
        .setTags(tags)
        .setType(type)
        .setConfigurationString(config)
        .setStatusString(statusJson)
        .build();

    return AvroEvent.newBuilder()
        .setConsumerEntity(avroConsumer)
        .build();
  }

  private static void validateConsumerKey(Consumer consumer) {
    requireNonNull(consumer, canNotBeNull("consumer"));
    requireNonNull(consumer.getKey(), canNotBeNull("consumer's key"));
    requireNonNull(consumer.getKey().getName(), canNotBeNull("key's name"));
    requireNonNull(consumer.getKey().getStreamName(), canNotBeNull("key's streamName"));
    requireNonNull(consumer.getKey().getStreamDomain(), canNotBeNull("key's streamDomain"));
    requireNonNull(consumer.getKey().getStreamVersion(), canNotBeNull("key's streamVersion"));
    requireNonNull(consumer.getKey().getZone(), canNotBeNull("key's zone"));
  }

  private static void validateConsumerValue(Consumer consumer) {
    validateConsumerKey(consumer);

    requireNonNull(consumer.getSpecification(), canNotBeNull("consumer spec"));
    requireNonNull(consumer.getSpecification().getDescription(), canNotBeNull("spec's description"));
    requireNonNull(consumer.getSpecification().getTags(), canNotBeNull("spec's tags"));
    requireNonNull(consumer.getSpecification().getType(), canNotBeNull("spec's type"));
    requireNonNull(consumer.getSpecification().getConfiguration(), canNotBeNull("spec's config json"));
  }

  private static String canNotBeNull(String target) {
    return String.format("%s can not be null", target);
  }
}