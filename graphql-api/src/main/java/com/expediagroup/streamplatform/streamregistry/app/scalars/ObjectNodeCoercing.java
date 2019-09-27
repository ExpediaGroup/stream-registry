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
package com.expediagroup.streamplatform.streamregistry.app.scalars;

import java.io.IOException;
import java.util.Collections;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import graphql.language.ObjectValue;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class ObjectNodeCoercing extends BaseCoercing {

  private static final ObjectMapper mapper = new ObjectMapper();

  @Override
  public Object parseValue(Object input) throws CoercingParseValueException {
    try {
      JsonNode jsonNode = mapper.readTree(input.toString());
      if (!jsonNode.isObject()) {
        throw new CoercingParseValueException("Expected 'ObjectNode', got: " + jsonNode);
      }
      return jsonNode;
    } catch (IOException e) {
      log.error("Error parsing value: {}", input, e);
      throw new CoercingParseValueException(e);
    }
  }

  @Override
  public Object parseLiteral(Object input) throws CoercingParseLiteralException {
    if (!(input instanceof ObjectValue)) {
      log.error("Expected 'ObjectValue', got: {}", input);
      throw new CoercingParseLiteralException("Expected 'ObjectValue', got: " + input);
    }
    return JsonCoercingUtil.parseLiteral(input, Collections.EMPTY_MAP);
  }
}
