/**
 * Copyright (C) 2018-2021 Expedia, Inc.
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
package com.expediagroup.streamplatform.streamregistry.graphql.filters;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Test;

import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.ConsumerBindingKeyQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.SecurityQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.SpecificationQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.TagQuery;
import com.expediagroup.streamplatform.streamregistry.model.ConsumerBinding;
import com.expediagroup.streamplatform.streamregistry.model.Principal;
import com.expediagroup.streamplatform.streamregistry.model.Security;
import com.expediagroup.streamplatform.streamregistry.model.Specification;
import com.expediagroup.streamplatform.streamregistry.model.Tag;
import com.expediagroup.streamplatform.streamregistry.model.keys.ConsumerBindingKey;

public class ConsumerBindingFilterTest {

  private final ConsumerBinding consumerBinding = new ConsumerBinding(
      new ConsumerBindingKey(
          "domain",
          "stream",
          1,
          "zone",
          "infrastructure",
          "consumer"
      ),
      new Specification(
          "description",
          Collections.singletonList(new Tag("name", "value")),
          "type",
          new ObjectMapper().createObjectNode(),
          Arrays.asList(
            new Security("admin", Arrays.asList(new Principal("user1"))),
            new Security("creator", Arrays.asList(new Principal("user2"), new Principal("user3")))
          ),
        "function"
      ),
      null
  );

  @Test
  public void fullMatch() {
    ConsumerBindingKeyQuery keyQuery = ConsumerBindingKeyQuery.builder()
        .streamDomainRegex("domain")
        .streamNameRegex("stream")
        .streamVersion(1)
        .infrastructureZoneRegex("zone")
        .infrastructureNameRegex("infrastructure")
        .consumerNameRegex("consumer")
        .build();
    SpecificationQuery specQuery = SpecificationQuery.builder()
        .descriptionRegex("description")
        .tags(Collections.singletonList(TagQuery.builder()
            .nameRegex("name")
            .valueRegex("value")
            .build()))
        .typeRegex("type")
        .security(Collections.singletonList(SecurityQuery.builder()
            .roleRegex("admin")
            .principalRegex("user1")
            .build()))
        .build();

    assertTrue(new ConsumerBindingFilter(keyQuery, specQuery).test(consumerBinding));
  }

  @Test
  public void domainDoesNotMatch() {
    ConsumerBindingKeyQuery keyQuery = ConsumerBindingKeyQuery.builder()
        .streamDomainRegex("x")
        .build();
    SpecificationQuery specQuery = SpecificationQuery.builder()
        .build();

    assertFalse(new ConsumerBindingFilter(keyQuery, specQuery).test(consumerBinding));
  }

  @Test
  public void streamDoesNotMatch() {
    ConsumerBindingKeyQuery keyQuery = ConsumerBindingKeyQuery.builder()
        .streamNameRegex("x")
        .build();
    SpecificationQuery specQuery = SpecificationQuery.builder()
        .build();

    assertFalse(new ConsumerBindingFilter(keyQuery, specQuery).test(consumerBinding));
  }

  @Test
  public void versionDoesNotMatch() {
    ConsumerBindingKeyQuery keyQuery = ConsumerBindingKeyQuery.builder()
        .streamVersion(2)
        .build();
    SpecificationQuery specQuery = SpecificationQuery.builder()
        .build();

    assertFalse(new ConsumerBindingFilter(keyQuery, specQuery).test(consumerBinding));
  }

  @Test
  public void zoneDoesNotMatch() {
    ConsumerBindingKeyQuery keyQuery = ConsumerBindingKeyQuery.builder()
        .infrastructureZoneRegex("x")
        .build();
    SpecificationQuery specQuery = SpecificationQuery.builder()
        .build();

    assertFalse(new ConsumerBindingFilter(keyQuery, specQuery).test(consumerBinding));
  }

  @Test
  public void infrastructureDoesNotMatch() {
    ConsumerBindingKeyQuery keyQuery = ConsumerBindingKeyQuery.builder()
        .infrastructureNameRegex("x")
        .build();
    SpecificationQuery specQuery = SpecificationQuery.builder()
        .build();

    assertFalse(new ConsumerBindingFilter(keyQuery, specQuery).test(consumerBinding));
  }

  @Test
  public void consumerDoesNotMatch() {
    ConsumerBindingKeyQuery keyQuery = ConsumerBindingKeyQuery.builder()
        .consumerNameRegex("x")
        .build();
    SpecificationQuery specQuery = SpecificationQuery.builder()
        .build();

    assertFalse(new ConsumerBindingFilter(keyQuery, specQuery).test(consumerBinding));
  }

  @Test
  public void descriptionDoesNotMatch() {
    ConsumerBindingKeyQuery keyQuery = ConsumerBindingKeyQuery.builder()
        .build();
    SpecificationQuery specQuery = SpecificationQuery.builder()
        .descriptionRegex("x")
        .build();

    assertFalse(new ConsumerBindingFilter(keyQuery, specQuery).test(consumerBinding));
  }

  @Test
  public void tagDoesNotMatch() {
    ConsumerBindingKeyQuery keyQuery = ConsumerBindingKeyQuery.builder()
        .build();
    SpecificationQuery specQuery = SpecificationQuery.builder()
        .tags(Collections.singletonList(TagQuery.builder()
            .nameRegex("x")
            .valueRegex("x")
            .build()))
        .build();

    assertFalse(new ConsumerBindingFilter(keyQuery, specQuery).test(consumerBinding));
  }

  @Test
  public void typeDoesNotMatch() {
    ConsumerBindingKeyQuery keyQuery = ConsumerBindingKeyQuery.builder()
        .build();
    SpecificationQuery specQuery = SpecificationQuery.builder()
        .typeRegex("x")
        .build();

    assertFalse(new ConsumerBindingFilter(keyQuery, specQuery).test(consumerBinding));
  }

  @Test
  public void securityDoesNotMatch() {
    ConsumerBindingKeyQuery keyQuery = ConsumerBindingKeyQuery.builder()
      .build();
    SpecificationQuery specQuery = SpecificationQuery.builder()
      .security(Collections.singletonList(SecurityQuery.builder()
        .roleRegex("x")
        .principalRegex("x")
        .build()))
      .build();

    assertFalse(new ConsumerBindingFilter(keyQuery, specQuery).test(consumerBinding));
  }
}
