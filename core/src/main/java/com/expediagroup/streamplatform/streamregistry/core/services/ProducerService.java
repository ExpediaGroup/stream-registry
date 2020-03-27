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

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.DataToModel;
import com.expediagroup.streamplatform.streamregistry.ModelToData;
import com.expediagroup.streamplatform.streamregistry.core.events.EventType;
import com.expediagroup.streamplatform.streamregistry.core.events.NotificationEventEmitter;
import com.expediagroup.streamplatform.streamregistry.core.handlers.HandlerService;
import com.expediagroup.streamplatform.streamregistry.core.repositories.ProducerRepository;
import com.expediagroup.streamplatform.streamregistry.core.validators.ProducerValidator;
import com.expediagroup.streamplatform.streamregistry.model.Producer;
import com.expediagroup.streamplatform.streamregistry.model.keys.ProducerKey;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProducerService {
  private final HandlerService handlerService;
  private final ProducerValidator producerValidator;
  private final ProducerRepository producerRepository;
  private final NotificationEventEmitter<Producer> producerServiceEventEmitter;

  public Optional<Producer> create(Producer producer) throws ValidationException {
    com.expediagroup.streamplatform.streamregistry.data.Producer data =
        ModelToData.convertProducer(producer);

    if (producerRepository.findById(data.getKey()).isPresent()) {
      throw new ValidationException("Can't create because it already exists");
    }
    producerValidator.validateForCreate(producer);
    data.setSpecification(handlerService.handleInsert(ModelToData.convertProducer(producer)));
    Producer out = DataToModel.convertProducer(producerRepository.save(data));
    producerServiceEventEmitter.emitEventOnProcessedEntity(EventType.CREATE, out);
    return Optional.ofNullable(out);
  }

  public Optional<Producer> read(ProducerKey key) {
    Optional<com.expediagroup.streamplatform.streamregistry.data.Producer> data =
        producerRepository.findById(ModelToData.convertProducerKey(key));
    return data.isPresent() ? Optional.of(DataToModel.convertProducer(data.get())) : Optional.empty();
  }

  public Iterable<Producer> readAll() {
    ArrayList out = new ArrayList();
    for (com.expediagroup.streamplatform.streamregistry.data.Producer producer : producerRepository.findAll()) {
      out.add(DataToModel.convertProducer(producer));
    }
    return out;
  }

  public Optional<Producer> update(Producer producer) throws ValidationException {
    com.expediagroup.streamplatform.streamregistry.data.Producer producerData =
        ModelToData.convertProducer(producer);

    Optional<com.expediagroup.streamplatform.streamregistry.data.Producer> existing =
        producerRepository.findById(producerData.getKey());
    if (!existing.isPresent()) {
      throw new ValidationException("Can't update because it doesn't exist");
    }
    producerValidator.validateForUpdate(producer, DataToModel.convertProducer(existing.get()));
    producerData.setSpecification(handlerService.handleInsert(producerData));
    Producer out = DataToModel.convertProducer(producerRepository.save(producerData));
    producerServiceEventEmitter.emitEventOnProcessedEntity(EventType.UPDATE, out);
    return Optional.ofNullable(out);
  }

  public Optional<Producer> upsert(Producer producer) throws ValidationException {

    com.expediagroup.streamplatform.streamregistry.data.Producer producerData =
        ModelToData.convertProducer(producer);

    return !producerRepository.findById(producerData.getKey()).isPresent() ?
        create(producer) :
        update(producer);
  }

  public void delete(Producer producer) {
    throw new UnsupportedOperationException();
  }

  public Iterable<Producer> findAll(Predicate<Producer> filter) {
    return producerRepository.findAll().stream().map(d -> DataToModel.convertProducer(d)).filter(filter).collect(toList());
  }

  public boolean exists(ProducerKey key) {
    return read(key).isEmpty();
  }

  @Deprecated
  public void validateProducerExists(ProducerKey key) {
    if (!exists(key)) {
      throw new ValidationException("Producer does not exist");
    }
  }
}
