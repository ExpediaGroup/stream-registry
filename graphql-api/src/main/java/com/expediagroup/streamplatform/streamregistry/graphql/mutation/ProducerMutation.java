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

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.services.ProducerService;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.ProducerKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.SpecificationInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.StatusInput;
import com.expediagroup.streamplatform.streamregistry.model.Producer;

@Component
@RequiredArgsConstructor
public class ProducerMutation {
  private final ProducerService producerService;

  public Producer insert(ProducerKeyInput key, SpecificationInput specification) {
    return producerService.create(asProducer(key, specification)).get();
  }

  public Producer update(ProducerKeyInput key, SpecificationInput specification) {
    return producerService.update(asProducer(key, specification)).get();
  }

  public Producer upsert(ProducerKeyInput key, SpecificationInput specification) {
    return producerService.upsert(asProducer(key, specification)).get();
  }

  private Producer asProducer(ProducerKeyInput key, SpecificationInput specification) {
    Producer producer = new Producer();
    producer.setKey(key.asProducerKey());
    producer.setSpecification(specification.asSpecification());
    maintainState(producer, producerService.read(producer.getKey()));
    return producer;
  }

  public Boolean delete(ProducerKeyInput key) {
    throw new UnsupportedOperationException("delete");
  }

  public Producer updateStatus(ProducerKeyInput key, StatusInput status) {
    Producer producer = producerService.read(key.asProducerKey()).get();
    producer.setStatus(status.asStatus());
    return producerService.update(producer).get();
  }
}
