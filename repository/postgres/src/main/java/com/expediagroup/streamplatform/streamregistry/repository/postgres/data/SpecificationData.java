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
package com.expediagroup.streamplatform.streamregistry.repository.postgres.data;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.OneToMany;

import lombok.Data;

@Data
@Embeddable
public class SpecificationData {

  private String description;

  @OneToMany(fetch = FetchType.EAGER, cascade = { CascadeType.ALL }, orphanRemoval = true)
  private List<TagData> tags = new ArrayList<>();

  @Column(name = "rword_type")
  private String type;

  @Lob
  @Column(name = "config_json", length = 20000)
  private String configJson;

  public SpecificationData(String description, List<TagData> tags, String type, String configJson) {
    this.description = description;
    this.tags = tags;
    this.type = type;
    this.configJson = configJson;
  }

  public SpecificationData() {}

  public List<TagData> getTags() {
    return tags == null ? Collections.emptyList() : tags;
  }

}
