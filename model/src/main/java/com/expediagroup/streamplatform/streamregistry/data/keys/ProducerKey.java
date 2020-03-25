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
package com.expediagroup.streamplatform.streamregistry.data.keys;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class ProducerKey implements Serializable {

  @Column(length = 100)
  private String streamDomain;
  @Column(length = 100)
  private String streamName;
  @Column(length = 100)
  private Integer streamVersion;
  @Column(length = 100)
  private String zone;
  @Column(length = 100)
  private String name;

  public StreamKey getStreamKey() {
    return new StreamKey(streamDomain, streamName, streamVersion);
  }

  public InfrastructureKey getInfrastructureKey() {
    return new InfrastructureKey(zone, name);
  }

  public ZoneKey getZoneKey() {
    return new ZoneKey(zone);
  }
}