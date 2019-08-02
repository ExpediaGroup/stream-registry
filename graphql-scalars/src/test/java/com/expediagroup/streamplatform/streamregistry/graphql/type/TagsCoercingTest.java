package com.expediagroup.streamplatform.streamregistry.graphql.type;

import graphql.language.BooleanValue;
import graphql.language.ObjectField;
import graphql.language.ObjectValue;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

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
