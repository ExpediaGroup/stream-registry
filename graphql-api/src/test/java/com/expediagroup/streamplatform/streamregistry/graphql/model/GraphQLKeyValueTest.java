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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class GraphQLKeyValueTest {
  private final Map<String, String> map = mapOf(
      "key1", "value1",
      null, "value2",
      "key3", null
  );

  private final List<GraphQLKeyValue> keyValues = List.of(
      new GraphQLKeyValue("key1", "value1"),
      new GraphQLKeyValue(null, "value2"),
      new GraphQLKeyValue("key3", null)
  );

  private static Map<String, String> mapOf(String... kvs) {
    Map<String, String> map = new HashMap<>();
    int i = 0;
    while (i < kvs.length - 1) {
      map.put(kvs[i++], kvs[i++]);
    }
    return map;
  }

  @Test
  public void fromDto() {
    assertThat(GraphQLKeyValue.toMap(map), is(keyValues));
  }

  @Test
  public void toDto() {
    assertThat(GraphQLKeyValue.toList(keyValues), is(map));
  }

  @Test(expected = IllegalArgumentException.class)
  public void toDtoDuplicateKeys() {
    List<GraphQLKeyValue> keyValues = List.of(
        new GraphQLKeyValue("key", "value1"),
        new GraphQLKeyValue("key", "value2")
    );
    GraphQLKeyValue.toList(keyValues);
  }
}
