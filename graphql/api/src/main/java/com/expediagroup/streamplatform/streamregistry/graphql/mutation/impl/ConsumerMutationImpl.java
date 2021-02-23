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

import com.expediagroup.streamplatform.streamregistry.core.services.ConsumerService;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.ConsumerKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.SpecificationInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.StatusInput;
import com.expediagroup.streamplatform.streamregistry.graphql.mutation.ConsumerMutation;
import com.expediagroup.streamplatform.streamregistry.model.Consumer;

@Component
@RequiredArgsConstructor
public class ConsumerMutationImpl implements ConsumerMutation {
  private final ConsumerService consumerService;

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
    if (!consumerService.unsecuredGet(consumer.getKey()).isPresent()) {
      return consumerService.create(consumer).get();
    } else {
      return consumerService.update(consumer).get();
    }
  }

  @Override
  public void delete(ConsumerKeyInput key) {
    Consumer consumer = new Consumer();
    consumer.setKey(key.asConsumerKey());
    consumerService.delete(consumer);
  }

  @Override
  public Consumer updateStatus(ConsumerKeyInput key, StatusInput status) {
    Consumer consumer = consumerService.unsecuredGet(key.asConsumerKey()).get();
    return consumerService.updateStatus(consumer, status.asStatus()).get();
  }

  private Consumer asConsumer(ConsumerKeyInput key, SpecificationInput specification) {
    Consumer consumer = new Consumer();
    consumer.setKey(key.asConsumerKey());
    consumer.setSpecification(specification.asSpecification());
    maintainState(consumer, consumerService.unsecuredGet(consumer.getKey()));
    return consumer;
  }
}
