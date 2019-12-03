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

import java.util.Optional;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.services.InfrastructureService;
import com.expediagroup.streamplatform.streamregistry.graphql.filters.InfrastructureFilter;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.InfrastructureKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.InfrastructureKeyQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.SpecificationQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.query.InfrastructureQuery;
import com.expediagroup.streamplatform.streamregistry.model.Infrastructure;

@Component
@RequiredArgsConstructor
public class InfrastructureQueryImpl implements InfrastructureQuery {
  private final InfrastructureService infrastructureService;

  @Override
  public Optional<Infrastructure> byKey(InfrastructureKeyInput key) {
    return infrastructureService.read(key.asInfrastructureKey());
  }

  @Override
  public Iterable<Infrastructure> byQuery(InfrastructureKeyQuery key, SpecificationQuery specification) {
    return infrastructureService.findAll(new InfrastructureFilter(key, specification));
  }
}
