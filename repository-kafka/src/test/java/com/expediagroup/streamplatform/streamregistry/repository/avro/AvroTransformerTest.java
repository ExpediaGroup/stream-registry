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

import com.expediagroup.streamplatform.streamregistry.model.Domain;
import com.expediagroup.streamplatform.streamregistry.model.Schema;
import com.expediagroup.streamplatform.streamregistry.model.Stream;

public class AvroTransformerTest {
  private final Domain domain = Domain
      .builder()
      .name("name")
      .owner("owner")
      .description("description")
      .tags(Map.of("key", "value"))
      .type("type")
      .configuration(Map.of("key", "value"))
      .build();

  private final AvroDomain avroDomain = AvroDomain
      .newBuilder()
      .setName("name")
      .setOwner("owner")
      .setDescription("description")
      .setTags(Map.of("key", "value"))
      .setType("type")
      .setConfiguration(Map.of("key", "value"))
      .build();

  private final Schema schema = Schema
      .builder()
      .name("name")
      .owner("owner")
      .description("description")
      .tags(Map.of("key", "value"))
      .type("type")
      .configuration(Map.of("key", "value"))
      .domain(Domain.Key
          .builder()
          .name("domain")
          .build())
      .build();

  private final AvroSchema avroSchema = AvroSchema
      .newBuilder()
      .setName("name")
      .setOwner("owner")
      .setDescription("description")
      .setTags(Map.of("key", "value"))
      .setType("type")
      .setConfiguration(Map.of("key", "value"))
      .setDomainKey(AvroKey
          .newBuilder()
          .setId("domain")
          .setType(AvroKeyType.DOMAIN)
          .setParent(null)
          .build())
      .build();

  private final Stream stream = Stream
      .builder()
      .name("name")
      .owner("owner")
      .description("description")
      .tags(Map.of("key", "value"))
      .type("type")
      .configuration(Map.of("key", "value"))
      .domain(Domain.Key
          .builder()
          .name("domain")
          .build())
      .version(1)
      .schema(Schema.Key
          .builder()
          .name("schemaName")
          .domain(Domain.Key
              .builder()
              .name("schemaDomain")
              .build())
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
      .setDomainKey(AvroKey
          .newBuilder()
          .setId("domain")
          .setType(AvroKeyType.DOMAIN)
          .setParent(null)
          .build())
      .setVersion(1)
      .setSchemaKey(AvroKey
          .newBuilder()
          .setId("schemaName")
          .setType(AvroKeyType.SCHEMA)
          .setParent(AvroKey
              .newBuilder()
              .setId("schemaDomain")
              .setType(AvroKeyType.DOMAIN)
              .setParent(null)
              .build())
          .build())
      .build();

  private final AvroTransformer underTest = new AvroTransformer();

  @Test
  public void toAvroDomain() {
    assertThat(underTest.transform(domain, AvroDomain.class), is(avroDomain));
  }

  @Test
  public void toEntityDomain() {
    assertThat(underTest.transform(avroDomain, Domain.class), is(domain));
  }

  @Test
  public void toAvroSchema() {
    assertThat(underTest.transform(schema, AvroSchema.class), is(avroSchema));
  }

  @Test
  public void toEntitySchema() {
    assertThat(underTest.transform(avroSchema, Schema.class), is(schema));
  }

  @Test
  public void toAvroStream() {
    assertThat(underTest.transform(stream, AvroStream.class), is(avroStream));
  }

  @Test
  public void toEntityStream() {
    assertThat(underTest.transform(avroStream, Stream.class), is(stream));
  }
}
