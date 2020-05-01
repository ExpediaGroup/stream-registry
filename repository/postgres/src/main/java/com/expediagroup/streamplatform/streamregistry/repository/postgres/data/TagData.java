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

import static org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.Cache;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name="tag")
@Cacheable
@Cache(usage = READ_WRITE)
public class TagData {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private int id;

  public TagData(String name, String value) {
    this.name = name;
    this.value = value;
  }

  private String name;
  private String value;
}
