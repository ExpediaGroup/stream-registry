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

import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.handlers.HandlerService;
import com.expediagroup.streamplatform.streamregistry.core.validators.ValidationException;
import com.expediagroup.streamplatform.streamregistry.core.validators.ZoneValidator;
import com.expediagroup.streamplatform.streamregistry.model.Status;
import com.expediagroup.streamplatform.streamregistry.model.Zone;
import com.expediagroup.streamplatform.streamregistry.model.keys.ZoneKey;
import com.expediagroup.streamplatform.streamregistry.repository.ZoneRepository;

@Component
@RequiredArgsConstructor
public class ZoneService {
  private final HandlerService handlerService;
  private final ZoneValidator zoneValidator;
  private final ZoneRepository zoneRepository;

  @PreAuthorize("hasPermission(#zone, 'CREATE')")
  public Optional<Zone> create(Zone zone) throws ValidationException {
    if (read(zone.getKey()).isPresent()) {
      throw new ValidationException("Can't create because it already exists");
    }
    zoneValidator.validateForCreate(zone);
    zone.setSpecification(handlerService.handleInsert(zone));
    return save(zone);
  }

  @PreAuthorize("hasPermission(#zone, 'UPDATE')")
  public Optional<Zone> update(Zone zone) throws ValidationException {
    var existing = read(zone.getKey());
    if (!existing.isPresent()) {
      throw new ValidationException("Can't update " + zone.getKey().getName() + " because it doesn't exist");
    }
    zoneValidator.validateForUpdate(zone, existing.get());
    zone.setSpecification(handlerService.handleUpdate(zone, existing.get()));
    return save(zone);
  }

  @PreAuthorize("hasPermission(#zone, 'UPDATE_STATUS')")
  public Optional<Zone> updateStatus(Zone zone, Status status) {
    zone.setStatus(status);
    return save(zone);
  }

  private Optional<Zone> save(Zone zone) {
    zone = zoneRepository.save(zone);
    return Optional.ofNullable(zone);
  }

  @PostAuthorize("returnObject.isEmpty() ? true: hasPermission(returnObject, 'READ')")
  public Optional<Zone> get(ZoneKey key) {
    return read(key);
  }

  public Optional<Zone> read(ZoneKey key) {
    return zoneRepository.findById(key);
  }

  @PostFilter("hasPermission(filterObject, 'READ')")
  public List<Zone> findAll(Predicate<Zone> filter) {
    return zoneRepository.findAll().stream().filter(filter).collect(toList());
  }

  @PreAuthorize("hasPermission(#zone, 'DELETE')")
  public void delete(Zone zone) {
    throw new UnsupportedOperationException();
  }

  public boolean exists(ZoneKey key) {
    return read(key).isPresent();
  }
}
