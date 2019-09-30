/**
 * Copyright (C) 2016-2019 Expedia Inc.
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

import com.expediagroup.streamplatform.streamregistry.app.inputs.ConsumerBindingKeyInput;
import com.expediagroup.streamplatform.streamregistry.app.inputs.SpecificationInput;
import com.expediagroup.streamplatform.streamregistry.app.inputs.StatusInput;
import com.expediagroup.streamplatform.streamregistry.core.services.Services;
import com.expediagroup.streamplatform.streamregistry.model.ConsumerBinding;

public class ConsumerBindingMutation {

  private Services services;

  public ConsumerBindingMutation(Services services) {
    this.services = services;
  }

  public ConsumerBinding insert(ConsumerBindingKeyInput key, SpecificationInput specification) {
    return services.getConsumerBindingService().create(asConsumerBinding(key, specification)).get();
  }

  public ConsumerBinding update(ConsumerBindingKeyInput key, SpecificationInput specification) {
    return services.getConsumerBindingService().update(asConsumerBinding(key, specification)).get();
  }

  public ConsumerBinding upsert(ConsumerBindingKeyInput key, SpecificationInput specification) {
    return services.getConsumerBindingService().upsert(asConsumerBinding(key, specification)).get();
  }

  public Boolean delete(ConsumerBindingKeyInput key) {
    throw new UnsupportedOperationException("delete");
  }

  public ConsumerBinding updateStatus(ConsumerBindingKeyInput key, StatusInput status) {
    ConsumerBinding consumerBinding = services.getConsumerBindingService().read(key.asConsumerBindingKey()).get();
    consumerBinding.setStatus(status.asStatus());
    return services.getConsumerBindingService().update(consumerBinding).get();
  }

  private ConsumerBinding asConsumerBinding(ConsumerBindingKeyInput key, SpecificationInput specification) {
    ConsumerBinding consumerBinding = new ConsumerBinding();
    consumerBinding.setKey(key.asConsumerBindingKey());
    consumerBinding.setSpecification(specification.asSpecification());
    maintainState(consumerBinding, services.getConsumerBindingService().read(key.asConsumerBindingKey()));
    return consumerBinding;
  }
}

