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
package com.expediagroup.streamplatform.streamregistry.core.services;

/**
 * Copyright (C) 2016-2019 Expedia Inc.
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

import static java.util.stream.StreamSupport.stream;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.handlers.HandlersForServices;
import com.expediagroup.streamplatform.streamregistry.core.repositories.ZoneRepository;
import com.expediagroup.streamplatform.streamregistry.core.validators.ZoneValidator;
import com.expediagroup.streamplatform.streamregistry.model.Zone;
import com.expediagroup.streamplatform.streamregistry.model.keys.ZoneKey;

@Component
public class ZoneService {

  ZoneRepository zoneRepository;
  ZoneValidator zoneValidator;
  private HandlersForServices handlerService;

  public ZoneService(
      ZoneRepository zoneRepository,
      ZoneValidator zoneValidator,
      HandlersForServices handlerService) {
    this.zoneRepository = zoneRepository;
    this.zoneValidator = zoneValidator;
    this.handlerService = handlerService;
  }

  public Optional<Zone> create(Zone zone) throws ValidationException {
    if (zoneRepository.findById(zone.getKey()).isPresent()) {
      throw new ValidationException("Can't create because it already exists");
    }
    zoneValidator.validateForCreate(zone);
    zone.setSpecification(handlerService.handleInsert(zone));
    return Optional.ofNullable(zoneRepository.save(zone));
  }

  public Optional<Zone> read(ZoneKey key) {
    return zoneRepository.findById(key);
  }

  public Iterable<Zone> readAll() {
    return zoneRepository.findAll();
  }

  public Optional<Zone> update(Zone zone) throws ValidationException {
    Optional<Zone> existing = zoneRepository.findById(zone.getKey());
    if (!existing.isPresent()) {
      throw new ValidationException("Can't update because it doesn't exist");
    }
    zoneValidator.validateForUpdate(zone, existing.get());
    zone.setSpecification(handlerService.handleUpdate(zone, existing.get()));
    return Optional.ofNullable(zoneRepository.save(zone));
  }

  public Optional<Zone> upsert(Zone zone) throws ValidationException {
    return !zoneRepository.findById(zone.getKey()).isPresent() ?
        create(zone) :
        update(zone);
  }

  public void delete(Zone zone) {
  }

  public Iterable<Zone> findAll(Predicate<Zone> filter) {
    return stream(zoneRepository.findAll().spliterator(), false)
        .filter(r -> filter.test(r))
        .collect(Collectors.toList());
  }
}