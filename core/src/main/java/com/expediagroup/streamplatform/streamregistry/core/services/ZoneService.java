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

import static com.expediagroup.streamplatform.streamregistry.DataToModel.convertToModel;
import static com.expediagroup.streamplatform.streamregistry.ModelToData.convertToData;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.events.EventType;
import com.expediagroup.streamplatform.streamregistry.core.events.NotificationEventEmitter;
import com.expediagroup.streamplatform.streamregistry.core.handlers.HandlerService;
import com.expediagroup.streamplatform.streamregistry.core.repositories.ZoneRepository;
import com.expediagroup.streamplatform.streamregistry.core.validators.ValidationException;
import com.expediagroup.streamplatform.streamregistry.core.validators.ZoneValidator;
import com.expediagroup.streamplatform.streamregistry.model.Zone;
import com.expediagroup.streamplatform.streamregistry.model.keys.ZoneKey;

@Component
@RequiredArgsConstructor
public class ZoneService {
  private final HandlerService handlerService;
  private final ZoneValidator zoneValidator;
  private final ZoneRepository zoneRepository;
  private final NotificationEventEmitter<Zone> zoneServiceEventEmitter;

  public Optional<Zone> create(Zone zone) throws ValidationException {
    var data = convertToData(zone);
    if (zoneRepository.findById(data.getKey()).isPresent()) {
      throw new ValidationException("Can't create because it already exists");
    }
    zoneValidator.validateForCreate(zone);
    data.setSpecification(convertToData(handlerService.handleInsert(zone)));
    Zone out = convertToModel(zoneRepository.save(data));
    zoneServiceEventEmitter.emitEventOnProcessedEntity(EventType.CREATE, out);
    return Optional.ofNullable(out);
  }

  public Optional<Zone> read(ZoneKey key) {
    var data = zoneRepository.findById(convertToData(key));
    return data.isPresent() ? Optional.of(convertToModel(data.get())) : Optional.empty();
  }

  public Optional<Zone> update(Zone zone) throws ValidationException {
    var zoneData = convertToData(zone);
    var existing = zoneRepository.findById(zoneData.getKey());
    if (!existing.isPresent()) {
      throw new ValidationException("Can't update because it doesn't exist");
    }
    zoneValidator.validateForUpdate(zone, convertToModel(existing.get()));
    zoneData.setSpecification(convertToData(handlerService.handleUpdate(zone, convertToModel(existing.get()))));
    Zone out = convertToModel(zoneRepository.save(zoneData));
    zoneServiceEventEmitter.emitEventOnProcessedEntity(EventType.UPDATE, out);
    return Optional.ofNullable(out);
  }

  public Optional<Zone> upsert(Zone zone) throws ValidationException {
    return !zoneRepository.findById(convertToData(zone).getKey()).isPresent() ?
        create(zone) :
        update(zone);
  }

  public void delete(Zone zone) {
    throw new UnsupportedOperationException();
  }

  public List<Zone> findAll(Predicate<Zone> filter) {
    return zoneRepository.findAll().stream().map(d -> convertToModel(d)).filter(filter).collect(toList());
  }

  public boolean exists(ZoneKey key) {
    return read(key).isPresent();
  }
}
