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
package com.expediagroup.streamplatform.streamregistry.repository.kafka;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.expediagroup.streamplatform.streamregistry.model.Domain;
import com.expediagroup.streamplatform.streamregistry.repository.avro.AvroDomain;
import com.expediagroup.streamplatform.streamregistry.repository.avro.AvroDomainConversion;
import com.expediagroup.streamplatform.streamregistry.repository.avro.AvroKey;
import com.expediagroup.streamplatform.streamregistry.repository.avro.AvroKeyType;
import com.expediagroup.streamplatform.streamregistry.repository.avro.AvroSchema;
import com.expediagroup.streamplatform.streamregistry.repository.avro.Conversion;

@RunWith(MockitoJUnitRunner.class)
public class KafkaRepositoryTest {
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
  private final AvroKey avroKey = AvroKey
      .newBuilder()
      .setId("name")
      .setType(AvroKeyType.DOMAIN)
      .setParent(null)
      .build();
  private final AvroSchema avroSchema = AvroSchema
      .newBuilder()
      .setName("name")
      .setOwner("owner")
      .setDescription("description")
      .setTags(Map.of("key", "value"))
      .setType("type")
      .setConfiguration(Map.of("key", "value"))
      .setDomainKey(avroKey)
      .build();
  private final AvroKey avroSchemaKey = AvroKey
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
  @Mock
  private StoreProducer producer;
  @Mock
  private StoreView view;
  private Conversion<Domain, Domain.Key, AvroDomain> conversion = new AvroDomainConversion();
  private KafkaRepository<Domain, Domain.Key, AvroDomain> underTest;

  @Before
  public void before() {
    underTest = new KafkaRepository<>(producer, view, conversion);
  }

  @Test
  public void upsert() {
    CompletableFuture<Void> future = CompletableFuture.completedFuture(null);
    when(producer.produce(any(), any())).thenReturn(future);

    underTest.upsert(domain);

    verify(producer).produce(avroKey, avroDomain);
  }

  @Test
  public void get() {
    when(view.get(avroKey)).thenReturn(Optional.of(avroDomain));

    Optional<Domain> result = underTest.get(domain.key());

    assertThat(result.get(), is(domain));
  }

  @Test
  public void stream() {
    when(view.stream()).thenReturn(Stream.of(
        Map.entry(avroKey, avroDomain),
        Map.entry(avroSchemaKey, avroSchema)
    ));

    List<Domain> result = underTest.stream().collect(Collectors.toList());

    assertThat(result.size(), is(1));
    assertThat(result.get(0), is(domain));
  }
}
