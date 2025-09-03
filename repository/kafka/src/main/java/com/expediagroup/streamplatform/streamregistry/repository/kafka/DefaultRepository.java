/**
 * Copyright (C) 2018-2025 Expedia, Inc.
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
package com.expediagroup.streamplatform.streamregistry.repository.kafka;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.expediagroup.streamplatform.streamregistry.state.EntityView;
import com.expediagroup.streamplatform.streamregistry.state.EventSender;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity;
import com.expediagroup.streamplatform.streamregistry.state.model.event.Event;
import com.expediagroup.streamplatform.streamregistry.state.model.status.StatusEntry;

import lombok.RequiredArgsConstructor;

/**
 * @param <ME> Model Entity
 * @param <MK> Model Key
 * @param <SK> State Key
 * @param <SS> State Specification
 */
@RequiredArgsConstructor
abstract class DefaultRepository<
    ME extends com.expediagroup.streamplatform.streamregistry.model.Entity<MK>,
    MK,
    SK extends Entity.Key<SS>,
    SS extends com.expediagroup.streamplatform.streamregistry.state.model.specification.Specification>
    implements com.expediagroup.streamplatform.streamregistry.repository.Repository<ME, MK> {
  private final EntityView view;
  private final EventSender sender;
  private final Converter<ME, MK, SK, SS> converter;
  private final Class<SK> stateKeyClass;

  @Override
  public ME saveSpecification(ME entity) {
    Optional<ME> existing = findById(entity.getKey());
    Entity<SK, SS> stateEntity = converter.convertEntity(entity);
    if (existing.isPresent()) {
      Entity<SK, SS> existingStateEntity = converter.convertEntity(existing.get());
      if (!existingStateEntity.getSpecification().equals(stateEntity.getSpecification())) {
        sender.send(Event.specification(stateEntity.getKey(), stateEntity.getSpecification())).join();
      }
    } else {
      sender.send(Event.specification(stateEntity.getKey(), stateEntity.getSpecification())).join();
    }
    return entity;
  }

  @Override
  public ME saveStatus(ME entity) {
    Optional<ME> existing = findById(entity.getKey());
    Entity<SK, SS> stateEntity = converter.convertEntity(entity);
    List<CompletableFuture<Void>> futures = new ArrayList<>();
    if (existing.isPresent()) {
      Entity<SK, SS> existingStateEntity = converter.convertEntity(existing.get());
      for (StatusEntry entry : stateEntity.getStatus().getEntries()) {
        if (existingStateEntity.getStatus().getNames().contains(entry.getName())
          && !entry.getValue().equals(existingStateEntity.getStatus().getValue(entry.getName()))) {
          send(Event.status(stateEntity.getKey(), entry), futures);
        }
      }
    } else {
      for (StatusEntry entry : stateEntity.getStatus().getEntries()) {
        send(Event.status(stateEntity.getKey(), entry), futures);
      }
    }
    futures.forEach(CompletableFuture::join);
    return entity;
  }

  private void send(Event<SK, SS> event, List<CompletableFuture<Void>> futures) {
    futures.add(sender.send(event));
  }

  @Override
  public Optional<ME> findById(MK key) {
    return view
        .get(converter.convertKey(key))
        .map(converter::convertEntity);
  }

  @Override
  public List<ME> findAll() {
    return view.all(stateKeyClass)
        .map(converter::convertEntity)
        .collect(toList());
  }

  @Override
  public List<ME> findAll(ME example) {
    // This is only used by ProducerBinding and ConsumerBinding
    throw new UnsupportedOperationException();
  }

  @Override
  public void delete(ME entity) {
    List<CompletableFuture<Void>> futures = new ArrayList<>();
    Entity<SK, SS> stateEntity = converter.convertEntity(entity);
    stateEntity.getStatus().getEntries().forEach(e -> send(Event.statusDeletion(stateEntity.getKey(), e.getName()), futures));
    send(Event.specificationDeletion(stateEntity.getKey()), futures);
    futures.forEach(CompletableFuture::join);
  }
}
