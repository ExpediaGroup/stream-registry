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
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.Test;
import org.mockito.Mockito;

import graphql.language.BooleanValue;
import graphql.language.ObjectField;
import graphql.language.ObjectValue;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;

public class TagsCoercingTest {
  private final TagsCoercing underTest = new TagsCoercing();

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

    TagsCoercing spy = Mockito.spy(underTest);
    Object parsed = new Object();
    when(spy.parseLiteral(input, emptyMap())).thenReturn(parsed);
    Object result = spy.parseLiteral(input);
    assertThat(result, is(sameInstance(parsed)));
  }

  @Test
  public void parseLiteralObjectValue() {
    Value input = ObjectValue.newObjectValue()
        .objectField(ObjectField.newObjectField()
            .name("a")
            .value(StringValue.newStringValue("b").build())
            .build())
        .build();
    Object result = underTest.parseLiteral(input, emptyMap());
    assertThat(result, is(Map.of("a", "b")));
  }

  @Test(expected = CoercingParseLiteralException.class)
  public void parseLiteralNotAnObjectValue() {
    Value input = StringValue.newStringValue("b").build();
    underTest.parseLiteral(input, emptyMap());
  }

  @Test(expected = CoercingParseLiteralException.class)
  public void parseLiteralNotAStringValue() {
    Value input = ObjectValue.newObjectValue()
        .objectField(ObjectField.newObjectField()
            .name("a")
            .value(BooleanValue.newBooleanValue(true).build())
            .build())
        .build();
    underTest.parseLiteral(input, emptyMap());
  }

  @Test
  public void parseValueMap() {
    Object result = underTest.parseValue("{\"a\":\"b\"}");
    assertThat(result, is(Map.of("a", "b")));
  }

  @Test(expected = CoercingParseValueException.class)
  public void parseValueNotAMap() {
    underTest.parseValue("[]");
  }

}
