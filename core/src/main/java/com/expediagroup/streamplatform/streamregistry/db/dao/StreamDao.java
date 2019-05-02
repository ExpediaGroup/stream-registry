/* Copyright (c) 2018-2019 Expedia, Inc.
 * All rights reserved.  http://www.expediagroup.com

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *      http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.expediagroup.streamplatform.streamregistry.db.dao;

import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.streams.state.KeyValueIterator;

import com.expediagroup.streamplatform.streamregistry.AvroStream;
import com.expediagroup.streamplatform.streamregistry.AvroStreamKey;

public interface StreamDao {

    /**
     * Insert or Update a Stream
     *
     * @param key - AvroStreamKey
     * @param stream - AvroStreamValue
     */
    void upsertStream(AvroStreamKey key, AvroStream stream);

    /**
     * Retrieve the Stream for a given stream name.
     *
     * @param streamName - name of the Stream.
     * @return a Pair of AvroStream Key and Value for a given Stream Name.
     */
    Pair<AvroStreamKey, Optional<AvroStream>> getStream(String streamName);

    /**
     * Retrieve all the Streams from the underlying data store.
     *
     * @return an Iterator of Key,Value
     */
    KeyValueIterator<AvroStreamKey, AvroStream> getAllStreams();

}
