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
import com.expediagroup.streamplatform.streamregistry.core.predicate.PatternMatchPredicateFactory;
import com.expediagroup.streamplatform.streamregistry.core.predicate.VersionPredicateFactory;
import com.expediagroup.streamplatform.streamregistry.core.validator.EntityValidator;
import com.expediagroup.streamplatform.streamregistry.model.Domain;
import com.expediagroup.streamplatform.streamregistry.model.Schema;
import com.expediagroup.streamplatform.streamregistry.model.Stream;
import com.expediagroup.streamplatform.streamregistry.repository.Repository;
import com.expediagroup.streamplatform.streamregistry.service.Service;

@Component
@RequiredArgsConstructor
public class StreamService implements Service<Stream, Stream.Key> {
  private final EntityValidator entityValidator;
  private final HandlerWrapper<Stream> streamHandler;
  private final Repository<Stream, Stream.Key> streamRepository;
  private final Repository<Schema, Schema.Key> schemaRepository;
  private final Repository<Domain, Domain.Key> domainrepository;
  private final PatternMatchPredicateFactory patternMatchPredicateFactory;
  private final VersionPredicateFactory versionPredicateFactory;

  @Override
  public void upsert(Stream stream) {
    Optional<Stream> existing = streamRepository.get(stream.key());
    entityValidator.validate(stream, existing);

    checkArgument(domainrepository.get(stream.getDomain()).isPresent(),
        "Domain '%s' does not exist.", stream.getDomain().getName());

    Optional
        .ofNullable(stream.getVersion())
        .filter(v -> v > 0)
        .orElseThrow(() -> new IllegalArgumentException("Version must be provided and greater than 0."));

    if (existing.isPresent()) {
      checkArgument(
          stream.getVersion() > existing.get().getVersion(),
          "Version greater than the current version.");
    } else {
      stream(Stream
          .builder()
          .name(stream.getName())
          .domain(stream.getDomain())
          .version(0) //get latest existing version
          .build())
          .findFirst()
          .map(Stream::getVersion)
          .ifPresent(latest -> checkArgument(
              stream.getVersion() == latest + 1,
              "Version must be 1 higher than the previous version."));
    }

    checkNotNull(stream.getSchema(), "Schema must be provided.");
    checkArgument(schemaRepository.get(stream.getSchema()).isPresent(), "Schema must exist.");

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
        .filter(patternMatchPredicateFactory.create(query, s -> Optional.ofNullable(s.getDomain()).map(Domain.Key::getName).orElse(null))
            .and(patternMatchPredicateFactory.create(query, s -> Optional.ofNullable(s.getSchema()).map(Schema.Key::getDomain).map(Domain.Key::getName).orElse(null)))
            .and(patternMatchPredicateFactory.create(query, s -> Optional.ofNullable(s.getSchema()).map(Schema.Key::getName).orElse(null)))));
  }
}
