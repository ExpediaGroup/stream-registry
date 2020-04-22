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

import static com.expediagroup.streamplatform.streamregistry.DataToModel.convertToModel;
import static com.expediagroup.streamplatform.streamregistry.ModelToData.convertToData;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.events.EventType;
import com.expediagroup.streamplatform.streamregistry.core.events.NotificationEventEmitter;
import com.expediagroup.streamplatform.streamregistry.core.handlers.HandlerService;
import com.expediagroup.streamplatform.streamregistry.core.repositories.ConsumerRepository;
import com.expediagroup.streamplatform.streamregistry.core.validators.ConsumerValidator;
import com.expediagroup.streamplatform.streamregistry.core.validators.ValidationException;
import com.expediagroup.streamplatform.streamregistry.model.Consumer;
import com.expediagroup.streamplatform.streamregistry.model.keys.ConsumerKey;

@Component
@RequiredArgsConstructor
public class ConsumerService {
  private final HandlerService handlerService;
  private final ConsumerValidator consumerValidator;
  private final ConsumerRepository consumerRepository;
  private final NotificationEventEmitter<Consumer> consumerServiceEventEmitter;

  public Optional<Consumer> create(Consumer consumer) throws ValidationException {
    var data = convertToData(consumer);
    if (consumerRepository.findById(data.getKey()).isPresent()) {
      throw new ValidationException("Can't create because it already exists");
    }
    consumerValidator.validateForCreate(consumer);
    data.setSpecification(convertToData(handlerService.handleInsert(consumer)));
    Consumer out = convertToModel(consumerRepository.save(data));
    consumerServiceEventEmitter.emitEventOnProcessedEntity(EventType.CREATE, out);
    return Optional.ofNullable(out);
  }

  public Optional<Consumer> read(ConsumerKey key) {
    var data = consumerRepository.findById(convertToData(key));
    return data.isPresent() ? Optional.of(convertToModel(data.get())) : Optional.empty();
  }

  public Optional<Consumer> update(Consumer consumer) throws ValidationException {
    var consumerData = convertToData(consumer);
    var existing = consumerRepository.findById(consumerData.getKey());
    if (!existing.isPresent()) {
      throw new ValidationException("Can't update because it doesn't exist");
    }
    consumerValidator.validateForUpdate(consumer, convertToModel(existing.get()));
    consumerData.setSpecification(convertToData(handlerService.handleUpdate(consumer, convertToModel(existing.get()))));
    Consumer out = convertToModel(consumerRepository.save(consumerData));
    consumerServiceEventEmitter.emitEventOnProcessedEntity(EventType.UPDATE, out);
    return Optional.ofNullable(out);
  }

  public Optional<Consumer> upsert(Consumer consumer) throws ValidationException {
    var consumerData = convertToData(consumer);
    return !consumerRepository.findById(consumerData.getKey()).isPresent() ?
        create(consumer) :
        update(consumer);
  }

  public void delete(Consumer consumer) {
    throw new UnsupportedOperationException();
  }

  public List<Consumer> findAll(Predicate<Consumer> filter) {
    return consumerRepository.findAll().stream().map(d -> convertToModel(d)).filter(filter).collect(toList());
  }

  public boolean exists(ConsumerKey key) {
    return read(key).isPresent();
  }
}
