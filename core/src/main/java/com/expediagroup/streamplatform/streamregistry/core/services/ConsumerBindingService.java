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
package com.expediagroup.streamplatform.streamregistry.core.services;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Predicate;

import org.springframework.data.domain.Example;
import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.DataToModel;
import com.expediagroup.streamplatform.streamregistry.ModelToData;
import com.expediagroup.streamplatform.streamregistry.core.events.EventType;
import com.expediagroup.streamplatform.streamregistry.core.events.NotificationEventEmitter;
import com.expediagroup.streamplatform.streamregistry.core.handlers.HandlerService;
import com.expediagroup.streamplatform.streamregistry.core.repositories.ConsumerBindingRepository;
import com.expediagroup.streamplatform.streamregistry.core.validators.ConsumerBindingValidator;
import com.expediagroup.streamplatform.streamregistry.model.ConsumerBinding;
import com.expediagroup.streamplatform.streamregistry.model.keys.ConsumerBindingKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.ConsumerKey;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ConsumerBindingService {
  private final HandlerService handlerService;
  private final ConsumerBindingValidator consumerBindingValidator;
  private final ConsumerBindingRepository consumerBindingRepository;
  private final NotificationEventEmitter<ConsumerBinding> consumerBindingServiceEventEmitter;

  public Optional<ConsumerBinding> create(ConsumerBinding consumerBindingModel) throws ValidationException {
    consumerBindingValidator.validateForCreate(consumerBindingModel);
    com.expediagroup.streamplatform.streamregistry.data.ConsumerBinding consumerBindingData =
        ModelToData.convertConsumerBinding(consumerBindingModel);
    if (consumerBindingRepository.findById(consumerBindingData.getKey()).isPresent()) {
      throw new ValidationException("Can't create because it already exists");
    }
    consumerBindingData.setSpecification(handlerService.handleInsert(consumerBindingData));
    ConsumerBinding out = DataToModel.convert(consumerBindingRepository.save(consumerBindingData));
    consumerBindingServiceEventEmitter.emitEventOnProcessedEntity(EventType.CREATE, out);
    return Optional.ofNullable(out);
  }

  public Optional<ConsumerBinding> read(ConsumerBindingKey key) {
    Optional<com.expediagroup.streamplatform.streamregistry.data.ConsumerBinding> data =
        consumerBindingRepository.findById(ModelToData.convertConsumerBindingKey(key));
    return data.isPresent() ? Optional.of(DataToModel.convert(data.get())) : Optional.empty();
  }

  public Iterable<ConsumerBinding> readAll() {
    ArrayList out = new ArrayList();
    for (com.expediagroup.streamplatform.streamregistry.data.ConsumerBinding cb : consumerBindingRepository.findAll()) {
      out.add(DataToModel.convert(cb));
    }
    return out;
  }

  public Optional<ConsumerBinding> update(ConsumerBinding consumerBinding) throws ValidationException {

    com.expediagroup.streamplatform.streamregistry.data.ConsumerBinding consumerBindingData =
        ModelToData.convertConsumerBinding(consumerBinding);

    Optional<com.expediagroup.streamplatform.streamregistry.data.ConsumerBinding> existing = consumerBindingRepository.findById(consumerBindingData.getKey());
    if (!existing.isPresent()) {
      throw new ValidationException("Can't update because it doesn't exist");
    }

    consumerBindingValidator.validateForUpdate(consumerBinding, DataToModel.convert(existing.get()));

    consumerBindingData.setSpecification(handlerService.handleInsert(consumerBindingData));

    ConsumerBinding out = DataToModel.convert(consumerBindingRepository.save(consumerBindingData));
    consumerBindingServiceEventEmitter.emitEventOnProcessedEntity(EventType.UPDATE, out);
    return Optional.ofNullable(out);
  }

  public Optional<ConsumerBinding> upsert(ConsumerBinding consumerBinding) throws ValidationException {

    com.expediagroup.streamplatform.streamregistry.data.ConsumerBinding consumerBindingData =
        ModelToData.convertConsumerBinding(consumerBinding);

    return !consumerBindingRepository.findById(consumerBindingData.getKey()).isPresent() ?
        create(consumerBinding) :
        update(consumerBinding);
  }

  public void delete(ConsumerBinding consumerBinding) {
    throw new UnsupportedOperationException();
  }

  public Iterable<ConsumerBinding> findAll(Predicate<ConsumerBinding> filter) {
    return consumerBindingRepository.findAll().stream().map(d -> DataToModel.convert(d)).filter(filter).collect(toList());
  }

  public Optional<ConsumerBinding> find(ConsumerKey key) {
    com.expediagroup.streamplatform.streamregistry.data.ConsumerBinding example = new
        com.expediagroup.streamplatform.streamregistry.data.ConsumerBinding(
        new com.expediagroup.streamplatform.streamregistry.data.keys.ConsumerBindingKey(
            key.getStreamDomain(),
            key.getStreamName(),
            key.getStreamVersion(),
            key.getZone(),
            null,
            key.getName()
        ), null, null);

    return consumerBindingRepository.findAll(Example.of(example)).stream().findFirst()
        .map(d -> DataToModel.convert(d));
  }

  public boolean exists(ConsumerBindingKey key) {
    return read(key).isEmpty();
  }

  @Deprecated
  public void validateConsumerBindingExists(ConsumerBindingKey key) {
    if (!exists(key)) {
      throw new ValidationException("ConsumerBinding does not exist");
    }
  }
}
