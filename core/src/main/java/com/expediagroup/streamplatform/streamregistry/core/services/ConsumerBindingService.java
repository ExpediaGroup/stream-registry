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

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Example;
import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.DataToModel;
import com.expediagroup.streamplatform.streamregistry.ModelToData;
import com.expediagroup.streamplatform.streamregistry.core.events.EventType;
import com.expediagroup.streamplatform.streamregistry.core.events.NotificationEventEmitter;
import com.expediagroup.streamplatform.streamregistry.core.handlers.HandlerService;
import com.expediagroup.streamplatform.streamregistry.core.repositories.ConsumerBindingRepository;
import com.expediagroup.streamplatform.streamregistry.core.validators.ConsumerBindingValidator;
import com.expediagroup.streamplatform.streamregistry.core.validators.ValidationException;
import com.expediagroup.streamplatform.streamregistry.data.ConsumerBindingData;
import com.expediagroup.streamplatform.streamregistry.model.ConsumerBinding;
import com.expediagroup.streamplatform.streamregistry.model.keys.ConsumerBindingKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.ConsumerKey;

@Component
@RequiredArgsConstructor
public class ConsumerBindingService {

  private final DataToModel dataToModel;
  private final ModelToData modelToData;
  private final HandlerService handlerService;
  private final ConsumerBindingValidator consumerBindingValidator;
  private final ConsumerBindingRepository consumerBindingRepository;
  private final NotificationEventEmitter<ConsumerBinding> consumerBindingServiceEventEmitter;

  public Optional<ConsumerBinding> create(ConsumerBinding consumerBindingModel) throws ValidationException {
    consumerBindingValidator.validateForCreate(consumerBindingModel);
    var consumerBindingData = modelToData.convertToData(consumerBindingModel);
    if (consumerBindingRepository.findById(consumerBindingData.getKey()).isPresent()) {
      throw new ValidationException("Can't create because it already exists");
    }
    consumerBindingData.setSpecification(modelToData.convertToData(handlerService.handleInsert(consumerBindingModel)));
    ConsumerBinding out = dataToModel.convertToModel(consumerBindingRepository.save(consumerBindingData));
    consumerBindingServiceEventEmitter.emitEventOnProcessedEntity(EventType.CREATE, out);
    return Optional.ofNullable(out);
  }

  public Optional<ConsumerBinding> read(ConsumerBindingKey key) {
    var data = consumerBindingRepository.findById(modelToData.convertToData(key));
    return data.map(dataToModel::convertToModel);
  }

  public Optional<ConsumerBinding> update(ConsumerBinding consumerBinding) throws ValidationException {
    var consumerBindingData = modelToData.convertToData(consumerBinding);
    var existing = consumerBindingRepository.findById(consumerBindingData.getKey());
    if (!existing.isPresent()) {
      throw new ValidationException("Can't update because it doesn't exist");
    }

    consumerBindingValidator.validateForUpdate(consumerBinding, dataToModel.convertToModel(existing.get()));
    consumerBindingData.setSpecification(modelToData.convertToData(handlerService.handleUpdate(consumerBinding, dataToModel.convertToModel(existing.get()))));
    ConsumerBinding out = dataToModel.convertToModel(consumerBindingRepository.save(consumerBindingData));
    consumerBindingServiceEventEmitter.emitEventOnProcessedEntity(EventType.UPDATE, out);
    return Optional.ofNullable(out);
  }

  public Optional<ConsumerBinding> upsert(ConsumerBinding consumerBinding) throws ValidationException {
    return !consumerBindingRepository.findById(modelToData.convertToData(consumerBinding).getKey()).isPresent() ?
        create(consumerBinding) :
        update(consumerBinding);
  }

  public void delete(ConsumerBinding consumerBinding) {
    throw new UnsupportedOperationException();
  }

  public List<ConsumerBinding> findAll(Predicate<ConsumerBinding> filter) {
    return consumerBindingRepository.findAll().stream().map(d -> dataToModel.convertToModel(d)).filter(filter).collect(toList());
  }

  public Optional<ConsumerBinding> find(ConsumerKey key) {
    var example = new
        ConsumerBindingData(
        new com.expediagroup.streamplatform.streamregistry.data.keys.ConsumerBindingKey(
            key.getStreamDomain(),
            key.getStreamName(),
            key.getStreamVersion(),
            key.getZone(),
            null,
            key.getName()
        ), null, null);
    return consumerBindingRepository.findAll(Example.of(example)).stream().findFirst().map(dataToModel::convertToModel);
  }

  public boolean exists(ConsumerBindingKey key) {
    return read(key).isPresent();
  }
}
