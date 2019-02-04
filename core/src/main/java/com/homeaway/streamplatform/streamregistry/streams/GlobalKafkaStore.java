/* Copyright (c) 2018 Expedia Group.
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
package com.homeaway.streamplatform.streamregistry.streams;

import java.util.Optional;
import java.util.Properties;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

@Slf4j
public class GlobalKafkaStore<K, V> {

    @Getter
    private final KafkaStreams streams;

    @Getter
    private final Properties streamProperties;

    @Getter
    private final String stateStoreName;

    @Getter
    private ReadOnlyKeyValueStore<K, V> view;

    private boolean isRunning = false;

    public GlobalKafkaStore(Properties streamProperties, String topicName, String stateStoreName,
                            KStreamsProcessorListener testListener) {

        this.streamProperties = streamProperties;
        this.stateStoreName = stateStoreName;

        KStreamBuilder kStreamBuilder = new KStreamBuilder();

        kStreamBuilder.globalTable(topicName, stateStoreName);

        streams = new KafkaStreams(kStreamBuilder, streamProperties);
        // [ #132 ] - Improve build times by notifying test listener that we are running
        streams.setStateListener((newState, oldState) -> {
            if (!isRunning && newState == KafkaStreams.State.RUNNING) {
                isRunning = true;
                if (testListener != null) {
                    testListener.stateStoreInitialized();
                }
            }
        });
        streams.setUncaughtExceptionHandler((t, e) -> log.error("KafkaStreams job failed", e));
        start();
    }

    private void start() {
        streams.start();
        log.info("Stream Registry KStreams started.");
        log.info("Stream Registry State Store Name: {}", stateStoreName);
        view = streams.store(stateStoreName, QueryableStoreTypes.keyValueStore());
    }

    public void stop() {
        streams.close();
        log.info("KStreams closed");
    }

    public Optional<V> getAvroStreamForKey(K key) {
        return Optional.ofNullable(view.get(key));
    }

    public KeyValueIterator<K, V> getAllValues() {
        return view.all();
    }
}
