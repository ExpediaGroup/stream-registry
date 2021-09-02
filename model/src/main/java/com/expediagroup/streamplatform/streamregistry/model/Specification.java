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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.databind.node.ObjectNode;

@Data
@NoArgsConstructor
public class Specification {

  private String description;
  private List<Tag> tags = new ArrayList<>();
  private String type;
  private ObjectNode configuration;
  private List<Security> security = new ArrayList<>();
  private String function = "";

  public Specification(String description, List<Tag> tags, String type, ObjectNode configuration, List<Security> security, String function) {
    this.description = description;
    this.tags = tags;
    this.type = type;
    this.configuration = configuration;
    this.security = security;
    this.function = function;
  }

  public List<Tag> getTags() {
    return tags == null ? Collections.emptyList() : tags;
  }
}
