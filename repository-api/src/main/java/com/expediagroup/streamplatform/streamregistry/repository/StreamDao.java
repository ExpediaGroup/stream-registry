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

import java.util.Optional;

import com.expediagroup.streamplatform.streamregistry.model.Stream;

//TODO Spring CrudRepository?
public interface StreamDao {
  /**
   * Insert or Update a Stream
   *
   * @param stream - Stream
   */
  void upsert(Stream stream);

  /**
   * Retrieve the Stream for a given stream name.
   *
   * @param streamName - name of the Stream.
   * @return a Pair of AvroStream Key and Value for a given Stream Name.
   */
  Optional<Stream> get(String streamName);

  /**
   * Retrieve all the Streams from the underlying data store.
   *
   * @return an Iterator of Key,Value
   */
  Iterable<Stream> getAll();

  void delete(String streamName);
}
