/**
 * Copyright (C) 2018-2021 Expedia, Inc.
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
package com.expediagroup.streamplatform.streamregistry.graphql.mutation.impl;

import static com.expediagroup.streamplatform.streamregistry.graphql.StateHelper.maintainState;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.services.ZoneService;
import com.expediagroup.streamplatform.streamregistry.core.views.ZoneView;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.SpecificationInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.StatusInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.ZoneKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.mutation.ZoneMutation;
import com.expediagroup.streamplatform.streamregistry.model.Zone;

@Component
@RequiredArgsConstructor
public class ZoneMutationImpl implements ZoneMutation {
  private final ZoneService zoneService;
  private final ZoneView zoneView;

  @Override
  public Zone insert(ZoneKeyInput key, SpecificationInput specification) {
    return zoneService.create(asZone(key, specification)).get();
  }

  @Override
  public Zone update(ZoneKeyInput key, SpecificationInput specification) {
    return zoneService.update(asZone(key, specification)).get();
  }

  @Override
  public Zone upsert(ZoneKeyInput key, SpecificationInput specification) {
    Zone zone = asZone(key, specification);
    if (!zoneView.get(zone.getKey()).isPresent()) {
      return zoneService.create(zone).get();
    } else {
      return zoneService.update(zone).get();
    }
  }

  @Override
  public Boolean delete(ZoneKeyInput key) {
    throw new UnsupportedOperationException("Zone deletion is not currently supported.");
  }

  @Override
  public Zone updateStatus(ZoneKeyInput key, StatusInput status) {
    Zone zone = zoneView.get(key.asZoneKey()).get();
    return zoneService.updateStatus(zone, status.asStatus()).get();
  }

  private Zone asZone(ZoneKeyInput key, SpecificationInput specification) {
    Zone zone = new Zone();
    zone.setKey(key.asZoneKey());
    zone.setSpecification(specification.asSpecification());
    maintainState(zone, zoneView.get(zone.getKey()));
    return zone;
  }
}
