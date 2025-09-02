/**
 * Copyright (C) 2018-2025 Expedia, Inc.
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

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.services.ProducerBindingService;
import com.expediagroup.streamplatform.streamregistry.core.views.ProducerBindingView;
import com.expediagroup.streamplatform.streamregistry.graphql.StateHelper;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.ProducerBindingKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.SpecificationInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.StatusInput;
import com.expediagroup.streamplatform.streamregistry.graphql.mutation.ProducerBindingMutation;
import com.expediagroup.streamplatform.streamregistry.model.ProducerBinding;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProducerBindingMutationImpl implements ProducerBindingMutation {

  @Value("${entityView.exist.check.enabled:true}")
  private boolean checkExistEnabled;

  private final ProducerBindingService producerBindingService;
  private final ProducerBindingView producerBindingView;

  @Override
  public ProducerBinding insert(ProducerBindingKeyInput key, SpecificationInput specification) {
    return producerBindingService.create(asProducerBinding(key, specification)).get();
  }

  @Override
  public ProducerBinding update(ProducerBindingKeyInput key, SpecificationInput specification) {
    return producerBindingService.update(asProducerBinding(key, specification)).get();
  }

  @Override
  public ProducerBinding upsert(ProducerBindingKeyInput key, SpecificationInput specification) {
    ProducerBinding producerBinding = asProducerBinding(key, specification);
    if (!producerBindingView.get(producerBinding.getKey()).isPresent()) {
      return producerBindingService.create(producerBinding).get();
    } else {
      return producerBindingService.update(producerBinding).get();
    }
  }

  @Override
  public Boolean delete(ProducerBindingKeyInput key) {
    Optional<ProducerBinding> producerBinding = producerBindingView.get(key.asProducerBindingKey());
    producerBinding.ifPresentOrElse(producerBindingService::delete, () -> {
      if (!checkExistEnabled) {
        producerBindingService.delete(new ProducerBinding(key.asProducerBindingKey(), StateHelper.specification(), StateHelper.status()));
      }
    });
    return true;
  }

  @Override
  public ProducerBinding updateStatus(ProducerBindingKeyInput key, StatusInput status) {
    ProducerBinding producerBinding = producerBindingView.get(key.asProducerBindingKey()).get();
    return producerBindingService.updateStatus(producerBinding, status.asStatus()).get();
  }

  private ProducerBinding asProducerBinding(ProducerBindingKeyInput key, SpecificationInput specification) {
    ProducerBinding producerBinding = new ProducerBinding();
    producerBinding.setKey(key.asProducerBindingKey());
    producerBinding.setSpecification(specification.asSpecification());
    maintainState(producerBinding, producerBindingView.get(producerBinding.getKey()));
    return producerBinding;
  }
}
