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

import static java.util.Collections.emptyMap;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import org.junit.Test;

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

public class JsonCoercingUtilTest {

  @Test(expected = CoercingParseLiteralException.class)
  public void notAValue() {
    JsonCoercingUtil.parseLiteral(null, emptyMap());
  }

  @Test
  public void stringValue() {
    Value value = StringValue.newStringValue("a").build();
    Object result = JsonCoercingUtil.parseLiteral(value, emptyMap());
    assertThat(result, is("a"));
  }

  @Test
  public void intValue() {
    Value value = IntValue.newIntValue(new BigInteger("1")).build();
    Object result = JsonCoercingUtil.parseLiteral(value, emptyMap());
    assertThat(result, is(new BigInteger("1")));
  }

  @Test
  public void floatValue() {
    Value value = FloatValue.newFloatValue(new BigDecimal("1.2")).build();
    Object result = JsonCoercingUtil.parseLiteral(value, emptyMap());
    assertThat(result, is(new BigDecimal("1.2")));
  }

  @Test
  public void booleanValue() {
    Value value = BooleanValue.newBooleanValue(true).build();
    Object result = JsonCoercingUtil.parseLiteral(value, emptyMap());
    assertThat(result, is(true));
  }

  @Test
  public void enumValue() {
    Value value = EnumValue.newEnumValue("a").build();
    Object result = JsonCoercingUtil.parseLiteral(value, emptyMap());
    assertThat(result, is("a"));
  }

  @Test
  public void variableReferenceValue() {
    Value value = VariableReference.newVariableReference().name("a").build();
    Object result = JsonCoercingUtil.parseLiteral(value, Map.of("a", "b"));
    assertThat(result, is("b"));
  }

  @Test
  public void arrayValue() {
    Value value = ArrayValue.newArrayValue()
        .value(StringValue.newStringValue("a").build())
        .build();
    Object result = JsonCoercingUtil.parseLiteral(value, emptyMap());
    assertThat(result, is(List.of("a")));
  }

  @Test
  public void objectValue() {
    Value value = ObjectValue.newObjectValue()
        .objectField(ObjectField.newObjectField()
            .name("a")
            .value(StringValue.newStringValue("b").build())
            .build())
        .build();
    Object result = JsonCoercingUtil.parseLiteral(value, emptyMap());
    assertThat(result, is(Map.of("a", "b")));
  }
}