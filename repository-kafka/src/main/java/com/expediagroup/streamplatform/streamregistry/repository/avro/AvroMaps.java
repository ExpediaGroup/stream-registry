/**
 * Copyright (C) 2018-2019 Expedia, Inc.
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
package com.expediagroup.streamplatform.streamregistry.repository.avro;

import static java.util.stream.Collectors.toMap;

import java.util.Map;

public final class AvroMaps {
  private AvroMaps() {}

  public static Map<CharSequence, CharSequence> fromDto(Map<String, String> map) {
    return map
        .entrySet()
        .stream()
        .collect(toMap(
            e -> e.getKey(),
            e -> e.getValue()
        ));
  }

  public static Map<String, String> toDto(Map<CharSequence, CharSequence> map) {
    return map
        .entrySet()
        .stream()
        .collect(toMap(
            e -> e.getKey().toString(),
            e -> e.getValue().toString()
        ));
  }
}
