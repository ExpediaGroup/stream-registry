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
package com.expediagroup.streamplatform.streamregistry.state.model.status;

import static java.util.Collections.unmodifiableList;
import static lombok.AccessLevel.PACKAGE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.val;

import com.fasterxml.jackson.databind.node.ObjectNode;

@EqualsAndHashCode
@ToString
@RequiredArgsConstructor(access = PACKAGE)
public class DefaultStatus implements Status {
  @NonNull
  private final Map<String, StatusEntry> statusMap;

  public DefaultStatus() {
    this(new HashMap<>());
  }

  @Override
  public Set<String> getNames() {
    return statusMap.keySet();
  }

  @Deprecated
  @Override
  public ObjectNode getValue(@NonNull String name) {
    StatusEntry entry = getStatusEntryByName(name);
    if (entry != null) return entry.getValue();
    return null;
  }

  @Override
  public StatusEntry getStatusEntryByName(@NonNull String name) {
    return statusMap.get(name);
  }

  @Override
  public List<StatusEntry> getEntries() {
    return unmodifiableList(new ArrayList<>(statusMap.values()));
  }

  @Override
  public DefaultStatus withAll(@NonNull List<StatusEntry> entries) {
    val statusMap = new HashMap<>(this.statusMap);
    entries.forEach(e -> statusMap.put(e.getName(), e));
    return new DefaultStatus(statusMap);
  }

  @Override
  public DefaultStatus with(@NonNull StatusEntry entry) {
    val statusMap = new HashMap<>(this.statusMap);
    statusMap.put(entry.getName(), entry);
    return new DefaultStatus(statusMap);
  }

  @Override
  public Status without(@NonNull String name) {
    val statusMap = new HashMap<>(this.statusMap);
    statusMap.remove(name);
    return new DefaultStatus(statusMap);
  }
}
