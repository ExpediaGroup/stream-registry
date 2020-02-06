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
package com.expediagroup.streamplatform.streamregistry.graphql.filters;

import static com.expediagroup.streamplatform.streamregistry.graphql.filters.FilterUtility.matches;
import static com.expediagroup.streamplatform.streamregistry.graphql.filters.SpecificationMatchUtility.matchesSpecification;

import java.util.function.Predicate;

import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.SchemaKeyQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.SpecificationQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.StreamKeyQuery;
import com.expediagroup.streamplatform.streamregistry.model.Stream;

public class StreamFilter implements Predicate<Stream> {

  private final StreamKeyQuery keyQuery;
  private final SpecificationQuery specQuery;
  private final SchemaKeyQuery schemaQuery;

  public StreamFilter(StreamKeyQuery keyQuery, SpecificationQuery specQuery, SchemaKeyQuery schemaQuery) {
    this.keyQuery = keyQuery;
    this.specQuery = specQuery;
    this.schemaQuery = schemaQuery;
  }

  @Override
  public boolean test(Stream stream) {

    if (keyQuery != null) {
      if (!matches(stream.getKey().getName(), keyQuery.getNameRegex())) {
        return false;
      }
      if (!matches(stream.getKey().getDomain(), keyQuery.getDomainRegex())) {
        return false;
      }
      if (keyQuery.getVersion() != null && stream.getKey().getVersion() != keyQuery.getVersion()) {
        return false;
      }
    }

    return matchesSpecification(stream.getSpecification(), specQuery);
  }
}
