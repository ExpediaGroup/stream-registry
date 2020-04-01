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
import com.expediagroup.streamplatform.streamregistry.core.repositories.ProducerBindingRepository;
import com.expediagroup.streamplatform.streamregistry.core.validators.ProducerBindingValidator;
import com.expediagroup.streamplatform.streamregistry.model.ProducerBinding;
import com.expediagroup.streamplatform.streamregistry.model.keys.ProducerBindingKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.ProducerKey;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProducerBindingService {
  private final HandlerService handlerService;
  private final ProducerBindingValidator producerBindingValidator;
  private final ProducerBindingRepository producerBindingRepository;
  private final NotificationEventEmitter<ProducerBinding> producerBindingServiceEventEmitter;

  public Optional<ProducerBinding> create(ProducerBinding producerBinding) throws ValidationException {
    com.expediagroup.streamplatform.streamregistry.data.ProducerBinding data =
        ModelToData.convertProducerBinding(producerBinding);

    if (producerBindingRepository.findById(data.getKey()).isPresent()) {
      throw new ValidationException("Can't create because it already exists");
    }
    producerBindingValidator.validateForCreate(producerBinding);
    data.setSpecification(handlerService.handleInsert(ModelToData.convertProducerBinding(producerBinding)));
    ProducerBinding out = DataToModel.convert(producerBindingRepository.save(data));
    producerBindingServiceEventEmitter.emitEventOnProcessedEntity(EventType.CREATE, out);
    return Optional.ofNullable(out);
  }

  public Optional<ProducerBinding> read(ProducerBindingKey key) {
    Optional<com.expediagroup.streamplatform.streamregistry.data.ProducerBinding> data =
        producerBindingRepository.findById(ModelToData.convertProducerBindingKey(key));
    return data.isPresent() ? Optional.of(DataToModel.convert(data.get())) : Optional.empty();
  }

  public Iterable<ProducerBinding> readAll() {
    ArrayList out = new ArrayList();
    for (com.expediagroup.streamplatform.streamregistry.data.ProducerBinding producerBinding : producerBindingRepository.findAll()) {
      out.add(DataToModel.convert(producerBinding));
    }
    return out;
  }

  public Optional<ProducerBinding> update(ProducerBinding producerBinding) throws ValidationException {
    com.expediagroup.streamplatform.streamregistry.data.ProducerBinding producerBindingData =
        ModelToData.convertProducerBinding(producerBinding);

    Optional<com.expediagroup.streamplatform.streamregistry.data.ProducerBinding> existing =
        producerBindingRepository.findById(producerBindingData.getKey());
    if (!existing.isPresent()) {
      throw new ValidationException("Can't update because it doesn't exist");
    }
    producerBindingValidator.validateForUpdate(producerBinding, DataToModel.convert(existing.get()));
    producerBindingData.setSpecification(handlerService.handleInsert(producerBindingData));
    ProducerBinding out = DataToModel.convert(producerBindingRepository.save(producerBindingData));
    producerBindingServiceEventEmitter.emitEventOnProcessedEntity(EventType.UPDATE, out);
    return Optional.ofNullable(out);
  }

  public Optional<ProducerBinding> upsert(ProducerBinding producerBinding) throws ValidationException {

    com.expediagroup.streamplatform.streamregistry.data.ProducerBinding producerBindingData =
        ModelToData.convertProducerBinding(producerBinding);

    return !producerBindingRepository.findById(producerBindingData.getKey()).isPresent() ?
        create(producerBinding) :
        update(producerBinding);
  }

  public void delete(ProducerBinding producerBinding) {
    throw new UnsupportedOperationException();
  }

  public Iterable<ProducerBinding> findAll(Predicate<ProducerBinding> filter) {
    return producerBindingRepository.findAll().stream().map(d -> DataToModel.convert(d)).filter(filter).collect(toList());
  }

  public Optional<ProducerBinding> find(ProducerKey key) {
    com.expediagroup.streamplatform.streamregistry.data.ProducerBinding example =
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
        .map(d -> DataToModel.convert(d));
  }


  public boolean exists(ProducerBindingKey key) {
    return read(key).isEmpty();
  }

  @Deprecated
  public void validateProducerBindingExists(ProducerBindingKey key) {
    if (!exists(key)) {
      throw new ValidationException("ProducerBinding does not exist");
    }
  }
}
