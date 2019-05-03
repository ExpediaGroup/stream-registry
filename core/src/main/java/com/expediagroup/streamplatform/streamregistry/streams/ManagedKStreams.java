/* Copyright (C) 2018-2019 Expedia, Inc.
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
package com.expediagroup.streamplatform.streamregistry.streams;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import com.google.common.base.Preconditions;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import io.dropwizard.lifecycle.Managed;

import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

import com.expediagroup.streamplatform.streamregistry.AvroStream;
import com.expediagroup.streamplatform.streamregistry.AvroStreamKey;
import com.expediagroup.streamplatform.streamregistry.configuration.SchemaManagerConfig;
import com.expediagroup.streamplatform.streamregistry.configuration.TopicsConfig;

@Slf4j
public class ManagedKStreams implements Managed {

    @Getter
    private final KafkaStreams streams;
    @Getter
    private final Properties streamProperties;
    @Getter
    private final TopicsConfig topicsConfig;
    private final String stateStoreName;

    private ReadOnlyKeyValueStore<AvroStreamKey, AvroStream> view;
    private boolean isRunning = false;

    public ManagedKStreams(Properties streamProperties, TopicsConfig topicsConfig, SchemaManagerConfig schemaManagerConfig) {
        this(streamProperties, topicsConfig, schemaManagerConfig, null);
    }

    public ManagedKStreams(Properties streamProperties, TopicsConfig topicsConfig, SchemaManagerConfig schemaManagerConfig, KStreamsProcessorListener testListener) {
        this.streamProperties = streamProperties;
        this.topicsConfig = topicsConfig;

        stateStoreName = topicsConfig.getStateStoreName();
        final StreamsBuilder kStreamBuilder= new StreamsBuilder();

        kStreamBuilder.globalTable(topicsConfig.getEventStoreTopic().getName(), createMaterialized(schemaManagerConfig));

        streams = new KafkaStreams(kStreamBuilder.build(), streamProperties);
        // [ #132 ] - Improve build times by notifying test listener that we are running
        streams.setStateListener((newState, oldState) -> {
            if (!isRunning && newState == KafkaStreams.State.RUNNING) {
                isRunning = true;
                if( testListener != null) {
                    testListener.stateStoreInitialized();
                }
            }
        });
        streams.setUncaughtExceptionHandler((t, e) -> log.error("KafkaStreams job failed", e));
    }

    @Override
    public void start() {
        streams.start();
        log.info("Stream Registry KStreams started.");
        log.info("Stream Registry State Store Name: {}",stateStoreName);
        view = streams.store(stateStoreName, QueryableStoreTypes.keyValueStore());
    }

    @Override
    public void stop() {
        streams.close();
        log.info("KStreams closed");
    }

    public Optional<AvroStream> getAvroStreamForKey(AvroStreamKey key){
        Preconditions.checkState(view != null, "ManagedKStreams not started. Please start before using.");
        return Optional.ofNullable(view.get(key));
    }

    public KeyValueIterator<AvroStreamKey, AvroStream> getAllStreams(){
        Preconditions.checkState(view != null, "ManagedKStreams not started. Please start before using.");
        return view.all();
    }


    private Materialized createMaterialized(SchemaManagerConfig schemaManagerConfig){
        final Map<String, String> serdeConfig =
            Collections.singletonMap(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG,
                (String) schemaManagerConfig.getProperties().get(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG));

        final SpecificAvroSerde<AvroStreamKey> keySpecificAvroSerde = new SpecificAvroSerde<>();
        keySpecificAvroSerde.configure(serdeConfig, true);

        final SpecificAvroSerde<AvroStream> valueSpecificAvroSerde = new SpecificAvroSerde<>();
        valueSpecificAvroSerde.configure(serdeConfig, false);


        return Materialized.<AvroStreamKey, AvroStream, KeyValueStore<Bytes, byte[]>>as(stateStoreName)
            .withKeySerde(keySpecificAvroSerde)
            .withValueSerde(valueSpecificAvroSerde);
    }
}
