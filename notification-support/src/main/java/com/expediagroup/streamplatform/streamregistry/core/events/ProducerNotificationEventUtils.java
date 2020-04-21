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

import static com.expediagroup.streamplatform.streamregistry.data.ObjectNodeMapper.serialise;
import static java.util.Objects.requireNonNull;

import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import com.expediagroup.streamplatform.streamregistry.avro.AvroEvent;
import com.expediagroup.streamplatform.streamregistry.avro.AvroKey;
import com.expediagroup.streamplatform.streamregistry.avro.AvroKeyType;
import com.expediagroup.streamplatform.streamregistry.avro.AvroProducer;
import com.expediagroup.streamplatform.streamregistry.model.Producer;

@Slf4j
public class ProducerNotificationEventUtils {

  public static AvroKey toAvroKeyRecord(Producer producer) {
    validateProducerKey(producer);
    val key = producer.getKey();
    val producerName = key.getName();
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
        .setId(producerName)
        .setParent(streamVersionKey)
        .setType(AvroKeyType.PRODUCER)
        .build();
  }

  public static AvroEvent toAvroValueRecord(Producer producer) {
    validateProducerValue(producer);
    val key = producer.getKey();
    val producerName = key.getName();
    val specification = producer.getSpecification();
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
        producer == null || producer.getStatus() == null || producer.getStatus().getObjectNode() == null
            ? null
            : serialise(producer.getStatus().getObjectNode());

    val avroProducer = AvroProducer.newBuilder()
        .setName(producerName)
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
        .setProducerEntity(avroProducer)
        .build();
  }

  private static void validateProducerKey(Producer producer) {
    requireNonNull(producer, canNotBeNull("producer"));
    requireNonNull(producer.getKey(), canNotBeNull("producer's key"));
    requireNonNull(producer.getKey().getName(), canNotBeNull("key's name"));
    requireNonNull(producer.getKey().getStreamName(), canNotBeNull("key's streamName"));
    requireNonNull(producer.getKey().getStreamDomain(), canNotBeNull("key's streamDomain"));
    requireNonNull(producer.getKey().getStreamVersion(), canNotBeNull("key's streamVersion"));
    requireNonNull(producer.getKey().getZone(), canNotBeNull("key's zone"));
  }

  private static void validateProducerValue(Producer producer) {
    validateProducerKey(producer);

    requireNonNull(producer.getSpecification(), canNotBeNull("producer spec"));
    requireNonNull(producer.getSpecification().getDescription(), canNotBeNull("spec's description"));
    requireNonNull(producer.getSpecification().getTags(), canNotBeNull("spec's tags"));
    requireNonNull(producer.getSpecification().getType(), canNotBeNull("spec's type"));
    requireNonNull(producer.getSpecification().getConfiguration(), canNotBeNull("spec's config json"));
  }

  private static String canNotBeNull(String target) {
    return String.format("%s can not be null", target);
  }
}