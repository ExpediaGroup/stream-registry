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

import java.util.Properties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.AvroStream;
import com.expediagroup.streamplatform.streamregistry.AvroStreamKey;

@Slf4j
@RequiredArgsConstructor
public class ManagedKafkaProducer implements AutoCloseable {
  private final Producer<AvroStreamKey, AvroStream> producer;
  private final String topic;

  @Override
  public void close() {
    producer.close();
    log.info("Manager Kafka Producer stopped.");
  }

  public void log(AvroStreamKey key, AvroStream value) {
    try {
      producer
          .send(new ProducerRecord<>(topic, key, value))
          .get();
    } catch (Exception e) {
      throw new IllegalStateException("Could not log key=" + key + " value=" + value + " to kafka", e);
    }
    log.info("Message pushed to the sourceKStreamProcessorTopic Topic={} with key={} successfully",
        topic, String.valueOf(key));
  }

  @Component
  public static class Factory {
    public ManagedKafkaProducer create(
        String bootstrapServers,
        String topic,
        String schemaRegistryUrl) {
      Properties properties = new Properties();
      properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
      properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
      properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
      properties.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
      return new ManagedKafkaProducer(new KafkaProducer<>(properties), topic);
    }
  }
}
