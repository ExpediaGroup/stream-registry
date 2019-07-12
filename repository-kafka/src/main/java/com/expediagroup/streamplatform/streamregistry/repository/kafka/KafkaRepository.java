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

import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.avro.specific.SpecificRecord;

import com.expediagroup.streamplatform.streamregistry.model.Entity;
import com.expediagroup.streamplatform.streamregistry.repository.Repository;
import com.expediagroup.streamplatform.streamregistry.repository.avro.AvroTransformer;
import com.expediagroup.streamplatform.streamregistry.repository.avro.Conversion;

public class KafkaRepository<E extends Entity<K>, K, A extends SpecificRecord> implements Repository<E, K> {
  private final StoreProducer producer;
  private final StoreView view;
  private final Conversion<E, K, A> conversion;
  private final AvroTransformer transformer;

  public KafkaRepository(StoreProducer producer, StoreView view, Conversion<E, K, A> conversion) {
    this.producer = producer;
    this.view = view;
    this.conversion = conversion;
    this.transformer = new AvroTransformer();
  }

  @Override
  public void upsert(E entity) {
    //TODO Introduce blocking to allow read-after-write. Blocking to compare equality after reading the record
    // back won't work in KStreams as records are mutable and there's a race with other potential writes.
    // Data Highway's KafkaStore uses Kafka Connect's KafkaBasedLog which has the ability to sync (wait).
    producer
        .produce(conversion.key(entity.key()), transformer.transform(entity, conversion.avroClass()))
        .join();
  }

  @Override
  public Optional<E> get(K key) {
    return view
        .get(conversion.key(key))
        .map(avro -> transformer.transform(avro, conversion.entityClass()));
  }

  @Override
  public Stream<E> stream() {
    return view
        .stream()
        .filter(e -> e.getKey().getType() == conversion.keyType())
        .map(Entry::getValue)
        .map(avro -> transformer.transform(avro, conversion.entityClass()));
  }
}
