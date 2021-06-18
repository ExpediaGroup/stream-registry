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
package com.expediagroup.streamplatform.streamregistry.state;


import static com.expediagroup.streamplatform.streamregistry.state.model.status.StatusEntry.State.OK;

import java.time.Instant;
import java.util.Collections;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.expediagroup.streamplatform.streamregistry.state.model.Entity;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.DomainKey;
import com.expediagroup.streamplatform.streamregistry.state.model.event.Event;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.DefaultSpecification;
import com.expediagroup.streamplatform.streamregistry.state.model.status.DefaultStatus;
import com.expediagroup.streamplatform.streamregistry.state.model.status.StatusEntry;

final class SampleEntities {
  private SampleEntities() {}

  static final ObjectMapper mapper = new ObjectMapper();
  static final ObjectNode configuration = mapper.createObjectNode();
  static final DomainKey key = new DomainKey("domain");
  static final DefaultSpecification specification = new DefaultSpecification(
    "description",
    Collections.emptyList(),
    "type",
    configuration,
    Collections.emptyMap()
  );
  static final ObjectNode statusValue = mapper.createObjectNode();
  static final StatusEntry statusEntry = new StatusEntry(
    "name",
    statusValue,
    Instant.ofEpochMilli(0L),
    Instant.ofEpochMilli(0L),
    OK
  );
  static final DefaultStatus status = new DefaultStatus().with(statusEntry);
  static final Entity<DomainKey, DefaultSpecification> entity = new Entity<>(key, specification, status);
  static final Event<DomainKey, DefaultSpecification> specificationEvent = Event.specification(key, specification);
  static final Event<DomainKey, DefaultSpecification> statusEvent = Event.status(key, statusEntry);
  static final Event<DomainKey, DefaultSpecification> specificationDeletionEvent = Event.specificationDeletion(key);
  static final Event<DomainKey, DefaultSpecification> statusDeletionEvent = Event.statusDeletion(key, statusEntry.getName());
}
