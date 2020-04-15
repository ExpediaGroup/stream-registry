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

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import com.expediagroup.streamplatform.streamregistry.avro.AvroEvent;
import com.expediagroup.streamplatform.streamregistry.avro.AvroKey;
import com.expediagroup.streamplatform.streamregistry.avro.AvroKeyType;
import com.expediagroup.streamplatform.streamregistry.avro.AvroStreamBinding;
import com.expediagroup.streamplatform.streamregistry.model.Status;
import com.expediagroup.streamplatform.streamregistry.model.StreamBinding;
import com.expediagroup.streamplatform.streamregistry.model.keys.InfrastructureKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.StreamKey;

@Slf4j
public class StreamBindingNotificationEventUtils {

  public static AvroKey toAvroKeyRecord(StreamBinding streamBinding) {
    validateStreamBindingKey(streamBinding);

    val key = streamBinding.getKey();

    val avroStreamKey = NotificationEventUtils.toAvroKeyRecord(new StreamKey(key.getStreamDomain(), key.getStreamName(), key.getStreamVersion()));
    val avroInfrastructureKey = NotificationEventUtils.toAvroKeyRecord(new InfrastructureKey(key.getInfrastructureZone(), key.getInfrastructureName()));

    return AvroKey.newBuilder()
        .setId(key.getStreamVersion().toString())
        .setParent(avroStreamKey)
        .setPhysical(avroInfrastructureKey)
        .setType(AvroKeyType.STREAM_BINDING)
        .build();
  }

  public static AvroEvent toAvroValueRecord(StreamBinding streamBinding) {
    validateStreamBindingValue(streamBinding);

    val key = streamBinding.getKey();
    val specification = streamBinding.getSpecification();

    val avroStreamKey = NotificationEventUtils.toAvroKeyRecord(new StreamKey(key.getStreamDomain(), key.getStreamName(), key.getStreamVersion()));
    val avroInfrastructureKey = NotificationEventUtils.toAvroKeyRecord(new InfrastructureKey(key.getInfrastructureZone(), key.getInfrastructureName()));

    val description = specification.getDescription();

    val tags = specification.getTags()
        .stream()
        .map(NotificationEventUtils::toAvroTag)
        .collect(Collectors.toList());

    val type = specification.getType();
    val configJson = specification.getConfigJson();

    val statusJson = Optional.ofNullable(streamBinding.getStatus())
        .map(Status::getStatusJson)
        .orElse(null);

    val avroStreamBinding = AvroStreamBinding.newBuilder()
        .setStreamKey(avroStreamKey)
        .setInfrastructureKey(avroInfrastructureKey)
        .setDescription(description)
        .setTags(tags)
        .setType(type)
        .setConfigurationString(configJson)
        .setStatusString(statusJson)
        .build();

    return AvroEvent.newBuilder()
        .setStreamBindingEntity(avroStreamBinding)
        .build();
  }

  private static void validateStreamBindingKey(StreamBinding streamBinding) {
    requireNonNull(streamBinding, canNotBeNull("streamBinding"));
    requireNonNull(streamBinding.getKey(), canNotBeNull("streamBinding's key"));
    requireNonNull(streamBinding.getKey().getStreamName(), canNotBeNull("key's streamName"));
    requireNonNull(streamBinding.getKey().getStreamDomain(), canNotBeNull("key's streamDomain"));
    requireNonNull(streamBinding.getKey().getStreamVersion(), canNotBeNull("key's streamVersion"));
    requireNonNull(streamBinding.getKey().getInfrastructureName(), canNotBeNull("key's infrastructureName"));
    requireNonNull(streamBinding.getKey().getInfrastructureZone(), canNotBeNull("key's infrastructureZone"));
  }

  private static void validateStreamBindingValue(StreamBinding streamBinding) {
    validateStreamBindingKey(streamBinding);

    requireNonNull(streamBinding.getSpecification(), canNotBeNull("stream spec"));
    requireNonNull(streamBinding.getSpecification().getDescription(), canNotBeNull("spec's description"));
    requireNonNull(streamBinding.getSpecification().getTags(), canNotBeNull("spec's tags"));
    requireNonNull(streamBinding.getSpecification().getType(), canNotBeNull("spec's type"));
    requireNonNull(streamBinding.getSpecification().getConfigJson(), canNotBeNull("spec's config json"));
  }

  private static String canNotBeNull(String target) {
    return String.format("%s can not be null", target);
  }
}