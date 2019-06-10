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

import com.expediagroup.streamplatform.streamregistry.model.Domain;

public class AvroDomainConversionTest {
  private final AvroDomainConversion underTest = new AvroDomainConversion();

  private final Domain domain = Domain
      .builder()
      .name("name")
      .owner("owner")
      .description("description")
      .tags(Map.of("key", "value"))
      .build();

  private final AvroDomain avroDomain = AvroDomain
      .newBuilder()
      .setName("name")
      .setOwner("owner")
      .setDescription("description")
      .setTags(Map.of("key", "value"))
      .build();

  private final AvroKey avroKey = AvroKey
      .newBuilder()
      .setId("name")
      .setType(AvroKeyType.DOMAIN)
      .setParent(null)
      .build();

  @Test
  public void avroKeyfromDomain() {
    assertThat(underTest.key(domain), is(avroKey));
  }

  @Test
  public void avroKeyfromKey() {
    assertThat(underTest.key(domain.key()), is(avroKey));
  }

  @Test
  public void toAvro() {
    assertThat(underTest.toAvro(domain), is(avroDomain));
  }

  @Test
  public void toEntity() {
    assertThat(underTest.toEntity(avroDomain), is(domain));
  }

  @Test
  public void avroClass() {
    assertThat(underTest.avroClass(), is(equalTo(AvroDomain.class)));
  }

  @Test
  public void keyType() {
    assertThat(underTest.keyType(), is(AvroKeyType.DOMAIN));
  }
}
