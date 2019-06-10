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
package com.expediagroup.streamplatform.streamregistry.graphql.model;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.expediagroup.streamplatform.streamregistry.model.NameDomain;

public class GraphQLNameDomainTest {
  private final NameDomain nameDomain = NameDomain
      .builder()
      .name("name")
      .domain("domain")
      .build();

  private final GraphQLNameDomain graphQLNameDomain = GraphQLNameDomain
      .builder()
      .name("name")
      .domain("domain")
      .build();

  @Test
  public void fromDto() {
    assertThat(GraphQLNameDomain.fromDto(nameDomain), is(graphQLNameDomain));
  }

  @Test
  public void toDto() {
    assertThat(GraphQLNameDomain.toDto(graphQLNameDomain), is(nameDomain));
  }
}
