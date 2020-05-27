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
package com.expediagroup.streamplatform.streamregistry.graphql.query.impl;

import java.util.Optional;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.services.ProducerBindingService;
import com.expediagroup.streamplatform.streamregistry.graphql.filters.ProducerBindingFilter;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.ProducerBindingKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.ProducerBindingKeyQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.SpecificationQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.query.ProducerBindingQuery;
import com.expediagroup.streamplatform.streamregistry.model.ProducerBinding;

@Component
@RequiredArgsConstructor
public class ProducerBindingQueryImpl implements ProducerBindingQuery {
  private final ProducerBindingService producerBindingService;

  @Override
  public Optional<ProducerBinding> byKey(ProducerBindingKeyInput key) {
    return producerBindingService.read(key.asProducerBindingKey());
  }

  @Override
  public Iterable<ProducerBinding> byQuery(ProducerBindingKeyQuery key, SpecificationQuery specification) {
    return producerBindingService.findAll(new ProducerBindingFilter(key, specification));
  }
}
