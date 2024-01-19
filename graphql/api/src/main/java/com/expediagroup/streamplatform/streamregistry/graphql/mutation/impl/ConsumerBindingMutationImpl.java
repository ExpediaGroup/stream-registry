/**
 * Copyright (C) 2018-2024 Expedia, Inc.
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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.services.ConsumerBindingService;
import com.expediagroup.streamplatform.streamregistry.core.views.ConsumerBindingView;
import com.expediagroup.streamplatform.streamregistry.graphql.StateHelper;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.ConsumerBindingKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.SpecificationInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.StatusInput;
import com.expediagroup.streamplatform.streamregistry.graphql.mutation.ConsumerBindingMutation;
import com.expediagroup.streamplatform.streamregistry.model.ConsumerBinding;

@Component
@RequiredArgsConstructor
public class ConsumerBindingMutationImpl implements ConsumerBindingMutation {
  private final ConsumerBindingService consumerBindingService;
  private final ConsumerBindingView consumerBindingView;

  @Value("${entityView.exist.check.enabled:true}")
  private boolean checkExistEnabled;

  @Override
  public ConsumerBinding insert(ConsumerBindingKeyInput key, SpecificationInput specification) {
    return consumerBindingService.create(asConsumerBinding(key, specification)).get();
  }

  @Override
  public ConsumerBinding update(ConsumerBindingKeyInput key, SpecificationInput specification) {
    return consumerBindingService.update(asConsumerBinding(key, specification)).get();
  }

  @Override
  public ConsumerBinding upsert(ConsumerBindingKeyInput key, SpecificationInput specification) {
    ConsumerBinding consumerBinding = asConsumerBinding(key, specification);
    if (!consumerBindingView.get(consumerBinding.getKey()).isPresent()) {
      return consumerBindingService.create(consumerBinding).get();
    } else {
      return consumerBindingService.update(consumerBinding).get();
    }
  }

  @Override
  public Boolean delete(ConsumerBindingKeyInput key) {
    if(checkExistEnabled){
    consumerBindingView.get(key.asConsumerBindingKey()).ifPresent(consumerBindingService::delete);
    }else {
      ConsumerBinding consumerBinding = new ConsumerBinding(key.asConsumerBindingKey(), StateHelper.specification(), StateHelper.status());
      consumerBindingService.delete(consumerBinding);
    }
    return true;
  }

  @Override
  public ConsumerBinding updateStatus(ConsumerBindingKeyInput key, StatusInput status) {
    ConsumerBinding consumerBinding = consumerBindingView.get(key.asConsumerBindingKey()).get();
    return consumerBindingService.updateStatus(consumerBinding, status.asStatus()).get();
  }

  private ConsumerBinding asConsumerBinding(ConsumerBindingKeyInput key, SpecificationInput specification) {
    ConsumerBinding consumerBinding = new ConsumerBinding();
    consumerBinding.setKey(key.asConsumerBindingKey());
    consumerBinding.setSpecification(specification.asSpecification());
    maintainState(consumerBinding, consumerBindingView.get(key.asConsumerBindingKey()));
    return consumerBinding;
  }
}
