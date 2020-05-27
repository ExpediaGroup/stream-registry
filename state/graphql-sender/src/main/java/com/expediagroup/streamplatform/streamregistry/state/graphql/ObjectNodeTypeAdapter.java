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
package com.expediagroup.streamplatform.streamregistry.state.graphql;

import com.apollographql.apollo.api.CustomTypeAdapter;
import com.apollographql.apollo.api.CustomTypeValue;
import com.apollographql.apollo.api.CustomTypeValue.GraphQLJsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ObjectNodeTypeAdapter implements CustomTypeAdapter<Object> {
  private static final ObjectMapper mapper = new ObjectMapper();

  @Override
  public CustomTypeValue<?> encode(Object value) {
    if (!(value instanceof ObjectNode)) {
      throw new IllegalArgumentException("Expected 'ObjectNode', got: " + value);
    }
    return CustomTypeValue.fromRawValue(value);
  }

  @Override
  public Object decode(CustomTypeValue value) {
    if (!(value instanceof GraphQLJsonObject)) {
      throw new IllegalArgumentException("Expected 'GraphQLJsonObject', got: " + value);
    }
    return mapper.convertValue(value.value, JsonNode.class);
  }
}
