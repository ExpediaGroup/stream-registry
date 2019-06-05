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

import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.apache.kafka.streams.state.RocksDBConfigSetter;
import org.rocksdb.BlockBasedTableConfig;
import org.rocksdb.Options;
import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.AvroStream;
import com.expediagroup.streamplatform.streamregistry.AvroStreamKey;

@Slf4j
@RequiredArgsConstructor
public class ManagedKStreams implements AutoCloseable {
  private final KafkaStreams streams;
  private final ReadOnlyKeyValueStore<AvroStreamKey, AvroStream> view;

  @Override
  public void close() {
    streams.close();
  }

  public Optional<AvroStream> getAvroStreamForKey(AvroStreamKey key) {
    return Optional.ofNullable(view.get(key));
  }

  public KeyValueIterator<AvroStreamKey, AvroStream> getAllStreams() {
    return view.all();
  }

  @Component
  public static class Factory {
    protected ManagedKStreams create(
        String bootstrapServers,
        String topic) {
      Properties properties = new Properties();
      properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
      properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, SpecificAvroSerde.class);
      properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde.class);
      properties.put(StreamsConfig.ROCKSDB_CONFIG_SETTER_CLASS_CONFIG, CustomRocksDBConfig.class);

      StreamsBuilder builder = new StreamsBuilder();
      GlobalKTable<Object, Object> table = builder.globalTable(topic);
      KafkaStreams streams = new KafkaStreams(builder.build(), properties);
      streams.setUncaughtExceptionHandler((t, e) -> log.error("KafkaStreams job failed", e));
      streams.start();
      log.info("Stream Registry State StoreApi Name: {}", table.queryableStoreName());

      ReadOnlyKeyValueStore<AvroStreamKey, AvroStream> view = streams.store(table.queryableStoreName(), QueryableStoreTypes.keyValueStore());

      return new ManagedKStreams(streams, view);
    }
  }

  public static class CustomRocksDBConfig implements RocksDBConfigSetter {
    @Override
    public void setConfig(String storeName, Options options, Map<String, Object> configs) {
      BlockBasedTableConfig tableConfig = new org.rocksdb.BlockBasedTableConfig();
      tableConfig.setBlockCacheSize(2 * 1024 * 1024L);
      tableConfig.setBlockSize(2 * 1024L);
      tableConfig.setCacheIndexAndFilterBlocks(true);
      options.setTableFormatConfig(tableConfig);
      options.setMaxWriteBufferNumber(2);
      options.optimizeFiltersForHits();
    }
  }
}
