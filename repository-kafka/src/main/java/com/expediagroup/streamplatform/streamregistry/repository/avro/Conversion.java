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

import org.apache.avro.specific.SpecificRecord;

import com.expediagroup.streamplatform.streamregistry.model.Entity;

public interface Conversion<E extends Entity<K>, K, A extends SpecificRecord> {
  AvroKey key(K key);

  Class<A> avroClass();

  Class<E> entityClass();

  AvroKeyType keyType();
}
