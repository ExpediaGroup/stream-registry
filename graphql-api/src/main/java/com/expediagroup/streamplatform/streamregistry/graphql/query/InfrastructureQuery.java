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

import com.expediagroup.streamplatform.streamregistry.core.services.InfrastructureService;
import com.expediagroup.streamplatform.streamregistry.graphql.filters.InfrastructureFilter;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.InfrastructureKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.InfrastructureKeyQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.SpecificationQuery;
import com.expediagroup.streamplatform.streamregistry.model.Infrastructure;

public class InfrastructureQuery {

  private final InfrastructureService service;

  public InfrastructureQuery(InfrastructureService service) {
    this.service = service;
  }

  public Infrastructure getInfrastructure(InfrastructureKeyInput key) {
    return service.read(key.asInfrastructureKey()).get();
  }

  public Iterable<Infrastructure> getInfrastructures(InfrastructureKeyQuery key, SpecificationQuery specification) {
    return service.findAll(new InfrastructureFilter(key, specification));
  }
}
