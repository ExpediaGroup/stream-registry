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
package com.expediagroup.streamplatform.streamregistry.model;

import java.util.Map;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Domain extends Entity<Domain.Key> {
  @Builder
  private Domain(
      String name,
      String owner,
      String description,
      Map<String, String> tags,
      String type,
      Map<String, String> configuration) {
    super(name, owner, description, tags, type, configuration);
  }

  @Override
  public Key key() {
    return new Key(getName());
  }

  @Value
  @Builder
  public static class Key {
    String name;
  }
}
