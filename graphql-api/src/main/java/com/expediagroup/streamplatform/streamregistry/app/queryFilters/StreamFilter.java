/**
 * Copyright (C) 2016-2019 Expedia Inc.
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

package com.expediagroup.streamplatform.streamregistry.app.queryFilters;

import static com.expediagroup.streamplatform.streamregistry.app.queryFilters.SpecificationMatchUtility.matchesSpecification;

import com.expediagroup.streamplatform.streamregistry.app.queries.SchemaKeyQuery;
import com.expediagroup.streamplatform.streamregistry.app.queries.SpecificationQuery;
import com.expediagroup.streamplatform.streamregistry.app.queries.StreamKeyQuery;
import com.expediagroup.streamplatform.streamregistry.core.services.Filter;
import com.expediagroup.streamplatform.streamregistry.model.Stream;

public class StreamFilter implements Filter<Stream> {

  private final StreamKeyQuery keyQuery;
  private final SpecificationQuery specQuery;
  private final SchemaKeyQuery schemaQuery;

  public StreamFilter(StreamKeyQuery keyQuery, SpecificationQuery specQuery, SchemaKeyQuery schemaQuery) {
    this.keyQuery = keyQuery;
    this.specQuery = specQuery;
    this.schemaQuery = schemaQuery;
  }

  public boolean matches(Stream stream) {

    if (keyQuery != null) {
      if (!stream.getKey().getName().matches(keyQuery.getNameRegex())) {
        return false;
      }
      if (!stream.getKey().getDomain().matches(keyQuery.getDomainRegex())) {
        return false;
      }
      if (keyQuery.getVersion() != null && stream.getKey().getVersion() != keyQuery.getVersion()) {
        return false;
      }
    }

    return matchesSpecification(stream.getSpecification(), specQuery);
  }
}

