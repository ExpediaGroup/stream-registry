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
import com.expediagroup.streamplatform.streamregistry.core.repositories.ConsumerRepository;
import com.expediagroup.streamplatform.streamregistry.core.validators.ConsumerValidator;
import com.expediagroup.streamplatform.streamregistry.model.Consumer;
import com.expediagroup.streamplatform.streamregistry.model.keys.ConsumerKey;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ConsumerService {
  private final HandlerService handlerService;
  private final ConsumerValidator consumerValidator;
  private final ConsumerRepository consumerRepository;
  private final NotificationEventEmitter<Consumer> consumerServiceEventEmitter;

  public Optional<Consumer> create(Consumer consumer) throws ValidationException {
    com.expediagroup.streamplatform.streamregistry.data.Consumer data =
        ModelToData.convertConsumer(consumer);

    if (consumerRepository.findById(data.getKey()).isPresent()) {
      throw new ValidationException("Can't create because it already exists");
    }
    consumerValidator.validateForCreate(consumer);
    data.setSpecification(handlerService.handleInsert(ModelToData.convertConsumer(consumer)));
    Consumer out = DataToModel.convert(consumerRepository.save(data));
    consumerServiceEventEmitter.emitEventOnProcessedEntity(EventType.CREATE, out);
    return Optional.ofNullable(out);
  }

  public Optional<Consumer> read(ConsumerKey key) {
    Optional<com.expediagroup.streamplatform.streamregistry.data.Consumer> data =
        consumerRepository.findById(ModelToData.convertConsumerKey(key));
    return data.isPresent() ? Optional.of(DataToModel.convert(data.get())) : Optional.empty();
  }

  public Iterable<Consumer> readAll() {
    ArrayList out = new ArrayList();
    for (com.expediagroup.streamplatform.streamregistry.data.Consumer consumer : consumerRepository.findAll()) {
      out.add(DataToModel.convert(consumer));
    }
    return out;
  }

  public Optional<Consumer> update(Consumer consumer) throws ValidationException {
    com.expediagroup.streamplatform.streamregistry.data.Consumer consumerData =
        ModelToData.convertConsumer(consumer);

    Optional<com.expediagroup.streamplatform.streamregistry.data.Consumer> existing =
        consumerRepository.findById(consumerData.getKey());
    if (!existing.isPresent()) {
      throw new ValidationException("Can't update because it doesn't exist");
    }
    consumerValidator.validateForUpdate(consumer, DataToModel.convert(existing.get()));
    consumerData.setSpecification(handlerService.handleInsert(consumerData));
    Consumer out = DataToModel.convert(consumerRepository.save(consumerData));
    consumerServiceEventEmitter.emitEventOnProcessedEntity(EventType.UPDATE, out);
    return Optional.ofNullable(out);
  }

  public Optional<Consumer> upsert(Consumer consumer) throws ValidationException {

    com.expediagroup.streamplatform.streamregistry.data.Consumer consumerData =
        ModelToData.convertConsumer(consumer);

    return !consumerRepository.findById(consumerData.getKey()).isPresent() ?
        create(consumer) :
        update(consumer);
  }

  public void delete(Consumer consumer) {
    throw new UnsupportedOperationException();
  }

  public Iterable<Consumer> findAll(Predicate<Consumer> filter) {
    return consumerRepository.findAll().stream().map(d -> DataToModel.convert(d)).filter(filter).collect(toList());
  }

  public boolean exists(ConsumerKey key) {
    return read(key).isEmpty();
  }

  @Deprecated
  public void validateConsumerExists(ConsumerKey key) {
    if (!exists(key)) {
      throw new ValidationException("Consumer does not exist");
    }
  }
}
