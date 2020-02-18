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
package com.expediagroup.streamplatform.streamregistry.graphql.resolvers;

import lombok.RequiredArgsConstructor;

import com.coxautodev.graphql.tools.GraphQLResolver;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.services.ZoneService;
import com.expediagroup.streamplatform.streamregistry.model.Infrastructure;
import com.expediagroup.streamplatform.streamregistry.model.Status;
import com.expediagroup.streamplatform.streamregistry.model.Zone;
import com.expediagroup.streamplatform.streamregistry.model.keys.ZoneKey;

@Component
@RequiredArgsConstructor
public class InfrastructureResolver implements GraphQLResolver<Infrastructure> {
  private final ZoneService zoneService;

  public Zone zone(Infrastructure infrastructure) {
    ZoneKey zoneKey = new ZoneKey(
        infrastructure.getKey().getZone()
    );
    return zoneService.read(zoneKey).orElse(null);
  }

  public Status status(Infrastructure infrastructure) {
    return infrastructure.getStatus() == null ? new Status() : infrastructure.getStatus();
  }
}
