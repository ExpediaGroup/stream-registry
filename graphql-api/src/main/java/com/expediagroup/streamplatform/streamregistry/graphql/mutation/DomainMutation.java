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
package com.expediagroup.streamplatform.streamregistry.graphql.mutation;

import static com.expediagroup.streamplatform.streamregistry.graphql.StateHelper.maintainState;

import com.expediagroup.streamplatform.streamregistry.core.services.Services;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.DomainKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.SpecificationInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.StatusInput;
import com.expediagroup.streamplatform.streamregistry.model.Domain;

public class DomainMutation {

  private Services services;

  public DomainMutation(Services services) {
    this.services = services;
  }

  public Domain insert(DomainKeyInput key, SpecificationInput specification) {
    return services.getDomainService().create(asDomain(key, specification)).get();
  }

  public Domain update(DomainKeyInput key, SpecificationInput specification) {
    return services.getDomainService().update(asDomain(key, specification)).get();
  }

  public Domain upsert(DomainKeyInput key, SpecificationInput specification) {
    return services.getDomainService().upsert(asDomain(key, specification)).get();
  }

  public Boolean delete(DomainKeyInput key) {
    throw new UnsupportedOperationException("delete");
  }

  public Domain updateStatus(DomainKeyInput key, StatusInput status) {
    Domain domain = services.getDomainService().read(key.asDomainKey()).get();
    domain.setStatus(status.asStatus());
    return services.getDomainService().update(domain).get();
  }

  private Domain asDomain(DomainKeyInput key, SpecificationInput specification) {
    Domain domain = new Domain();
    domain.setKey(key.asDomainKey());
    domain.setSpecification(specification.asSpecification());
    maintainState(domain, services.getDomainService().read(domain.getKey()));
    return domain;
  }
}
