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

import java.util.Properties;
import java.util.concurrent.CompletableFuture;

import lombok.RequiredArgsConstructor;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import io.confluent.kafka.serializers.subject.TopicRecordNameStrategy;

import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.repository.avro.AvroKey;

@RequiredArgsConstructor
public class StoreProducer implements AutoCloseable {
  private final Producer<AvroKey, SpecificRecord> producer;
  private final String topic;

  public CompletableFuture<Void> produce(AvroKey key, SpecificRecord value) {
    CompletableFuture<Void> result = new CompletableFuture<>();
    producer.send(new ProducerRecord<>(topic, key, value), (metadata, exception) -> {
      if (exception != null) {
        result.completeExceptionally(exception);
      } else {
        result.complete(null);
      }
    });
    return result;
  }

  @Override
  public void close() {
    producer.close();
  }

  @Component
  @RequiredArgsConstructor
  public static class Factory extends AbstractFactoryBean<StoreProducer> {
    private final Config config;

    @Override
    public Class<?> getObjectType() {
      return StoreProducer.class;
    }

    @Override
    protected StoreProducer createInstance() {
      Properties properties = new Properties();
      properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapServers());
      properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
      properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
      properties.put(KafkaAvroSerializerConfig.VALUE_SUBJECT_NAME_STRATEGY, TopicRecordNameStrategy.class);
      properties.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, config.getSchemaRegistryUrl());
      return new StoreProducer(new KafkaProducer<>(properties), config.getTopic());
    }
  }
}
