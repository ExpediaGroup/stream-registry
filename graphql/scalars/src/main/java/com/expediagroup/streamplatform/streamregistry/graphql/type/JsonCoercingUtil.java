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
package com.expediagroup.streamplatform.streamregistry.graphql.type;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import graphql.language.ArrayValue;
import graphql.language.BooleanValue;
import graphql.language.EnumValue;
import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.language.ObjectField;
import graphql.language.ObjectValue;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.language.VariableReference;
import graphql.schema.CoercingParseLiteralException;

@Slf4j
class JsonCoercingUtil {

  public static Object parseLiteral(Object input, Map<String, Object> variables) throws CoercingParseLiteralException {
    if (!(input instanceof Value)) {
      log.error("Expected 'Value', got: {}", input);
      throw new CoercingParseLiteralException("Expected 'Value', got: " + input);
    }
    Object result = null;
    if (input instanceof StringValue) {
      result = ((StringValue) input).getValue();
    } else if (input instanceof IntValue) {
      result = ((IntValue) input).getValue();
    } else if (input instanceof FloatValue) {
      result = ((FloatValue) input).getValue();
    } else if (input instanceof BooleanValue) {
      result = ((BooleanValue) input).isValue();
    } else if (input instanceof EnumValue) {
      result = ((EnumValue) input).getName();
    } else if (input instanceof VariableReference) {
      result = variables.get(((VariableReference) input).getName());
    } else if (input instanceof ArrayValue) {
      result = ((ArrayValue) input).getValues().stream()
          .map(v -> parseLiteral(v, variables))
          .collect(toList());
    } else if (input instanceof ObjectValue) {
      result = ((ObjectValue) input).getObjectFields().stream()
          .collect(toMap(ObjectField::getName, f -> parseLiteral(f.getValue(), variables)));
    }
    return result;
  }
}
