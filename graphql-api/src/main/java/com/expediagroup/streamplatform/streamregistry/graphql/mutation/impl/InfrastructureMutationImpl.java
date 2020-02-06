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
package com.expediagroup.streamplatform.streamregistry.graphql.mutation.impl;

import static com.expediagroup.streamplatform.streamregistry.graphql.StateHelper.maintainState;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.services.InfrastructureService;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.InfrastructureKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.SpecificationInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.StatusInput;
import com.expediagroup.streamplatform.streamregistry.graphql.mutation.InfrastructureMutation;
import com.expediagroup.streamplatform.streamregistry.model.Infrastructure;

@Component
@RequiredArgsConstructor
public class InfrastructureMutationImpl implements InfrastructureMutation {
  private final InfrastructureService infrastructureService;

  @Override
  public Infrastructure insert(InfrastructureKeyInput key, SpecificationInput specification) {
    return infrastructureService.create(asInfrastructure(key, specification)).get();
  }

  @Override
  public Infrastructure update(InfrastructureKeyInput key, SpecificationInput specification) {
    return infrastructureService.update(asInfrastructure(key, specification)).get();
  }

  @Override
  public Infrastructure upsert(InfrastructureKeyInput key, SpecificationInput specification) {
    return infrastructureService.upsert(asInfrastructure(key, specification)).get();
  }

  @Override
  public Boolean delete(InfrastructureKeyInput key) {
    throw new UnsupportedOperationException("delete");
  }

  @Override
  public Infrastructure updateStatus(InfrastructureKeyInput key, StatusInput status) {
    Infrastructure infrastructure = infrastructureService.read(key.asInfrastructureKey()).get();
    infrastructure.setStatus(status.asStatus());
    return infrastructureService.update(infrastructure).get();
  }

  private Infrastructure asInfrastructure(InfrastructureKeyInput key, SpecificationInput specification) {
    Infrastructure out = new Infrastructure();
    out.setKey(key.asInfrastructureKey());
    out.setSpecification(specification.asSpecification());
    maintainState(out, infrastructureService.read(out.getKey()));
    return out;
  }
}
