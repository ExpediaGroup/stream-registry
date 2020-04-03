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
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.avro.specific.SpecificRecord;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import com.expediagroup.streamplatform.streamregistry.avro.AvroEvent;
import com.expediagroup.streamplatform.streamregistry.avro.AvroKey;
import com.expediagroup.streamplatform.streamregistry.avro.AvroKeyType;
import com.expediagroup.streamplatform.streamregistry.avro.AvroSchema;
import com.expediagroup.streamplatform.streamregistry.avro.AvroStream;
import com.expediagroup.streamplatform.streamregistry.model.Status;
import com.expediagroup.streamplatform.streamregistry.model.StreamBinding;
import com.expediagroup.streamplatform.streamregistry.model.Tag;

@Slf4j
public class StreamBindingNotificationEventUtils {

  public static AvroKey toAvroKeyRecord(StreamBinding streamBinding) {
    return null;
  }

  public static AvroEvent toAvroValueRecord(StreamBinding streamBinding) {
    return null;

  }

  public static com.expediagroup.streamplatform.streamregistry.avro.Tag toAvroTag(Tag tag) {
    return com.expediagroup.streamplatform.streamregistry.avro.Tag.newBuilder()
        .setName(tag.getName())
        .setValue(tag.getValue())
        .build();
  }

  private static void validateStreamBindingKey(StreamBinding streamBinding) {

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