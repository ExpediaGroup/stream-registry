/**
 * Copyright (C) 2018-2024 Expedia, Inc.
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

import java.util.List;

import com.expediagroup.streamplatform.streamregistry.model.keys.ProcessKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.ZoneKey;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Process implements Entity<ProcessKey> {
  private ProcessKey key;
  private Specification specification;
  private List<ZoneKey> zones;
  private List<ProcessInputStream> inputs;
  private List<ProcessOutputStream> outputs;
  private Status status;

  public Process(
    ProcessKey key,
    Specification specification,
    List<ZoneKey> zones,
    List<ProcessInputStream> inputs,
    List<ProcessOutputStream> outputs
  ) {
    this.key = key;
    this.specification = specification;
    this.zones = zones;
    this.inputs = inputs;
    this.outputs = outputs;
    this.status = new Status();
  }
}
