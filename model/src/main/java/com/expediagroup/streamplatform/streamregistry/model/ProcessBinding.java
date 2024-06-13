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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.expediagroup.streamplatform.streamregistry.model.keys.ProcessBindingKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.ZoneKey;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcessBinding implements Entity<ProcessBindingKey> {
  private ProcessBindingKey key;
  private Specification specification;
  private ZoneKey zone;
  private List<ProcessInputStreamBinding> inputs;
  private List<ProcessOutputStreamBinding> outputs;
  private Status status;

  public ProcessBinding(
    ProcessBindingKey key,
    Specification specification,
    ZoneKey zone,
    List<ProcessInputStreamBinding> inputs,
    List<ProcessOutputStreamBinding> outputs
  ) {
    this.key = key;
    this.specification = specification;
    this.zone = zone;
    this.inputs = inputs;
    this.outputs = outputs;
    this.status = new Status();
  }
}
