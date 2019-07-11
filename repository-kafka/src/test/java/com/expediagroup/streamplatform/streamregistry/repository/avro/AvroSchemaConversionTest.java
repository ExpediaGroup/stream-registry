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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.expediagroup.streamplatform.streamregistry.model.Domain;
import com.expediagroup.streamplatform.streamregistry.model.Schema;

public class AvroSchemaConversionTest {
  private final AvroSchemaConversion underTest = new AvroSchemaConversion();

  private final Schema.Key schemaKey = Schema.Key
      .builder()
      .name("name")
      .domain(Domain.Key
          .builder()
          .name("domain")
          .build())
      .build();

  private final AvroKey avroKey = AvroKey
      .newBuilder()
      .setId("name")
      .setType(AvroKeyType.SCHEMA)
      .setParent(AvroKey
          .newBuilder()
          .setId("domain")
          .setType(AvroKeyType.DOMAIN)
          .setParent(null)
          .build())
      .build();

  @Test
  public void avroKeyfromKey() {
    assertThat(underTest.key(schemaKey), is(avroKey));
  }

  @Test
  public void avroClass() {
    assertThat(underTest.avroClass(), is(equalTo(AvroSchema.class)));
  }

  @Test
  public void keyType() {
    assertThat(underTest.keyType(), is(AvroKeyType.SCHEMA));
  }
}
