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
package com.expediagroup.streamplatform.streamregistry.model;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class StatusEntry {
  private String name;
  private ObjectNode value;
  private Instant createdTs;
  private Instant updatedTs;
  private StatusEntry.State state;

  public StatusEntry(
    String name,
    ObjectNode value,
    StatusEntry.State state
  ) {
    this(name, value, null, null, state);
  }

  public ObjectNode getValue() {
    return value == null ? new ObjectMapper().createObjectNode() : value;
  }

  public enum State {
    PENDING,
    OK,
    ERROR,
    UNDEFINED
  }
}
