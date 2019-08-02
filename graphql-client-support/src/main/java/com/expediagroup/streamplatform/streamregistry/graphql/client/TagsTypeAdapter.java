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
package com.expediagroup.streamplatform.streamregistry.graphql.client;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;

import com.apollographql.apollo.response.CustomTypeAdapter;
import com.apollographql.apollo.response.CustomTypeValue;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TagsTypeAdapter implements CustomTypeAdapter<Object> {
  private static final ObjectMapper mapper = new ObjectMapper();
  private static final TypeReference<Map<String, String>> mapStringString = new TypeReference<>() {
  };

  @Override
  public CustomTypeValue encode(Object value) {
    if (!(value instanceof Map)) {
      throw new IllegalArgumentException("Expected 'Map', got: " + value);
    }
    return CustomTypeValue.fromRawValue(value);
  }

  @Override
  public Object decode(CustomTypeValue value) {
    try {
      return mapper.readValue(value.value.toString(), mapStringString);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
