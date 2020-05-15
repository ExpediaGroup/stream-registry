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
package com.expediagroup.streamplatform.streamregistry.state.model.status;

import static lombok.AccessLevel.PACKAGE;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import com.fasterxml.jackson.databind.node.ObjectNode;

@RequiredArgsConstructor(access = PACKAGE)
public class DefaultStatus implements Status {
  @NonNull
  private final Map<String, ObjectNode> statusMap;

  public DefaultStatus() {
    this(new HashMap<>());
  }

  @Override
  public Set<String> getNames() {
    return statusMap.keySet();
  }

  @Override
  public ObjectNode getValue(@NonNull String name) {
    return statusMap.get(name);
  }

  @Override
  public List<StatusEntry> getEntries() {
    return statusMap
        .entrySet().stream()
        .map(e -> new StatusEntry(e.getKey(), e.getValue()))
        .collect(Collectors.toList());
  }

  @Override
  public Status with(@NonNull StatusEntry entry) {
    Map<String, ObjectNode> statusMap = new HashMap<>(this.statusMap);
    statusMap.put(entry.getName(), entry.getValue());
    return new DefaultStatus(statusMap);
  }
}
