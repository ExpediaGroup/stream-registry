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
package com.expediagroup.streamplatform.streamregistry.graphql.mutation.impl;

import static com.expediagroup.streamplatform.streamregistry.graphql.StateHelper.maintainState;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.services.DomainService;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.DomainKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.SpecificationInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.StatusInput;
import com.expediagroup.streamplatform.streamregistry.graphql.mutation.DomainMutation;
import com.expediagroup.streamplatform.streamregistry.model.Domain;

@Component
@RequiredArgsConstructor
public class DomainMutationImpl implements DomainMutation {
  private final DomainService domainService;

  @Override
  public Domain insert(DomainKeyInput key, SpecificationInput specification) {
    return domainService.create(asDomain(key, specification)).get();
  }

  @Override
  public Domain update(DomainKeyInput key, SpecificationInput specification) {
    return domainService.update(asDomain(key, specification)).get();
  }
  @Override
  public Domain upsert(DomainKeyInput key, SpecificationInput specification) {
    return domainService.upsert(asDomain(key, specification)).get();
  }

  @Override
  public Boolean delete(DomainKeyInput key) {
    throw new UnsupportedOperationException("delete");
  }

  @Override
  public Domain updateStatus(DomainKeyInput key, StatusInput status) {
    Domain domain = domainService.read(key.asDomainKey()).get();
    domain.setStatus(status.asStatus());
    return domainService.update(domain).get();
  }

  private Domain asDomain(DomainKeyInput key, SpecificationInput specification) {
    Domain domain = new Domain();
    domain.setKey(key.asDomainKey());
    domain.setSpecification(specification.asSpecification());
    maintainState(domain, domainService.read(domain.getKey()));
    return domain;
  }
}
