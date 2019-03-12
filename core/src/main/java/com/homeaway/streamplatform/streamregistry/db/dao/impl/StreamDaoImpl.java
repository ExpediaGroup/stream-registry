/* Copyright (c) 2018-Present Expedia Group.
 * All rights reserved.  http://www.homeaway.com

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
package com.homeaway.streamplatform.streamregistry.db.dao.impl;

import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.streams.state.KeyValueIterator;

import com.homeaway.digitalplatform.streamregistry.AvroStream;
import com.homeaway.digitalplatform.streamregistry.AvroStreamKey;
import com.homeaway.streamplatform.streamregistry.db.dao.StreamDao;
import com.homeaway.streamplatform.streamregistry.streams.ManagedKStreams;
import com.homeaway.streamplatform.streamregistry.streams.ManagedKafkaProducer;

@Slf4j
public class StreamDaoImpl implements StreamDao {

    protected final ManagedKafkaProducer kafkaProducer;
    protected final ManagedKStreams kStreams;

    public StreamDaoImpl(ManagedKafkaProducer kafkaProducer, ManagedKStreams kStreams) {
        this.kafkaProducer = kafkaProducer;
        this.kStreams = kStreams;
    }

    @Override
    public void upsertStream(AvroStreamKey key, AvroStream stream) {
        kafkaProducer.log(key, stream);
    }

    @Override
    public Pair<AvroStreamKey, Optional<AvroStream>> getStream(String streamName) {
        AvroStreamKey key = AvroStreamKey.newBuilder().setStreamName(streamName).build();
        Optional<AvroStream> value = kStreams.getAvroStreamForKey(key);
        return new ImmutablePair<>(key, value);
    }

    @Override
    public KeyValueIterator<AvroStreamKey, AvroStream> getAllStreams() {
         return kStreams.getAllStreams();
    }

}
