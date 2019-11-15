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
package com.expediagroup.streamplatform.streamregistry.graphql.query.impl;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.services.StreamService;
import com.expediagroup.streamplatform.streamregistry.graphql.filters.StreamFilter;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.StreamKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.SchemaKeyQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.SpecificationQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.StreamKeyQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.query.StreamQuery;
import com.expediagroup.streamplatform.streamregistry.model.Stream;

@Component
@RequiredArgsConstructor
public class StreamQueryImpl implements StreamQuery {
  private final StreamService streamService;

  @Override
  public Stream byKey(StreamKeyInput key) {
    return streamService.read(key.asStreamKey()).get();
  }

  @Override
  public Iterable<Stream> byQuery(StreamKeyQuery key, SpecificationQuery specification, SchemaKeyQuery schemaKeyQuery) {
    return streamService.findAll(new StreamFilter(key, specification, schemaKeyQuery));
  }
}
