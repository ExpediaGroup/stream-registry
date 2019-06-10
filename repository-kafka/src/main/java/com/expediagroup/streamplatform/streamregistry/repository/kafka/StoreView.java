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

import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.serializers.subject.TopicRecordNameStrategy;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.apache.kafka.streams.state.RocksDBConfigSetter;
import org.rocksdb.BlockBasedTableConfig;
import org.rocksdb.Options;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.repository.avro.AvroKey;

@Slf4j
@RequiredArgsConstructor
public class StoreView implements AutoCloseable {
  private final KafkaStreams streams;
  private final ReadOnlyKeyValueStore<AvroKey, SpecificRecord> view;

  public Optional<SpecificRecord> get(AvroKey key) {
    return Optional.ofNullable(view.get(key));
  }

  public Stream<Entry<AvroKey, SpecificRecord>> stream() {
    return StreamSupport
        .stream(spliteratorUnknownSize(view.all(), ORDERED), false)
        .map(x -> Map.entry(x.key, x.value));
  }

  @Override
  public void close() {
    streams.close();
  }

  @Component
  @RequiredArgsConstructor
  public static class Factory extends AbstractFactoryBean<StoreView> {
    private final Config config;

    @Override
    public Class<?> getObjectType() {
      return StoreView.class;
    }

    @Override
    protected StoreView createInstance() throws Exception {
      Properties properties = new Properties();
      properties.put(StreamsConfig.APPLICATION_ID_CONFIG, UUID.randomUUID().toString());
      properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapServers());
      properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, SpecificAvroSerde.class);
      properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde.class);
      properties.put(StreamsConfig.ROCKSDB_CONFIG_SETTER_CLASS_CONFIG, CustomRocksDBConfig.class);
      properties.put(AbstractKafkaAvroSerDeConfig.VALUE_SUBJECT_NAME_STRATEGY, TopicRecordNameStrategy.class);
      properties.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, config.getSchemaRegistryUrl());

      StreamsBuilder builder = new StreamsBuilder();
      GlobalKTable<Object, Object> table = builder.globalTable(config.getTopic(), Materialized
          .<Object, Object, KeyValueStore<Bytes, byte[]>>as(config.getTopic())
          .withLoggingDisabled()
          .withCachingDisabled());
      KafkaStreams streams = new KafkaStreams(builder.build(), properties);
      streams.setUncaughtExceptionHandler((t, e) -> log.error("KafkaStreams job failed", e));
      streams.start();
      ReadOnlyKeyValueStore<AvroKey, SpecificRecord> view = streams.store(table.queryableStoreName(), QueryableStoreTypes.keyValueStore());
      return new StoreView(streams, view);
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
