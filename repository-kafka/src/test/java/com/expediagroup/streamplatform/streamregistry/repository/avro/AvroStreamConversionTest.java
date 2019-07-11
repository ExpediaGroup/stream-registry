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
import com.expediagroup.streamplatform.streamregistry.model.Stream;

public class AvroStreamConversionTest {
  private final AvroStreamConversion underTest = new AvroStreamConversion();

  private final Stream.Key streamKey = Stream.Key
      .builder()
      .name("name")
      .domain(Domain.Key
          .builder()
          .name("domain")
          .build())
      .version(1)
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
  public void avroKeyfromKey() {
    assertThat(underTest.key(streamKey), is(avroKey));
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
