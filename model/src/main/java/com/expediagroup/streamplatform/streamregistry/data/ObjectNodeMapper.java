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
package com.expediagroup.streamplatform.streamregistry.data;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ObjectNodeMapper {

  private static final ObjectMapper mapper = new ObjectMapper();
  private static final String EMPTY_JSON = "{}";

  public static ObjectNode deserialise(String data) {
    if (data == null || data.isEmpty()) {
      data = EMPTY_JSON;
    }
    try {
      return (ObjectNode) mapper.readTree(data);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static String serialise(ObjectNode objectNode) {
    if (objectNode == null) {
      return EMPTY_JSON;
    }
    try {
      return mapper.writeValueAsString(objectNode);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
