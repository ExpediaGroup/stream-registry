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
package com.expediagroup.streamplatform.streamregistry.app.mutation;

import static com.expediagroup.streamplatform.streamregistry.app.StateHelper.maintainState;

import com.expediagroup.streamplatform.streamregistry.app.inputs.SpecificationInput;
import com.expediagroup.streamplatform.streamregistry.app.inputs.StatusInput;
import com.expediagroup.streamplatform.streamregistry.app.inputs.ZoneKeyInput;
import com.expediagroup.streamplatform.streamregistry.core.services.Services;
import com.expediagroup.streamplatform.streamregistry.model.Zone;

public class ZoneMutation {

  private Services services;

  public ZoneMutation(Services services) {
    this.services = services;
  }

  public Zone insert(ZoneKeyInput key, SpecificationInput specification) {
    return services.getZoneService().create(asZone(key, specification)).get();
  }

  public Zone update(ZoneKeyInput key, SpecificationInput specification) {
    return services.getZoneService().update(asZone(key, specification)).get();
  }

  public Zone upsert(ZoneKeyInput key, SpecificationInput specification) {
    return services.getZoneService().upsert(asZone(key, specification)).get();
  }

  private Zone asZone(ZoneKeyInput key, SpecificationInput specification) {
    Zone zone = new Zone();
    zone.setKey(key.asZoneKey());
    zone.setSpecification(specification.asSpecification());
    maintainState(zone, services.getZoneService().read(zone.getKey()));
    return zone;
  }

  public Boolean delete(ZoneKeyInput key) {
    throw new UnsupportedOperationException("deleteZone");
  }

  public Zone updateStatus(ZoneKeyInput key, StatusInput status) {
    Zone zone = services.getZoneService().read(key.asZoneKey()).get();
    zone.setStatus(status.asStatus());
    return services.getZoneService().update(zone).get();
  }
}
