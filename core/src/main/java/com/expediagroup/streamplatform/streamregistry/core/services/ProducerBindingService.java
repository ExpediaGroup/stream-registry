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
import com.expediagroup.streamplatform.streamregistry.core.repositories.ProducerBindingRepository;
import com.expediagroup.streamplatform.streamregistry.core.validators.ProducerBindingValidator;
import com.expediagroup.streamplatform.streamregistry.core.validators.ValidationException;
import com.expediagroup.streamplatform.streamregistry.model.ProducerBinding;
import com.expediagroup.streamplatform.streamregistry.model.keys.ProducerBindingKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.ProducerKey;

@Component
@RequiredArgsConstructor
public class ProducerBindingService {
  private final DataToModel dataToModel;
  private final ModelToData modelToData;
  private final HandlerService handlerService;
  private final ProducerBindingValidator producerBindingValidator;
  private final ProducerBindingRepository producerBindingRepository;
  private final NotificationEventEmitter<ProducerBinding> producerBindingServiceEventEmitter;

  public Optional<ProducerBinding> create(ProducerBinding producerBinding) throws ValidationException {
    var data = modelToData.convertToData(producerBinding);
    if (producerBindingRepository.findById(data.getKey()).isPresent()) {
      throw new ValidationException("Can't create because it already exists");
    }
    producerBindingValidator.validateForCreate(producerBinding);
    data.setSpecification(modelToData.convertToData(handlerService.handleInsert(producerBinding)));
    ProducerBinding out = dataToModel.convertToModel(producerBindingRepository.save(data));
    producerBindingServiceEventEmitter.emitEventOnProcessedEntity(EventType.CREATE, out);
    return Optional.ofNullable(out);
  }

  public Optional<ProducerBinding> read(ProducerBindingKey key) {
    var data = producerBindingRepository.findById(modelToData.convertToData(key));
    return data.map(dataToModel::convertToModel);
  }

  public Optional<ProducerBinding> update(ProducerBinding producerBinding) throws ValidationException {
    var producerBindingData = modelToData.convertToData(producerBinding);
    var existing = producerBindingRepository.findById(producerBindingData.getKey());
    if (!existing.isPresent()) {
      throw new ValidationException("Can't update because it doesn't exist");
    }
    producerBindingValidator.validateForUpdate(producerBinding, dataToModel.convertToModel(existing.get()));
    producerBindingData.setSpecification(modelToData.convertToData(handlerService.handleUpdate(producerBinding, dataToModel.convertToModel(existing.get()))));
    ProducerBinding out = dataToModel.convertToModel(producerBindingRepository.save(producerBindingData));
    producerBindingServiceEventEmitter.emitEventOnProcessedEntity(EventType.UPDATE, out);
    return Optional.ofNullable(out);
  }

  public Optional<ProducerBinding> upsert(ProducerBinding producerBinding) throws ValidationException {
    return !producerBindingRepository.findById(modelToData.convertToData(producerBinding).getKey()).isPresent() ?
        create(producerBinding) :
        update(producerBinding);
  }

  public void delete(ProducerBinding producerBinding) {
    throw new UnsupportedOperationException();
  }

  public List<ProducerBinding> findAll(Predicate<ProducerBinding> filter) {
    return producerBindingRepository.findAll().stream().map(d -> dataToModel.convertToModel(d)).filter(filter).collect(toList());
  }

  public Optional<ProducerBinding> find(ProducerKey key) {
    var example =
        new com.expediagroup.streamplatform.streamregistry.data.ProducerBinding(
            new com.expediagroup.streamplatform.streamregistry.data.keys.ProducerBindingKey(
                key.getStreamDomain(),
                key.getStreamName(),
                key.getStreamVersion(),
                key.getZone(),
                null,
                key.getName()
            ), null, null);

    return producerBindingRepository
        .findAll(Example.of(example)).stream()
        .findFirst()
        .map(d -> dataToModel.convertToModel(d));
  }

  public boolean exists(ProducerBindingKey key) {
    return read(key).isPresent();
  }
}
