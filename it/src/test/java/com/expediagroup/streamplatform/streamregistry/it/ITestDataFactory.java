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
package com.expediagroup.streamplatform.streamregistry.it;

import java.util.Collections;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.expediagroup.streamplatform.streamregistry.graphql.client.InsertConsumerMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpsertConsumerMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpsertDomainMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpsertZoneMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.ConsumerKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.DomainKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.SpecificationInput;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.ZoneKeyInput;

public class ITestDataFactory {

  private static final ObjectMapper mapper = new ObjectMapper();

  private static final String DOMAIN_NAME = "domainName";
  private static final String DESCRIPTION = "description";
  private static final String DEFAULT = "default";
  private static final String A = "a";
  private static final String B = "b";

  private DomainKeyInput.Builder domainKeyInputBuilder;
  private SpecificationInput.Builder specificationInputBuilder;

  private ConsumerKeyInput.Builder consumerKeyInputBuilder;

  public final DomainKeyInput.Builder domainKeyInputBuilder() {
    if (domainKeyInputBuilder == null) {
      domainKeyInputBuilder = DomainKeyInput.builder().name(DOMAIN_NAME);
    }
    return domainKeyInputBuilder;
  }

  public final ConsumerKeyInput.Builder consumerKeyInputBuilder() {
    if (consumerKeyInputBuilder == null) {
      consumerKeyInputBuilder = ConsumerKeyInput.builder()
          .name("consumerName")
          .streamDomain(domainKeyInputBuilder().build().name())
          .streamName("stream_name")
          .streamVersion(1)
          .zone("zone");
    }
    return consumerKeyInputBuilder;
  }

  public SpecificationInput.Builder specificationInputBuilder() {
    if (specificationInputBuilder == null) {
      specificationInputBuilder = SpecificationInput.builder()
          .configuration(mapper.createObjectNode().put(A, B))
          .description(DESCRIPTION)
          .tags(Collections.emptyList())
          .type(DEFAULT);
    }
    return specificationInputBuilder;
  }

  public UpsertDomainMutation.Builder upsertDomainMutationBuilder(
  ) {
    return UpsertDomainMutation.builder()
        .key(domainKeyInputBuilder().build())
        .specification(specificationInputBuilder().build());
  }

  public UpsertConsumerMutation.Builder upsertConsumerMutationBuilder() {
    return UpsertConsumerMutation.builder()
        .key(consumerKeyInputBuilder().build())
        .specification(specificationInputBuilder().build());
  }

  public InsertConsumerMutation.Builder insertConsumerMutationBuilder() {
    return InsertConsumerMutation.builder()
        .key(consumerKeyInputBuilder().build())
        .specification(specificationInputBuilder().build());
  }

  public UpsertZoneMutation.Builder upsertZoneMutationBuilder() {
    return UpsertZoneMutation.builder()
        .key(ZoneKeyInputBuilder().build())
        .specification(specificationInputBuilder().build())
        ;
  }

  private ZoneKeyInput.Builder ZoneKeyInputBuilder() {
    return ZoneKeyInput.builder()
        .name("zoneName")
        ;
  }
}
