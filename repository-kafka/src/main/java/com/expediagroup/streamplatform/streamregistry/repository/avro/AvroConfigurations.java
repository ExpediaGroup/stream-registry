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
package com.expediagroup.streamplatform.streamregistry.repository.avro;

import com.expediagroup.streamplatform.streamregistry.model.Configuration;

public final class AvroConfigurations {
  private AvroConfigurations() {}

  public static AvroConfiguration fromDto(Configuration configuration) {
    return AvroConfiguration
        .newBuilder()
        .setType(configuration.getType())
        .setProperties(configuration.getProperties())
        .build();
  }

  public static Configuration toDto(AvroConfiguration configuration) {
    return Configuration
        .builder()
        .type(configuration.getType().toString())
        .properties(configuration.getProperties())
        .build();
  }
}
