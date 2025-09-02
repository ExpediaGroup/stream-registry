/**
 * Copyright (C) 2018-2025 Expedia, Inc.
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
package com.expediagroup.streamplatform.streamregistry.graphql.resolvers;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.services.DomainService;
import com.expediagroup.streamplatform.streamregistry.model.Domain;
import com.expediagroup.streamplatform.streamregistry.model.Schema;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SchemaResolver implements Resolvers.SchemaResolver {
  private final DomainService domainService;

  public Domain domain(Schema schema) {
    return domainService.get(schema.getKey().getDomainKey()).orElse(null);
  }
}
