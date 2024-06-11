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

import java.util.Optional;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.services.ConsumerService;
import com.expediagroup.streamplatform.streamregistry.core.views.ConsumerView;
import com.expediagroup.streamplatform.streamregistry.graphql.StateHelper;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.ConsumerKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.SpecificationInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.StatusInput;
import com.expediagroup.streamplatform.streamregistry.graphql.mutation.ConsumerMutation;
import com.expediagroup.streamplatform.streamregistry.model.Consumer;

@Component
@RequiredArgsConstructor
public class ConsumerMutationImpl implements ConsumerMutation {

  private final ConsumerService consumerService;
  private final ConsumerView consumerView;

  @Value("${entityView.exist.check.enabled:true}")
  private boolean checkExistEnabled;

  @Value("${stream-registry.entity.status.enabled:true}")
  private boolean entityStatusEnabled;

  @Override
  public Consumer insert(ConsumerKeyInput key, SpecificationInput specification) {
    return consumerService.create(asConsumer(key, specification)).get();
  }

  @Override
  public Consumer update(ConsumerKeyInput key, SpecificationInput specification) {
    return consumerService.update(asConsumer(key, specification)).get();
  }

  @Override
  public Consumer upsert(ConsumerKeyInput key, SpecificationInput specification) {
    Consumer consumer = asConsumer(key, specification);
    if (!consumerView.get(consumer.getKey()).isPresent()) {
      return consumerService.create(consumer).get();
    } else {
      return consumerService.update(consumer).get();
    }
  }

  @Override
  public Boolean delete(ConsumerKeyInput key) {
    Optional<Consumer> consumer = consumerView.get(key.asConsumerKey());
    consumer.ifPresentOrElse(consumerService::delete, () -> {
      if (!checkExistEnabled) {
        consumerService.delete(new Consumer(key.asConsumerKey(), StateHelper.specification(), StateHelper.status()));
      }
    });
    return true;
  }

  @Override
  public Consumer updateStatus(ConsumerKeyInput key, StatusInput status) {
    Consumer consumer = consumerView.get(key.asConsumerKey()).get();

    if (entityStatusEnabled) {
      return consumerService.updateStatus(consumer, status.asStatus()).get();
    } else {
      return consumer;
    }
  }

  private Consumer asConsumer(ConsumerKeyInput key, SpecificationInput specification) {
    Consumer consumer = new Consumer();
    consumer.setKey(key.asConsumerKey());
    consumer.setSpecification(specification.asSpecification());
    maintainState(consumer, consumerView.get(consumer.getKey()));
    return consumer;
  }
}
