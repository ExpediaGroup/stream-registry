package com.expediagroup.streamplatform.streamregistry.graphql.type;

import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.language.ArrayValue;
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
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public class ConfigCoercingTest {
  private static final ObjectMapper mapper = new ObjectMapper();
  private final ConfigCoercing underTest = new ConfigCoercing();

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

    ConfigCoercing spy = Mockito.spy(underTest);
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
