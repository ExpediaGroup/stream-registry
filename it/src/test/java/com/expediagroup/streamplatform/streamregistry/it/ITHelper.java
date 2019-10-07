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

import com.expediagroup.streamplatform.streamregistry.graphql.client.UpsertDomainMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.DomainKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.SpecificationInput;

public class ITHelper {

  private static final ObjectMapper mapper = new ObjectMapper();

  public static DomainKeyInput.Builder domainKeyInputBuilder() {
    return DomainKeyInput.builder().name("domainName");
  }

  public static DomainKeyInput domainKeyInput() {
    return domainKeyInputBuilder().build();
  }

  public static SpecificationInput.Builder specificationInputBuilder() {
    return SpecificationInput.builder()
        .configuration(mapper.createObjectNode().put("a", "b"))
        .description("description")
        .tags(Collections.emptyList())
        .type("default");
  }


  public static UpsertDomainMutation.Builder upsertDomainMutationBuilder(
      DomainKeyInput key,
      SpecificationInput spec
  ) {
    return UpsertDomainMutation.builder()
        .key(key)
        .specification(spec);
  }

  public static UpsertDomainMutation upsertDomainMutation(
      DomainKeyInput key,
      SpecificationInput spec
  ) {
    return upsertDomainMutationBuilder(key,spec).build();
  }
}
