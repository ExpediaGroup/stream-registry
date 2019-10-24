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

import com.expediagroup.streamplatform.streamregistry.core.services.StreamBindingService;
import com.expediagroup.streamplatform.streamregistry.graphql.filters.StreamBindingFilter;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.StreamBindingKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.SpecificationQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.StreamBindingKeyQuery;
import com.expediagroup.streamplatform.streamregistry.model.StreamBinding;

public class StreamBindingQuery {

  private final StreamBindingService streamBindingService;

  public StreamBindingQuery(StreamBindingService streamBindingService) {
    this.streamBindingService = streamBindingService;
  }

  public StreamBinding getByKey(StreamBindingKeyInput key) {
    return streamBindingService.read(key.asStreamBindingKey()).get();
  }

  public Iterable<StreamBinding> getByQuery(StreamBindingKeyQuery key, SpecificationQuery specification) {
    return streamBindingService.findAll(new StreamBindingFilter(key, specification));
  }
}
