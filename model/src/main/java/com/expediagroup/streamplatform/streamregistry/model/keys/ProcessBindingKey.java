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
package com.expediagroup.streamplatform.streamregistry.model.keys;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcessBindingKey implements Serializable {

  private String domainName;
  private String infrastructureZone;
  private String processName;

  public DomainKey getDomainKey() {
    return new DomainKey(domainName);
  }

  public ZoneKey getZoneKey() {
    return new ZoneKey(infrastructureZone);
  }

  public ProcessKey getProcessKey() {
    return new ProcessKey(domainName, processName);
  }
}
