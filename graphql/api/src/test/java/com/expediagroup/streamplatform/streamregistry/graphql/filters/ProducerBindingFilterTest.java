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

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Test;

import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.ProducerBindingKeyQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.SecurityQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.SpecificationQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.TagQuery;
import com.expediagroup.streamplatform.streamregistry.model.Principal;
import com.expediagroup.streamplatform.streamregistry.model.ProducerBinding;
import com.expediagroup.streamplatform.streamregistry.model.Role;
import com.expediagroup.streamplatform.streamregistry.model.Specification;
import com.expediagroup.streamplatform.streamregistry.model.Tag;
import com.expediagroup.streamplatform.streamregistry.model.keys.ProducerBindingKey;


public class ProducerBindingFilterTest {

  private final ProducerBinding ProducerBinding = new ProducerBinding(
      new ProducerBindingKey(
          "domain",
          "stream",
          1,
          "zone",
          "infrastructure",
          "producer"
      ),
      new Specification(
          "description",
          Collections.singletonList(new Tag("name", "value")),
          "type",
          new ObjectMapper().createObjectNode(),
        Stream.of(
          new AbstractMap.SimpleEntry<>(new Role("admin"), Arrays.asList(new Principal("user1"))),
          new AbstractMap.SimpleEntry<>(new Role("creator"), Arrays.asList(new Principal("user2"), new Principal("user3")))
        ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
      ),
      null
  );

  @Test
  public void fullMatch() {
    ProducerBindingKeyQuery keyQuery = ProducerBindingKeyQuery.builder()
        .streamDomainRegex("domain")
        .streamNameRegex("stream")
        .streamVersion(1)
        .infrastructureZoneRegex("zone")
        .infrastructureNameRegex("infrastructure")
        .producerNameRegex("producer")
        .build();
    SpecificationQuery specQuery = SpecificationQuery.builder()
        .descriptionRegex("description")
        .tags(Collections.singletonList(TagQuery.builder()
            .nameRegex("name")
            .valueRegex("value")
            .build()))
        .typeRegex("type")
        .securities(Collections.singletonList(SecurityQuery.builder()
          .roleRegex("admin")
          .principalRegex("user1")
          .build()))
        .build();

    assertTrue(new ProducerBindingFilter(keyQuery, specQuery).test(ProducerBinding));
  }

  @Test
  public void domainDoesNotMatch() {
    ProducerBindingKeyQuery keyQuery = ProducerBindingKeyQuery.builder()
        .streamDomainRegex("x")
        .build();
    SpecificationQuery specQuery = SpecificationQuery.builder()
        .build();

    assertFalse(new ProducerBindingFilter(keyQuery, specQuery).test(ProducerBinding));
  }

  @Test
  public void streamDoesNotMatch() {
    ProducerBindingKeyQuery keyQuery = ProducerBindingKeyQuery.builder()
        .streamNameRegex("x")
        .build();
    SpecificationQuery specQuery = SpecificationQuery.builder()
        .build();

    assertFalse(new ProducerBindingFilter(keyQuery, specQuery).test(ProducerBinding));
  }

  @Test
  public void versionDoesNotMatch() {
    ProducerBindingKeyQuery keyQuery = ProducerBindingKeyQuery.builder()
        .streamVersion(2)
        .build();
    SpecificationQuery specQuery = SpecificationQuery.builder()
        .build();

    assertFalse(new ProducerBindingFilter(keyQuery, specQuery).test(ProducerBinding));
  }

  @Test
  public void zoneDoesNotMatch() {
    ProducerBindingKeyQuery keyQuery = ProducerBindingKeyQuery.builder()
        .infrastructureZoneRegex("x")
        .build();
    SpecificationQuery specQuery = SpecificationQuery.builder()
        .build();

    assertFalse(new ProducerBindingFilter(keyQuery, specQuery).test(ProducerBinding));
  }

  @Test
  public void infrastructureDoesNotMatch() {
    ProducerBindingKeyQuery keyQuery = ProducerBindingKeyQuery.builder()
        .infrastructureNameRegex("x")
        .build();
    SpecificationQuery specQuery = SpecificationQuery.builder()
        .build();

    assertFalse(new ProducerBindingFilter(keyQuery, specQuery).test(ProducerBinding));
  }

  @Test
  public void producerDoesNotMatch() {
    ProducerBindingKeyQuery keyQuery = ProducerBindingKeyQuery.builder()
        .producerNameRegex("x")
        .build();
    SpecificationQuery specQuery = SpecificationQuery.builder()
        .build();

    assertFalse(new ProducerBindingFilter(keyQuery, specQuery).test(ProducerBinding));
  }

  @Test
  public void descriptionDoesNotMatch() {
    ProducerBindingKeyQuery keyQuery = ProducerBindingKeyQuery.builder()
        .build();
    SpecificationQuery specQuery = SpecificationQuery.builder()
        .descriptionRegex("x")
        .build();

    assertFalse(new ProducerBindingFilter(keyQuery, specQuery).test(ProducerBinding));
  }

  @Test
  public void tagDoesNotMatch() {
    ProducerBindingKeyQuery keyQuery = ProducerBindingKeyQuery.builder()
        .build();
    SpecificationQuery specQuery = SpecificationQuery.builder()
        .tags(Collections.singletonList(TagQuery.builder()
            .nameRegex("x")
            .valueRegex("x")
            .build()))
        .build();

    assertFalse(new ProducerBindingFilter(keyQuery, specQuery).test(ProducerBinding));
  }

  @Test
  public void typeDoesNotMatch() {
    ProducerBindingKeyQuery keyQuery = ProducerBindingKeyQuery.builder()
        .build();
    SpecificationQuery specQuery = SpecificationQuery.builder()
        .typeRegex("x")
        .build();

    assertFalse(new ProducerBindingFilter(keyQuery, specQuery).test(ProducerBinding));
  }

  @Test
  public void securityDoesNotMatch() {
    ProducerBindingKeyQuery keyQuery = ProducerBindingKeyQuery.builder()
      .build();
    SpecificationQuery specQuery = SpecificationQuery.builder()
      .securities(Collections.singletonList(SecurityQuery.builder()
        .roleRegex("x")
        .principalRegex("x")
        .build()))
      .build();

    assertFalse(new ProducerBindingFilter(keyQuery, specQuery).test(ProducerBinding));
  }
}
