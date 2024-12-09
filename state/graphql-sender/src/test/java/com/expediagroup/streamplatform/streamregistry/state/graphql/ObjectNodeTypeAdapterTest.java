/**
 * Copyright (C) 2018-2024 Expedia, Inc.
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
package com.expediagroup.streamplatform.streamregistry.state.graphql;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Collections;

import com.apollographql.apollo.api.CustomTypeValue;
import com.apollographql.apollo.api.CustomTypeValue.GraphQLJsonObject;
import com.apollographql.apollo.api.CustomTypeValue.GraphQLString;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Test;

import lombok.val;

public class ObjectNodeTypeAdapterTest {
  private final ObjectMapper mapper = new ObjectMapper();

  private final ObjectNodeTypeAdapter underTest = new ObjectNodeTypeAdapter();

  @Test(expected = IllegalArgumentException.class)
  public void encodeNotObject() {
    underTest.encode(1);
  }

  @Test
  public void encode() {
    val result = (GraphQLString) underTest.encode(mapper.createObjectNode());

    assertThat(result.value, is("{}"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void decodeNotObject() {
    underTest.decode(new CustomTypeValue.GraphQLNumber(1));
  }

  @Test
  public void decode() {
    val result = (JsonNode) underTest.decode(new GraphQLJsonObject(Collections.emptyMap()));

    assertThat(result, is(mapper.createObjectNode()));
  }
}
