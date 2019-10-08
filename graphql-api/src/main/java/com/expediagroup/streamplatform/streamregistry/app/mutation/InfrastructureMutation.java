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
package com.expediagroup.streamplatform.streamregistry.app.mutation;

import static com.expediagroup.streamplatform.streamregistry.app.StateHelper.maintainState;

import com.expediagroup.streamplatform.streamregistry.app.inputs.InfrastructureKeyInput;
import com.expediagroup.streamplatform.streamregistry.app.inputs.SpecificationInput;
import com.expediagroup.streamplatform.streamregistry.app.inputs.StatusInput;
import com.expediagroup.streamplatform.streamregistry.core.services.Services;
import com.expediagroup.streamplatform.streamregistry.model.Infrastructure;

public class InfrastructureMutation {

  private Services services;

  public InfrastructureMutation(Services services) {
    this.services = services;
  }

  public Infrastructure insert(InfrastructureKeyInput key, SpecificationInput specification) {
    return services.getInfrastructureService().create(asInfrastructure(key, specification)).get();
  }

  private Infrastructure asInfrastructure(InfrastructureKeyInput key, SpecificationInput specification) {
    Infrastructure out = new Infrastructure();
    out.setKey(key.asInfrastructureKey());
    out.setSpecification(specification.asSpecification());
    maintainState(out, services.getInfrastructureService().read(out.getKey()));
    return out;
  }

  public Infrastructure update(InfrastructureKeyInput key, SpecificationInput specification) {
    return services.getInfrastructureService().update(asInfrastructure(key, specification)).get();
  }

  public Infrastructure upsert(InfrastructureKeyInput key, SpecificationInput specification) {
    return services.getInfrastructureService().upsert(asInfrastructure(key, specification)).get();
  }

  public Boolean delete(InfrastructureKeyInput key) {
    throw new UnsupportedOperationException("delete");
  }

  public Infrastructure updateStatus(InfrastructureKeyInput key, StatusInput status) {
    Infrastructure infrastructure = services.getInfrastructureService().read(key.asInfrastructureKey()).get();
    infrastructure.setStatus(status.asStatus());
    return services.getInfrastructureService().update(infrastructure).get();
  }
}
