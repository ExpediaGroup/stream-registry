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
import com.expediagroup.streamplatform.streamregistry.core.repositories.InfrastructureRepository;
import com.expediagroup.streamplatform.streamregistry.core.validators.InfrastructureValidator;
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
    com.expediagroup.streamplatform.streamregistry.data.Infrastructure data =
        ModelToData.convertInfrastructure(infrastructure);

    if (infrastructureRepository.findById(data.getKey()).isPresent()) {
      throw new ValidationException("Can't create because it already exists");
    }
    infrastructureValidator.validateForCreate(infrastructure);
    data.setSpecification(handlerService.handleInsert(ModelToData.convertInfrastructure(infrastructure)));
    Infrastructure out = DataToModel.convert(infrastructureRepository.save(data));
    infrastructureServiceEventEmitter.emitEventOnProcessedEntity(EventType.CREATE, out);
    return Optional.ofNullable(out);
  }

  public Optional<Infrastructure> read(InfrastructureKey key) {
    Optional<com.expediagroup.streamplatform.streamregistry.data.Infrastructure> data =
        infrastructureRepository.findById(ModelToData.convertInfrastructureKey(key));
    return data.isPresent() ? Optional.of(DataToModel.convert(data.get())) : Optional.empty();
  }

  public Iterable<Infrastructure> readAll() {
    ArrayList out = new ArrayList();
    for (com.expediagroup.streamplatform.streamregistry.data.Infrastructure infrastructure : infrastructureRepository.findAll()) {
      out.add(DataToModel.convert(infrastructure));
    }
    return out;
  }

  public Optional<Infrastructure> update(Infrastructure infrastructure) throws ValidationException {
    com.expediagroup.streamplatform.streamregistry.data.Infrastructure infrastructureData =
        ModelToData.convertInfrastructure(infrastructure);

    Optional<com.expediagroup.streamplatform.streamregistry.data.Infrastructure> existing =
        infrastructureRepository.findById(infrastructureData.getKey());
    if (!existing.isPresent()) {
      throw new ValidationException("Can't update because it doesn't exist");
    }
    infrastructureValidator.validateForUpdate(infrastructure, DataToModel.convert(existing.get()));
    infrastructureData.setSpecification(handlerService.handleInsert(infrastructureData));
    Infrastructure out = DataToModel.convert(infrastructureRepository.save(infrastructureData));
    infrastructureServiceEventEmitter.emitEventOnProcessedEntity(EventType.UPDATE, out);
    return Optional.ofNullable(out);
  }

  public Optional<Infrastructure> upsert(Infrastructure infrastructure) throws ValidationException {

    com.expediagroup.streamplatform.streamregistry.data.Infrastructure infrastructureData =
        ModelToData.convertInfrastructure(infrastructure);

    return !infrastructureRepository.findById(infrastructureData.getKey()).isPresent() ?
        create(infrastructure) :
        update(infrastructure);
  }

  public void delete(Infrastructure infrastructure) {
    throw new UnsupportedOperationException();
  }

  public Iterable<Infrastructure> findAll(Predicate<Infrastructure> filter) {
    return infrastructureRepository.findAll().stream().map(d -> DataToModel.convert(d)).filter(filter).collect(toList());
  }

  public boolean exists(InfrastructureKey key) {
    return read(key).isEmpty();
  }

  @Deprecated
  public void validateInfrastructureExists(InfrastructureKey key) {
    if (!exists(key)) {
      throw new ValidationException("Infrastructure does not exist");
    }
  }
}
