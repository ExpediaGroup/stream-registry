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
import com.expediagroup.streamplatform.streamregistry.core.repositories.ZoneRepository;
import com.expediagroup.streamplatform.streamregistry.core.validators.ZoneValidator;
import com.expediagroup.streamplatform.streamregistry.model.Zone;
import com.expediagroup.streamplatform.streamregistry.model.keys.ZoneKey;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ZoneService {
  private final HandlerService handlerService;
  private final ZoneValidator zoneValidator;
  private final ZoneRepository zoneRepository;
  private final NotificationEventEmitter<Zone> zoneServiceEventEmitter;

  public Optional<Zone> create(Zone zone) throws ValidationException {
    com.expediagroup.streamplatform.streamregistry.data.Zone data =
        ModelToData.convertZone(zone);

    if (zoneRepository.findById(data.getKey()).isPresent()) {
      throw new ValidationException("Can't create because it already exists");
    }
    zoneValidator.validateForCreate(zone);
    data.setSpecification(handlerService.handleInsert(ModelToData.convertZone(zone)));
    Zone out = DataToModel.convert(zoneRepository.save(data));
    zoneServiceEventEmitter.emitEventOnProcessedEntity(EventType.CREATE, out);
    return Optional.ofNullable(out);
  }

  public Optional<Zone> read(ZoneKey key) {
    Optional<com.expediagroup.streamplatform.streamregistry.data.Zone> data =
        zoneRepository.findById(ModelToData.convertZoneKey(key));
    return data.isPresent() ? Optional.of(DataToModel.convert(data.get())) : Optional.empty();
  }

  public Iterable<Zone> readAll() {
    ArrayList out = new ArrayList();
    for (com.expediagroup.streamplatform.streamregistry.data.Zone zone : zoneRepository.findAll()) {
      out.add(DataToModel.convert(zone));
    }
    return out;
  }

  public Optional<Zone> update(Zone zone) throws ValidationException {
    com.expediagroup.streamplatform.streamregistry.data.Zone zoneData =
        ModelToData.convertZone(zone);

    Optional<com.expediagroup.streamplatform.streamregistry.data.Zone> existing =
        zoneRepository.findById(zoneData.getKey());
    if (!existing.isPresent()) {
      throw new ValidationException("Can't update because it doesn't exist");
    }
    zoneValidator.validateForUpdate(zone, DataToModel.convert(existing.get()));
    zoneData.setSpecification(handlerService.handleInsert(zoneData));
    Zone out = DataToModel.convert(zoneRepository.save(zoneData));
    zoneServiceEventEmitter.emitEventOnProcessedEntity(EventType.UPDATE, out);
    return Optional.ofNullable(out);
  }

  public Optional<Zone> upsert(Zone zone) throws ValidationException {

    com.expediagroup.streamplatform.streamregistry.data.Zone zoneData =
        ModelToData.convertZone(zone);

    return !zoneRepository.findById(zoneData.getKey()).isPresent() ?
        create(zone) :
        update(zone);
  }

  public void delete(Zone zone) {
    throw new UnsupportedOperationException();
  }

  public Iterable<Zone> findAll(Predicate<Zone> filter) {
    return zoneRepository.findAll().stream().map(d -> DataToModel.convert(d)).filter(filter).collect(toList());
  }

  public boolean exists(ZoneKey key) {
    return read(key).isEmpty();
  }

  @Deprecated
  public void validateZoneExists(ZoneKey key) {
    if (!exists(key)) {
      throw new ValidationException("Zone does not exist");
    }
  }
}
