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
package com.expediagroup.streamplatform.streamregistry.data;

import static com.expediagroup.streamplatform.streamregistry.model.ObjectNodeMapper.deserialise;
import static com.expediagroup.streamplatform.streamregistry.model.ObjectNodeMapper.serialise;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Lob;

import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.Data;

@Data
@Embeddable
public class Status {

  @Lob
  @Column(name = "statusJson", length = 20000)
  private String statusJson;

  public Status() {}

  public Status(String statusJson) {
    this.statusJson = statusJson;
  }

  public ObjectNode getAgentStatus() {
    return deserialise(getStatusJson());
  }

  public void setAgentStatus(ObjectNode configuration) {
    statusJson = serialise(configuration);
  }
}