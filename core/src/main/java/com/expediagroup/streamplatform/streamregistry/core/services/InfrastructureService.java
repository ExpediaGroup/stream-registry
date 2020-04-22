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

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.DataToModel;
import com.expediagroup.streamplatform.streamregistry.ModelToData;
import com.expediagroup.streamplatform.streamregistry.core.events.EventType;
import com.expediagroup.streamplatform.streamregistry.core.events.NotificationEventEmitter;
import com.expediagroup.streamplatform.streamregistry.core.handlers.HandlerService;
import com.expediagroup.streamplatform.streamregistry.core.repositories.InfrastructureRepository;
import com.expediagroup.streamplatform.streamregistry.core.validators.InfrastructureValidator;
import com.expediagroup.streamplatform.streamregistry.core.validators.ValidationException;
import com.expediagroup.streamplatform.streamregistry.model.Infrastructure;
import com.expediagroup.streamplatform.streamregistry.model.keys.InfrastructureKey;

@Component
@RequiredArgsConstructor
public class InfrastructureService {
  private final DataToModel dataToModel;
  private final ModelToData modelToData;
  private final HandlerService handlerService;
  private final InfrastructureValidator infrastructureValidator;
  private final InfrastructureRepository infrastructureRepository;
  private final NotificationEventEmitter<Infrastructure> infrastructureServiceEventEmitter;

  public Optional<Infrastructure> create(Infrastructure infrastructure) throws ValidationException {
    var data = modelToData.convertToData(infrastructure);
    if (infrastructureRepository.findById(data.getKey()).isPresent()) {
      throw new ValidationException("Can't create because it already exists");
    }
    infrastructureValidator.validateForCreate(infrastructure);
    data.setSpecification(modelToData.convertToData(handlerService.handleInsert(infrastructure)));
    Infrastructure out = dataToModel.convertToModel(infrastructureRepository.save(data));
    infrastructureServiceEventEmitter.emitEventOnProcessedEntity(EventType.CREATE, out);
    return Optional.ofNullable(out);
  }

  public Optional<Infrastructure> read(InfrastructureKey key) {
    var data = infrastructureRepository.findById(modelToData.convertToData(key));
    return data.map(dataToModel::convertToModel);
  }

  public Optional<Infrastructure> update(Infrastructure infrastructure) throws ValidationException {
    var infrastructureData = modelToData.convertToData(infrastructure);
    var existing = infrastructureRepository.findById(infrastructureData.getKey());
    if (!existing.isPresent()) {
      throw new ValidationException("Can't update because it doesn't exist");
    }
    infrastructureValidator.validateForUpdate(infrastructure, dataToModel.convertToModel(existing.get()));
    infrastructureData.setSpecification(modelToData.convertToData(handlerService.handleUpdate(infrastructure, dataToModel.convertToModel(existing.get()))));
    Infrastructure out = dataToModel.convertToModel(infrastructureRepository.save(infrastructureData));
    infrastructureServiceEventEmitter.emitEventOnProcessedEntity(EventType.UPDATE, out);
    return Optional.ofNullable(out);
  }

  public Optional<Infrastructure> upsert(Infrastructure infrastructure) throws ValidationException {
    var infrastructureData = modelToData.convertToData(infrastructure);
    return !infrastructureRepository.findById(infrastructureData.getKey()).isPresent() ?
        create(infrastructure) :
        update(infrastructure);
  }

  public void delete(Infrastructure infrastructure) {
    throw new UnsupportedOperationException();
  }

  public List<Infrastructure> findAll(Predicate<Infrastructure> filter) {
    return infrastructureRepository.findAll().stream().map(d -> dataToModel.convertToModel(d)).filter(filter).collect(toList());
  }

  public boolean exists(InfrastructureKey key) {
    return read(key).isPresent();
  }
}
