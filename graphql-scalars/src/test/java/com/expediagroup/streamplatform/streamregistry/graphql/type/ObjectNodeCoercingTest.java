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

import static java.util.Collections.emptyMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Test;
import org.mockito.Mockito;

import graphql.language.ArrayValue;
import graphql.language.ObjectField;
import graphql.language.ObjectValue;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;

public class ObjectNodeCoercingTest {
  private static final ObjectMapper mapper = new ObjectMapper();
  private final ObjectNodeCoercing underTest = new ObjectNodeCoercing();

  @Test
  public void serialize() {
    Object input = new Object();
    Object result = underTest.serialize(input);
    assertThat(result, is(sameInstance(input)));
  }

  @Test
  public void parseLiteral() {
    Value input = ObjectValue.newObjectValue()
        .objectField(ObjectField.newObjectField()
            .name("a")
            .value(StringValue.newStringValue("b").build())
            .build())
        .build();

    ObjectNodeCoercing spy = Mockito.spy(underTest);
    Object parsed = new Object();
    when(spy.parseLiteral(input, emptyMap())).thenReturn(parsed);
    Object result = spy.parseLiteral(input);
    assertThat(result, is(sameInstance(parsed)));
  }

  @Test(expected = CoercingParseLiteralException.class)
  public void parseLiteralNotAnObjectNode() {
    underTest.parseLiteral(ArrayValue.newArrayValue().build(), emptyMap());
  }

  @Test
  public void parseValueObjectNode() {
    Object result = underTest.parseValue("{\"a\":\"b\"}");
    assertThat(result, is(mapper.createObjectNode().put("a", "b")));
  }

  @Test(expected = CoercingParseValueException.class)
  public void parseValueNotAnObjectNode() {
    underTest.parseValue("[]");
  }

  @Test(expected = CoercingParseValueException.class)
  public void parseValueNotValidJson() {
    underTest.parseValue("{");
  }
}
