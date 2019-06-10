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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Optional;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.handler.HandlerWrapper;
import com.expediagroup.streamplatform.streamregistry.core.predicate.DomainConfiguredEntityPredicateFactory;
import com.expediagroup.streamplatform.streamregistry.core.predicate.NameDomainPatternMatchPredicateFactory;
import com.expediagroup.streamplatform.streamregistry.core.predicate.VersionPredicateFactory;
import com.expediagroup.streamplatform.streamregistry.core.validator.ConfiguredEntityValidator;
import com.expediagroup.streamplatform.streamregistry.core.validator.DomainConfiguredEntityValidator;
import com.expediagroup.streamplatform.streamregistry.core.validator.EntityValidator;
import com.expediagroup.streamplatform.streamregistry.model.Schema;
import com.expediagroup.streamplatform.streamregistry.model.Stream;
import com.expediagroup.streamplatform.streamregistry.repository.Repository;
import com.expediagroup.streamplatform.streamregistry.service.Service;

@Component
@RequiredArgsConstructor
public class StreamService implements Service<Stream, Stream.Key> {
  private final EntityValidator entityValidator;
  private final ConfiguredEntityValidator configuredEntityValidator;
  private final DomainConfiguredEntityValidator domainConfiguredEntityValidator;
  private final HandlerWrapper<Stream> streamHandler;
  private final Repository<Stream, Stream.Key> streamRepository;
  private final Repository<Schema, Schema.Key> schemaRepository;
  private final DomainConfiguredEntityPredicateFactory domainConfiguredEntityPredicateFactory;
  private final NameDomainPatternMatchPredicateFactory nameDomainPatternMatchPredicateFactory;
  private final VersionPredicateFactory versionPredicateFactory;

  @Override
  public void upsert(Stream stream) {
    Optional<Stream> existing = streamRepository.get(stream.key());
    entityValidator.validate(stream, existing);
    configuredEntityValidator.validate(stream, existing);
    domainConfiguredEntityValidator.validate(stream, existing);


    Optional
        .ofNullable(stream.getVersion())
        .filter(v -> v > 0)
        .orElseThrow(() -> new IllegalArgumentException("Version must be provided and greater than 0."));
    existing.ifPresent(e -> checkArgument(
        stream.getVersion() > e.getVersion(),
        "Version greater than the current version."));

    checkNotNull(stream.getSchema(), "Schema must be provided.");
    checkArgument(schemaRepository.get(stream.schemaKey()).isPresent(), "Schema must exist.");

    Stream handled = streamHandler.handle(stream, existing);
    streamRepository.upsert(handled);
  }

  @Override
  public Stream get(Stream.Key key) {
    return streamRepository
        .get(key)
        .orElseThrow(() -> new IllegalArgumentException(key + " does not exist."));
  }

  @Override
  public java.util.stream.Stream<Stream> stream(Stream query) {
    return versionPredicateFactory.filter(query, streamRepository
        .stream()
        .filter(domainConfiguredEntityPredicateFactory.create(query)
            .and(nameDomainPatternMatchPredicateFactory.create(query, Stream::getSchema))));
  }
}
