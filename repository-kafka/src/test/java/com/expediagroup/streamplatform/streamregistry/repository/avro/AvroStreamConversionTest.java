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

import java.util.Map;

import org.junit.Test;

import com.expediagroup.streamplatform.streamregistry.model.NameDomain;
import com.expediagroup.streamplatform.streamregistry.model.Stream;

public class AvroStreamConversionTest {
  private final AvroStreamConversion underTest = new AvroStreamConversion();

  private final Stream stream = Stream
      .builder()
      .name("name")
      .owner("owner")
      .description("description")
      .tags(Map.of("key", "value"))
      .type("type")
      .configuration(Map.of("key", "value"))
      .domain("domain")
      .version(1)
      .schema(NameDomain
          .builder()
          .name("schemaName")
          .domain("schemaDomain")
          .build())
      .build();

  private final AvroStream avroStream = AvroStream
      .newBuilder()
      .setName("name")
      .setOwner("owner")
      .setDescription("description")
      .setTags(Map.of("key", "value"))
      .setType("type")
      .setConfiguration(Map.of("key", "value"))
      .setDomain("domain")
      .setVersion(1)
      .setSchemaRef(AvroNameDomain
          .newBuilder()
          .setName("schemaName")
          .setDomain("schemaDomain")
          .build())
      .build();

  private final AvroKey avroKey = AvroKey
      .newBuilder()
      .setId("1")
      .setType(AvroKeyType.STREAM_VERSION)
      .setParent(AvroKey
          .newBuilder()
          .setId("name")
          .setType(AvroKeyType.STREAM)
          .setParent(AvroKey
              .newBuilder()
              .setId("domain")
              .setType(AvroKeyType.DOMAIN)
              .setParent(null)
              .build())
          .build())
      .build();

  @Test
  public void avroKeyfromDomain() {
    assertThat(underTest.key(stream), is(avroKey));
  }

  @Test
  public void avroKeyfromKey() {
    assertThat(underTest.key(stream.key()), is(avroKey));
  }

  @Test
  public void toAvro() {
    assertThat(underTest.toAvro(stream), is(avroStream));
  }

  @Test
  public void toEntity() {
    assertThat(underTest.toEntity(avroStream), is(stream));
  }

  @Test
  public void avroClass() {
    assertThat(underTest.avroClass(), is(equalTo(AvroStream.class)));
  }

  @Test
  public void keyType() {
    assertThat(underTest.keyType(), is(AvroKeyType.STREAM_VERSION));
  }
}
