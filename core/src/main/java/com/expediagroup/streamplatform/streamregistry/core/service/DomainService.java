/**
 * Copyright (C) 2018-2019 Expedia, Inc.
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
package com.expediagroup.streamplatform.streamregistry.core.service;

import java.util.Optional;
import java.util.stream.Stream;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.handler.HandlerWrapper;
import com.expediagroup.streamplatform.streamregistry.core.predicate.EntityPredicateFactory;
import com.expediagroup.streamplatform.streamregistry.core.validator.EntityValidator;
import com.expediagroup.streamplatform.streamregistry.model.Domain;
import com.expediagroup.streamplatform.streamregistry.repository.Repository;
import com.expediagroup.streamplatform.streamregistry.service.Service;

@Component
@RequiredArgsConstructor
public class DomainService implements Service<Domain, Domain.Key> {
  private final EntityValidator entityValidator;
  private final HandlerWrapper<Domain> domainHandler;
  private final Repository<Domain, Domain.Key> domainRepository;
  private final EntityPredicateFactory entityPredicateFactory;

  @Override
  public void upsert(Domain domain) {
    Optional<Domain> existing = domainRepository.get(domain.key());
    entityValidator.validate(domain, existing);

    Domain handled = domainHandler.handle(domain, existing);
    domainRepository.upsert(handled);
  }

  @Override
  public Domain get(Domain.Key key) {
    return domainRepository
        .get(key)
        .orElseThrow(() -> new IllegalArgumentException(key + " does not exist."));
  }

  @Override
  public Stream<Domain> stream(Domain query) {
    return domainRepository
        .stream()
        .filter(entityPredicateFactory.create(query));
  }
}
