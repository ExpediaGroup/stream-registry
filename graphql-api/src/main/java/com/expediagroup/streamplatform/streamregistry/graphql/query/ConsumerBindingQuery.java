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

import com.expediagroup.streamplatform.streamregistry.core.services.ConsumerBindingService;
import com.expediagroup.streamplatform.streamregistry.graphql.filters.ConsumerBindingFilter;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.ConsumerBindingKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.ConsumerBindingKeyQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.SpecificationQuery;
import com.expediagroup.streamplatform.streamregistry.model.ConsumerBinding;

public class ConsumerBindingQuery {

  private final ConsumerBindingService service;

  public ConsumerBindingQuery(ConsumerBindingService service) {
    this.service = service;
  }

  public ConsumerBinding getConsumerBinding(ConsumerBindingKeyInput key) {
    return service.read(key.asConsumerBindingKey()).get();
  }

  public Iterable<ConsumerBinding> getConsumerBindings(ConsumerBindingKeyQuery key, SpecificationQuery specification) {
    return service.findAll(new ConsumerBindingFilter(key, specification));
  }
}
