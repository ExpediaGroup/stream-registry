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
import com.expediagroup.streamplatform.streamregistry.core.repositories.DomainRepository;
import com.expediagroup.streamplatform.streamregistry.core.validators.DomainValidator;
import com.expediagroup.streamplatform.streamregistry.model.Domain;
import com.expediagroup.streamplatform.streamregistry.model.keys.DomainKey;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DomainService {
  private final HandlerService handlerService;
  private final DomainValidator domainValidator;
  private final DomainRepository domainRepository;
  private final NotificationEventEmitter<Domain> domainServiceEventEmitter;

  public Optional<Domain> create(Domain domain) throws ValidationException {
    com.expediagroup.streamplatform.streamregistry.data.Domain data =
        ModelToData.convertDomain(domain);

    if (domainRepository.findById(data.getKey()).isPresent()) {
      throw new ValidationException("Can't create because it already exists");
    }
    domainValidator.validateForCreate(domain);
    data.setSpecification(handlerService.handleInsert(ModelToData.convertDomain(domain)));
    Domain out = DataToModel.convert(domainRepository.save(data));
    domainServiceEventEmitter.emitEventOnProcessedEntity(EventType.CREATE, out);
    return Optional.ofNullable(out);
  }

  public Optional<Domain> read(DomainKey key) {
    Optional<com.expediagroup.streamplatform.streamregistry.data.Domain> data =
        domainRepository.findById(ModelToData.convertDomainKey(key));
    return data.isPresent() ? Optional.of(DataToModel.convert(data.get())) : Optional.empty();
  }

  public Iterable<Domain> readAll() {
    ArrayList out = new ArrayList();
    for (com.expediagroup.streamplatform.streamregistry.data.Domain domain : domainRepository.findAll()) {
      out.add(DataToModel.convert(domain));
    }
    return out;
  }

  public Optional<Domain> update(Domain domain) throws ValidationException {
    com.expediagroup.streamplatform.streamregistry.data.Domain domainData =
        ModelToData.convertDomain(domain);

    Optional<com.expediagroup.streamplatform.streamregistry.data.Domain> existing =
        domainRepository.findById(domainData.getKey());
    if (!existing.isPresent()) {
      throw new ValidationException("Can't update " + domain.getKey() + " because it doesn't exist");
    }
    domainValidator.validateForUpdate(domain, DataToModel.convert(existing.get()));
    domainData.setSpecification(handlerService.handleInsert(domainData));

    Domain out = DataToModel.convert(domainRepository.save(domainData));
    domainServiceEventEmitter.emitEventOnProcessedEntity(EventType.UPDATE, out);
    return Optional.ofNullable(out);
  }

  public Optional<Domain> upsert(Domain domain) throws ValidationException {

    com.expediagroup.streamplatform.streamregistry.data.Domain DomainData =
        ModelToData.convertDomain(domain);

    return !domainRepository.findById(DomainData.getKey()).isPresent() ?
        create(domain) :
        update(domain);
  }

  public void delete(Domain domain) {
    throw new UnsupportedOperationException();
  }

  public Iterable<Domain> findAll(Predicate<Domain> filter) {
    return domainRepository.findAll().stream().map(d -> DataToModel.convert(d)).filter(filter).collect(toList());
  }

  public boolean exists(DomainKey key) {
    return read(key).isEmpty();
  }

  @Deprecated
  public void validateDomainExists(DomainKey key) {
    if (!exists(key)) {
      throw new ValidationException("Domain does not exist");
    }
  }
}
