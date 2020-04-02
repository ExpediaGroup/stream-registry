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

import static com.expediagroup.streamplatform.streamregistry.DataToModel.convertToModel;
import static com.expediagroup.streamplatform.streamregistry.ModelToData.convertToData;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Predicate;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.events.EventType;
import com.expediagroup.streamplatform.streamregistry.core.events.NotificationEventEmitter;
import com.expediagroup.streamplatform.streamregistry.core.handlers.HandlerService;
import com.expediagroup.streamplatform.streamregistry.core.repositories.InfrastructureRepository;
import com.expediagroup.streamplatform.streamregistry.core.validators.InfrastructureValidator;
import com.expediagroup.streamplatform.streamregistry.core.validators.ValidationException;
import com.expediagroup.streamplatform.streamregistry.model.Infrastructure;
import com.expediagroup.streamplatform.streamregistry.model.keys.InfrastructureKey;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InfrastructureService {
  private final HandlerService handlerService;
  private final InfrastructureValidator infrastructureValidator;
  private final InfrastructureRepository infrastructureRepository;
  private final NotificationEventEmitter<Infrastructure> infrastructureServiceEventEmitter;

  public Optional<Infrastructure> create(Infrastructure infrastructure) throws ValidationException {
    var data = convertToData(infrastructure);
    if (infrastructureRepository.findById(data.getKey()).isPresent()) {
      throw new ValidationException("Can't create because it already exists");
    }
    infrastructureValidator.validateForCreate(infrastructure);
    data.setSpecification(convertToData(handlerService.handleInsert(infrastructure)));
    Infrastructure out = convertToModel(infrastructureRepository.save(data));
    infrastructureServiceEventEmitter.emitEventOnProcessedEntity(EventType.CREATE, out);
    return Optional.ofNullable(out);
  }

  public Optional<Infrastructure> read(InfrastructureKey key) {
    var data = infrastructureRepository.findById(convertToData(key));
    return data.isPresent() ? Optional.of(convertToModel(data.get())) : Optional.empty();
  }

  public Iterable<Infrastructure> readAll() {
    ArrayList out = new ArrayList();
    for (var infrastructure : infrastructureRepository.findAll()) {
      out.add(convertToModel(infrastructure));
    }
    return out;
  }

  public Optional<Infrastructure> update(Infrastructure infrastructure) throws ValidationException {
    var infrastructureData = convertToData(infrastructure);
    var existing = infrastructureRepository.findById(infrastructureData.getKey());
    if (!existing.isPresent()) {
      throw new ValidationException("Can't update because it doesn't exist");
    }
    infrastructureValidator.validateForUpdate(infrastructure, convertToModel(existing.get()));
    infrastructureData.setSpecification(convertToData(handlerService.handleUpdate(infrastructure, convertToModel(existing.get()))));
    Infrastructure out = convertToModel(infrastructureRepository.save(infrastructureData));
    infrastructureServiceEventEmitter.emitEventOnProcessedEntity(EventType.UPDATE, out);
    return Optional.ofNullable(out);
  }

  public Optional<Infrastructure> upsert(Infrastructure infrastructure) throws ValidationException {
    var infrastructureData = convertToData(infrastructure);
    return !infrastructureRepository.findById(infrastructureData.getKey()).isPresent() ?
        create(infrastructure) :
        update(infrastructure);
  }

  public void delete(Infrastructure infrastructure) {
    throw new UnsupportedOperationException();
  }

  public Iterable<Infrastructure> findAll(Predicate<Infrastructure> filter) {
    return infrastructureRepository.findAll().stream().map(d -> convertToModel(d)).filter(filter).collect(toList());
  }

  public boolean exists(InfrastructureKey key) {
    return read(key).isPresent();
  }
}
