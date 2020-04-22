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
import com.expediagroup.streamplatform.streamregistry.core.repositories.DomainRepository;
import com.expediagroup.streamplatform.streamregistry.core.validators.DomainValidator;
import com.expediagroup.streamplatform.streamregistry.core.validators.ValidationException;
import com.expediagroup.streamplatform.streamregistry.model.Domain;
import com.expediagroup.streamplatform.streamregistry.model.keys.DomainKey;

@Component
@RequiredArgsConstructor
public class DomainService {
  private final DataToModel dataToModel;
  private final ModelToData modelToData;
  private final HandlerService handlerService;
  private final DomainValidator domainValidator;
  private final DomainRepository domainRepository;
  private final NotificationEventEmitter<Domain> domainServiceEventEmitter;

  public Optional<Domain> create(Domain domain) throws ValidationException {
    var data = modelToData.convertToData(domain);
    if (domainRepository.findById(data.getKey()).isPresent()) {
      throw new ValidationException("Can't create because it already exists");
    }
    domainValidator.validateForCreate(domain);
    data.setSpecification(modelToData.convertToData(handlerService.handleInsert(domain)));
    Domain out = dataToModel.convertToModel(domainRepository.save(data));
    domainServiceEventEmitter.emitEventOnProcessedEntity(EventType.CREATE, out);
    return Optional.ofNullable(out);
  }

  public Optional<Domain> read(DomainKey key) {
    var data = domainRepository.findById(modelToData.convertToData(key));
    return data.map(dataToModel::convertToModel);
  }

  public Optional<Domain> update(Domain domain) throws ValidationException {
    var domainData = modelToData.convertToData(domain);
    var existing = domainRepository.findById(domainData.getKey());
    if (!existing.isPresent()) {
      throw new ValidationException("Can't update " + domain.getKey().getName() + " because it doesn't exist");
    }
    domainValidator.validateForUpdate(domain, dataToModel.convertToModel(existing.get()));
    domainData.setSpecification(modelToData.convertToData(handlerService.handleUpdate(domain, dataToModel.convertToModel(existing.get()))));
    Domain out = dataToModel.convertToModel(domainRepository.save(domainData));
    domainServiceEventEmitter.emitEventOnProcessedEntity(EventType.UPDATE, out);
    return Optional.ofNullable(out);
  }

  public Optional<Domain> upsert(Domain domain) throws ValidationException {
    var DomainData = modelToData.convertToData(domain);
    return !domainRepository.findById(DomainData.getKey()).isPresent() ?
        create(domain) :
        update(domain);
  }

  public void delete(Domain domain) {
    throw new UnsupportedOperationException();
  }

  public List<Domain> findAll(Predicate<Domain> filter) {
    return domainRepository.findAll().stream().map(d -> dataToModel.convertToModel(d)).filter(filter).collect(toList());
  }

  public boolean exists(DomainKey key) {
    return read(key).isPresent();
  }
}
