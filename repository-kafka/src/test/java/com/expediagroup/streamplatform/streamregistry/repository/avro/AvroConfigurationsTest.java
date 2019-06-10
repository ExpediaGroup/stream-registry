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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.junit.Test;

import com.expediagroup.streamplatform.streamregistry.model.Configuration;

public class AvroConfigurationsTest {
  private final Configuration configuration = Configuration
      .builder()
      .type("type")
      .properties(Map.of("key", "value"))
      .build();

  private final AvroConfiguration avroConfiguration = AvroConfiguration
      .newBuilder()
      .setType("type")
      .setProperties(Map.of("key", "value"))
      .build();

  @Test
  public void fromDto() {
    assertThat(AvroConfigurations.fromDto(configuration), is(avroConfiguration));
  }

  @Test
  public void toDto() {
    assertThat(AvroConfigurations.toDto(avroConfiguration), is(configuration));
  }
}
