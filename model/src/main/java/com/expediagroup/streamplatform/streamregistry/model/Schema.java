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

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
public class Schema {
  /**
   * Id of the Subject in schema-registry
   */
  @NonNull String id;

  /**
   * version of the Subject in schema-registry
   */
  int version;

  /**
   * complete schema
   */
  @NonNull String schemaString;

  /**
   * created timestamp in schema-registry
   */
  @NonNull String created;

  /**
   * updated timestamp in schema-registry
   */
  @NonNull String updated;
}
