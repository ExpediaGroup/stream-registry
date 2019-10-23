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
package com.expediagroup.streamplatform.streamregistry.graphql.query;

import com.expediagroup.streamplatform.streamregistry.core.services.StreamService;
import com.expediagroup.streamplatform.streamregistry.graphql.filters.StreamFilter;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.StreamKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.SchemaKeyQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.SpecificationQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.StreamKeyQuery;
import com.expediagroup.streamplatform.streamregistry.model.Stream;

public class StreamQuery {

  private final StreamService service;

  public StreamQuery(StreamService service) {
    this.service = service;
  }

  public Stream getStream(StreamKeyInput key) {
    return service.read(key.asStreamKey()).get();
  }

  public Iterable<Stream> getStreams(StreamKeyQuery key, SpecificationQuery specification, SchemaKeyQuery schemaKeyQuery) {
    return service.findAll(new StreamFilter(key, specification, schemaKeyQuery));
  }

  public Stream getByKey(StreamKeyInput key) {
    return service.read(key.asStreamKey()).get();
  }

  public Iterable<Stream> getByQuery(StreamKeyQuery key, SpecificationQuery specification, SchemaKeyQuery schemaKeyQuery) {
    return service.findAll(new StreamFilter(key, specification, schemaKeyQuery));
  }
}
