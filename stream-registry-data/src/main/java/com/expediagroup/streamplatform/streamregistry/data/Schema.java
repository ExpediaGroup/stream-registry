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
package com.expediagroup.streamplatform.streamregistry.data;

import static org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE;

import javax.persistence.Cacheable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.Cache;

import com.expediagroup.streamplatform.streamregistry.data.keys.SchemaKey;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity(name = "rword_schema")
@Cacheable
@Cache(usage = READ_WRITE)
public class Schema implements ManagedType {

  @EmbeddedId
  private SchemaKey key;
  private Specification specification;
  private Status status;
}
