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
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.ProducerBindingKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.SpecificationInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.StatusInput;
import com.expediagroup.streamplatform.streamregistry.model.ProducerBinding;

public class ProducerBindingMutation {

  private Services services;

  public ProducerBindingMutation(Services services) {
    this.services = services;
  }

  public ProducerBinding insert(ProducerBindingKeyInput key, SpecificationInput specification) {
    return services.getProducerBindingService().upsert(asProducerBinding(key, specification)).get();
  }

  public ProducerBinding update(ProducerBindingKeyInput key, SpecificationInput specification) {
    return services.getProducerBindingService().update(asProducerBinding(key, specification)).get();
  }

  public ProducerBinding upsert(ProducerBindingKeyInput key, SpecificationInput specification) {
    return services.getProducerBindingService().upsert(asProducerBinding(key, specification)).get();
  }

  public Boolean delete(ProducerBindingKeyInput key) {
    throw new UnsupportedOperationException("delete");
  }

  public ProducerBinding updateStatus(ProducerBindingKeyInput key, StatusInput status) {
    ProducerBinding producerBinding = services.getProducerBindingService().read(key.asProducerBindingKey()).get();
    producerBinding.setStatus(status.asStatus());
    return services.getProducerBindingService().update(producerBinding).get();
  }

  private ProducerBinding asProducerBinding(ProducerBindingKeyInput key, SpecificationInput specification) {
    ProducerBinding producerBinding = new ProducerBinding();
    producerBinding.setKey(key.asProducerBindingKey());
    producerBinding.setSpecification(specification.asSpecification());
    maintainState(producerBinding, services.getProducerBindingService().read(producerBinding.getKey()));
    return producerBinding;
  }
}
