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
package com.expediagroup.streamplatform.streamregistry.graphql.model.inputs;

import static com.expediagroup.streamplatform.streamregistry.model.StatusEntry.State.UNDEFINED;
import static java.util.Collections.unmodifiableMap;

import java.time.Clock;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Value;

import com.fasterxml.jackson.databind.node.ObjectNode;

import com.expediagroup.streamplatform.streamregistry.model.Status;
import com.expediagroup.streamplatform.streamregistry.model.StatusEntry;

@Value
@Builder
public class StatusInput {
  @Deprecated
  ObjectNode agentStatus;
  List<StatusEntryInput> entries;

  public Status asStatus() {
    Map<String, StatusEntry> m = new HashMap<>();
    if (agentStatus != null) {
      // Backwards compatibility
      m.put("agentStatus",
        new StatusEntry(
          "agentStatus",
          agentStatus,
          Clock.systemUTC().instant(),
          UNDEFINED
        )
      );
    }
    if (entries != null) {
      for (StatusEntryInput i : entries) {
        String k = i.getName();
        StatusEntry e = new StatusEntry(i.getName(), i.getValue(), Clock.systemUTC().instant(), i.getState());
        m.put(k, e);
      }
    }
    return new Status(unmodifiableMap(m));
  }
}
