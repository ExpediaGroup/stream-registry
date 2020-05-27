/**
 * Copyright (C) 2018-2020 Expedia, Inc.
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

import static com.expediagroup.streamplatform.streamregistry.graphql.filters.FilterUtility.matchesSchemaKey;
import static com.expediagroup.streamplatform.streamregistry.graphql.filters.FilterUtility.matchesSpecification;

import java.util.function.Predicate;

import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.SchemaKeyQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.SpecificationQuery;
import com.expediagroup.streamplatform.streamregistry.model.Schema;

public class SchemaFilter implements Predicate<Schema> {

  private final SchemaKeyQuery keyQuery;
  private final SpecificationQuery specQuery;

  public SchemaFilter(SchemaKeyQuery schemaKeyQuery, SpecificationQuery specificationQuery) {
    this.keyQuery = schemaKeyQuery;
    this.specQuery = specificationQuery;
  }

  @Override
  public boolean test(Schema schema) {
    return matchesSchemaKey(schema.getKey(), keyQuery)
        && matchesSpecification(schema.getSpecification(), specQuery);
  }
}
