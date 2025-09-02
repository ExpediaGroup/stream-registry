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
package com.expediagroup.streamplatform.streamregistry.graphql.query.impl;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.services.SchemaService;
import com.expediagroup.streamplatform.streamregistry.graphql.filters.SchemaFilter;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.SchemaKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.SchemaKeyQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.SpecificationQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.query.SchemaQuery;
import com.expediagroup.streamplatform.streamregistry.model.Schema;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SchemaQueryImpl implements SchemaQuery {
  private final SchemaService schemaService;

  @Override
  public Optional<Schema> byKey(SchemaKeyInput key) {
    return schemaService.get(key.asSchemaKey());
  }

  @Override
  public Iterable<Schema> byQuery(SchemaKeyQuery key, SpecificationQuery specification) {
    return schemaService.findAll(new SchemaFilter(key, specification));
  }
}
