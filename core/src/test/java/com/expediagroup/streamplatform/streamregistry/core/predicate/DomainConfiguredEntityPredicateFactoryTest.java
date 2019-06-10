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
package com.expediagroup.streamplatform.streamregistry.core.predicate;

import static org.hamcrest.CoreMatchers.is;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.expediagroup.streamplatform.streamregistry.model.Configuration;
import com.expediagroup.streamplatform.streamregistry.model.NameDomain;
import com.expediagroup.streamplatform.streamregistry.model.Stream;

public class DomainConfiguredEntityPredicateFactoryTest {
  private final DomainConfiguredEntityPredicateFactory underTest = new DomainConfiguredEntityPredicateFactory();

  @Test
  public void domainConfiguredEntity() {
    Stream query = Stream
        .builder()
        .name("^name$")
        .owner("^owner$")
        .description("^description$")
        .tags(Map.of("^key$", "^value$"))
        .configuration(Configuration
            .builder()
            .type("^type$")
            .properties(Map.of("^key$", "^value$"))
            .build())
        .domain("^domain$")
        .version(1)
        .schema(NameDomain
            .builder()
            .name("^name$")
            .domain("^domain$")
            .build())
        .build();

    Stream entity = Stream
        .builder()
        .name("name")
        .owner("owner")
        .description("description")
        .tags(Map.of("key", "value"))
        .configuration(Configuration
            .builder()
            .type("type")
            .properties(Map.of("key", "value"))
            .build())
        .domain("domain")
        .version(1)
        .schema(NameDomain
            .builder()
            .name("name")
            .domain("domain")
            .build())
        .build();

    boolean result = underTest
        .create(query)
        .test(entity);

    Assert.assertThat(result, is(true));
  }
}
