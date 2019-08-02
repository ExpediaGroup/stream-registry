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
package com.expediagroup.streamplatform.streamregistry.graphql.type;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import graphql.language.ObjectField;
import graphql.language.ObjectValue;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;

@Slf4j
class TagsCoercing extends BaseCoercing {
  private static final ObjectMapper mapper = new ObjectMapper();
  private static final TypeReference<Map<String, String>> mapStringString = new TypeReference<>() {};

  @Override
  public Object parseValue(Object input) throws CoercingParseValueException {
    try {
      return mapper.readValue(input.toString(), mapStringString);
    } catch (IOException e) {
      log.error("Error parsing value: {}", input, e);
      throw new CoercingParseValueException(e);
    }
  }

  @Override
  public Object parseLiteral(Object input, Map<String, Object> variables) throws CoercingParseLiteralException {
    if (!(input instanceof ObjectValue)) {
      log.error("Expected 'ObjectValue', got: {}", input);
      throw new CoercingParseLiteralException("Expected 'ObjectValue', got: " + input);
    }

    Map<String, String> result = new HashMap<>();
    for (ObjectField field : ((ObjectValue) input).getObjectFields()) {
      Value value = field.getValue();
      if (!(value instanceof StringValue)) {
        log.error("Expected 'StringValue', got: {}", value);
        throw new CoercingParseLiteralException("Expected 'StringValue', got: " + value);
      }
      result.put(field.getName(), ((StringValue) value).getValue());
    }
    return result;
  }
}
