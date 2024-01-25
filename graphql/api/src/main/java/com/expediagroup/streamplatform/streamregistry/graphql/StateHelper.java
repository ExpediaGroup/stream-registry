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
package com.expediagroup.streamplatform.streamregistry.graphql;

import java.util.Collections;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.SchemaKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.SpecificationInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.StatusInput;
import com.expediagroup.streamplatform.streamregistry.model.Specification;
import com.expediagroup.streamplatform.streamregistry.model.Stated;
import com.expediagroup.streamplatform.streamregistry.model.Status;
import com.expediagroup.streamplatform.streamregistry.model.keys.SchemaKey;

public class StateHelper {

  private static final ObjectMapper mapper = new ObjectMapper();

  public static void maintainState(Stated stated, Optional<? extends Stated> existing) {
    existing.ifPresent(value -> stated.setStatus(value.getStatus()));
  }

  public static Specification specification() {
    return SpecificationInput.builder()
      .description("description")
      .tags(Collections.emptyList())
      .type("egsp.kafka")
      .configuration(mapper.createObjectNode())
      .security(Collections.emptyList())
      .build().asSpecification();
  }

  public static Status status() {
    return StatusInput.builder()
      .agentStatus(mapper.createObjectNode())
      .build().asStatus();
  }

  public static SchemaKey schemaKey() {
    return SchemaKeyInput.builder()
      .domain("domain")
      .name("name")
      .build().asSchemaKey();
  }
}
