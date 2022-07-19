/**
 * Copyright (C) 2018-2022 Expedia, Inc.
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
import lombok.val;

import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.handlers.HandlerService;
import com.expediagroup.streamplatform.streamregistry.core.validators.DomainValidator;
import com.expediagroup.streamplatform.streamregistry.core.validators.ValidationException;
import com.expediagroup.streamplatform.streamregistry.core.views.ConsumerView;
import com.expediagroup.streamplatform.streamregistry.core.views.DomainView;
import com.expediagroup.streamplatform.streamregistry.core.views.ProcessView;
import com.expediagroup.streamplatform.streamregistry.core.views.ProducerView;
import com.expediagroup.streamplatform.streamregistry.core.views.SchemaView;
import com.expediagroup.streamplatform.streamregistry.core.views.StreamView;
import com.expediagroup.streamplatform.streamregistry.model.Domain;
import com.expediagroup.streamplatform.streamregistry.model.Status;
import com.expediagroup.streamplatform.streamregistry.model.keys.DomainKey;
import com.expediagroup.streamplatform.streamregistry.repository.DomainRepository;

@Component
@RequiredArgsConstructor
public class DomainService {
  private final DomainView domainView;
  private final HandlerService handlerService;
  private final DomainValidator domainValidator;
  private final DomainRepository domainRepository;

  private final StreamView streamView;

  private final ProducerView producerView;

  private final ConsumerView consumerView;

  private final SchemaView schemaView;

  private final ProcessView processView;

  @PreAuthorize("hasPermission(#domain, 'CREATE')")
  public Optional<Domain> create(Domain domain) throws ValidationException {
    if (domainView.get(domain.getKey()).isPresent()) {
      throw new ValidationException("Can't create " + domain.getKey() + " because it already exists");
    }
    domainValidator.validateForCreate(domain);
    domain.setSpecification(handlerService.handleInsert(domain));
    return save(domain);
  }

  @PreAuthorize("hasPermission(#domain, 'UPDATE')")
  public Optional<Domain> update(Domain domain) throws ValidationException {
    val existing = domainView.get(domain.getKey());
    if (!existing.isPresent()) {
      throw new ValidationException("Can't update " + domain.getKey().getName() + " because it doesn't exist");
    }
    domainValidator.validateForUpdate(domain, existing.get());
    domain.setSpecification(handlerService.handleUpdate(domain, existing.get()));
    return save(domain);
  }

  @PreAuthorize("hasPermission(#domain, 'UPDATE_STATUS')")
  public Optional<Domain> updateStatus(Domain domain, Status status) {
    domain.setStatus(status);
    return save(domain);
  }

  private Optional<Domain> save(Domain domain) {
    return Optional.ofNullable(domainRepository.save(domain));
  }

  @PostAuthorize("returnObject.isPresent() ? hasPermission(returnObject, 'READ') : true")
  public Optional<Domain> get(DomainKey key) {
    return domainView.get(key);
  }

  @PostFilter("hasPermission(filterObject, 'READ')")
  public List<Domain> findAll(Predicate<Domain> filter) {
    return domainRepository.findAll().stream().filter(filter).collect(toList());
  }

  @PreAuthorize("hasPermission(#domain, 'DELETE')")
  public void delete(Domain domain) {
    handlerService.handleDelete(domain);

    processView
      .findAll(p -> p.getKey().getDomainKey().equals(domain.getKey()))
      .findAny()
      .ifPresent(ex -> { throw errorHandler(); });

    streamView
      .findAll(s -> s.getKey().getDomainKey().equals(domain.getKey()))
      .findAny()
      .ifPresent(ex -> { throw errorHandler(); });

    schemaView
      .findAll(sc -> sc.getKey().getDomainKey().equals(domain.getKey()))
      .findAny()
      .ifPresent(ex -> { throw errorHandler(); });

    producerView
      .findAll(p -> p.getKey().getStreamKey().getDomainKey().equals(domain.getKey()))
      .findAny()
      .ifPresent(ex -> { throw errorHandler(); });

    consumerView
      .findAll(c -> c.getKey().getStreamKey().getDomainKey().equals(domain.getKey()))
      .findAny()
      .ifPresent(ex -> { throw errorHandler(); });

    domainRepository.delete(domain);
  }

  private static IllegalStateException errorHandler() {
    return new IllegalStateException("Domain in use.");
  }
}
