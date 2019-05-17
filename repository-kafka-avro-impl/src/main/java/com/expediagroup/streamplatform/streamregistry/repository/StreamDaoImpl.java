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
package com.expediagroup.streamplatform.streamregistry.repository;

import java.util.Collection;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.expediagroup.streamplatform.streamregistry.AvroStream;
import com.expediagroup.streamplatform.streamregistry.AvroStreamKey;
import com.expediagroup.streamplatform.streamregistry.model.Stream;

@Slf4j
@RequiredArgsConstructor
public class StreamDaoImpl implements StreamDao {
  private final ManagedKafkaProducer producer;
  private final ManagedKStreams kStreams;

  @Override
  public void upsert(Stream stream) {
    //TODO DTO->Avro
    // kafkaProducer.log(key, stream);
  }

  @Override
  public Optional<Stream> get(String streamName) {
    AvroStreamKey key = AvroStreamKey.newBuilder().setStreamName(streamName).build();
    Optional<AvroStream> value = kStreams.getAvroStreamForKey(key);
    //TODO Avro->DTO
    return null;
  }

  @Override
  public Collection<Stream> getAll() {
    //TODO Avro->DTO
    //kStreams.getAllStreams();
    return null;
  }

  @Override
  public void delete(String streamName) {
    //TODO delete
  }
}
