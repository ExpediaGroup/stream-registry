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
package com.expediagroup.streamplatform.streamregistry.graphql.query.impl;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.services.ZoneService;
import com.expediagroup.streamplatform.streamregistry.graphql.filters.ZoneFilter;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.ZoneKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.SpecificationQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.ZoneKeyQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.query.ZoneQuery;
import com.expediagroup.streamplatform.streamregistry.model.Zone;

@Component
@RequiredArgsConstructor
public class ZoneQueryImpl implements ZoneQuery {
  private final ZoneService zoneService;

  @Override
  public Zone byKey(ZoneKeyInput key) {
    return zoneService.read(key.asZoneKey()).get();
  }

  @Override
  public Iterable<Zone> byQuery(ZoneKeyQuery key, SpecificationQuery specification) {
    return zoneService.findAll(new ZoneFilter(key, specification));
  }
}
