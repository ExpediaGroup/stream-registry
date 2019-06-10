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
import com.expediagroup.streamplatform.streamregistry.core.predicate.DomainConfiguredEntityPredicateFactory;
import com.expediagroup.streamplatform.streamregistry.core.validator.ConfiguredEntityValidator;
import com.expediagroup.streamplatform.streamregistry.core.validator.DomainConfiguredEntityValidator;
import com.expediagroup.streamplatform.streamregistry.core.validator.EntityValidator;
import com.expediagroup.streamplatform.streamregistry.model.Schema;
import com.expediagroup.streamplatform.streamregistry.repository.Repository;
import com.expediagroup.streamplatform.streamregistry.service.Service;

@Component
@RequiredArgsConstructor
public class SchemaService implements Service<Schema, Schema.Key> {
  private final EntityValidator entityValidator;
  private final ConfiguredEntityValidator configuredEntityValidator;
  private final DomainConfiguredEntityValidator domainConfiguredEntityValidator;
  private final HandlerWrapper<Schema> schemaHandler;
  private final Repository<Schema, Schema.Key> schemaRepository;
  private final DomainConfiguredEntityPredicateFactory domainConfiguredEntityPredicateFactory;

  @Override
  public void upsert(Schema schema) {
    Optional<Schema> existing = schemaRepository.get(schema.key());
    entityValidator.validate(schema, existing);
    configuredEntityValidator.validate(schema, existing);
    domainConfiguredEntityValidator.validate(schema, existing);

    Schema handled = schemaHandler.handle(schema, existing);
    schemaRepository.upsert(handled);
  }

  @Override
  public Schema get(Schema.Key key) {
    return schemaRepository
        .get(key)
        .orElseThrow(() -> new IllegalArgumentException(key + " does not exist."));
  }

  @Override
  public Stream<Schema> stream(Schema query) {
    return schemaRepository
        .stream()
        .filter(domainConfiguredEntityPredicateFactory.create(query));
  }
}
