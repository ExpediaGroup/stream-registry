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

import com.apollographql.apollo.response.CustomTypeValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;

import java.io.UncheckedIOException;
import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ConfigTypeAdapterTest {
  private static final ObjectMapper mapper = new ObjectMapper();
  private final ConfigTypeAdapter underTest = new ConfigTypeAdapter();

  @Test
  public void encodeObjectNode() {
    CustomTypeValue result = underTest.encode(mapper.createObjectNode().put("a", "b"));
    assertThat(result.value, is("{\"a\":\"b\"}"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void encodeNotAnObjectNode() {
    underTest.encode(null);
  }

  @Test
  public void decodeObjectNode() {
    Object result = underTest.decode(CustomTypeValue.fromRawValue("{\"a\":\"b\"}"));
    assertThat(result, is(instanceOf(ObjectNode.class)));
    assertThat(result, is(mapper.createObjectNode().put("a", "b")));
  }

  @Test(expected = IllegalStateException.class)
  public void decodeNotAnObjectNode() {
    underTest.decode(CustomTypeValue.fromRawValue("[{\"a\":\"b\"}]"));
  }

  @Test(expected = UncheckedIOException.class)
  public void decodeFailure() {
    underTest.decode(CustomTypeValue.fromRawValue("[]"));
  }
}
