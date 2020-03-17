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

import static com.expediagroup.streamplatform.streamregistry.graphql.filters.FilterUtility.matches;
import static com.expediagroup.streamplatform.streamregistry.graphql.filters.FilterUtility.matchesInt;
import static com.expediagroup.streamplatform.streamregistry.graphql.filters.FilterUtility.matchesSchemaKey;
import static com.expediagroup.streamplatform.streamregistry.graphql.filters.FilterUtility.matchesSpecification;

import java.util.function.Predicate;

import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.SchemaKeyQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.SpecificationQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.StreamKeyQuery;
import com.expediagroup.streamplatform.streamregistry.model.Stream;
import com.expediagroup.streamplatform.streamregistry.model.keys.StreamKey;

public class StreamFilter implements Predicate<Stream> {

  private final StreamKeyQuery keyQuery;
  private final SpecificationQuery specQuery;
  private final SchemaKeyQuery schemaKeyQuery;

  public StreamFilter(StreamKeyQuery keyQuery, SpecificationQuery specQuery, SchemaKeyQuery schemaKeyQuery) {
    this.keyQuery = keyQuery;
    this.specQuery = specQuery;
    this.schemaKeyQuery = schemaKeyQuery;
  }

  @Override
  public boolean test(Stream stream) {
    return matchesStreamKey(stream.getKey(), keyQuery)
        && matchesSchemaKey(stream.getSchemaKey(), schemaKeyQuery)
        && matchesSpecification(stream.getSpecification(), specQuery);
  }

  public static boolean matchesStreamKey(StreamKey key, StreamKeyQuery streamKeyQuery) {
    if (streamKeyQuery == null) {
      return true;
    }
    return matches(key.getDomain(), streamKeyQuery.getDomainRegex())
        && matches(key.getName(), streamKeyQuery.getNameRegex())
        && matchesInt(key.getVersion(), streamKeyQuery.getVersion());
  }
}
