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
package com.expediagroup.streamplatform.streamregistry.graphql.model;

import static java.util.stream.Collectors.toList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

import lombok.Value;

@Value
public class GraphQLKeyValue {
  String key;
  String value;

  public static Iterable<GraphQLKeyValue> fromDto(Map<String, String> map) {
    if (map == null) {
      return List.of();
    }
    return map
        .entrySet()
        .stream()
        .map(e -> new GraphQLKeyValue(e.getKey(), e.getValue()))
        .collect(toList());
  }

  public static Map<String, String> toDto(Iterable<GraphQLKeyValue> keyValues) {
    if (keyValues == null) {
      return Map.of();
    }
    return StreamSupport
        .stream(keyValues.spliterator(), false)
        //Allow null keys and values in map but not duplicate keys
        .collect(HashMap::new, (m, kv) -> {
          if (m.containsKey(kv.getKey())) {
            throw new IllegalArgumentException("Duplicate key: " + kv.getKey());
          }
          m.put(kv.getKey(), kv.getValue());
        }, HashMap::putAll);
  }
}
