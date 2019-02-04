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

import java.util.Properties;
import java.util.concurrent.Future;

import lombok.extern.slf4j.Slf4j;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

@Slf4j
public class StreamRegistryProducer<K, V> implements StreamProducer<K, V> {

    private final String topicName;
    private Producer<K, V> producer;

    public StreamRegistryProducer(Properties properties, String topicName) {
        this.topicName = topicName;
        producer = new KafkaProducer<>(properties);
        log.info("Managed Kafka Producer Started with properties: " + properties);
    }

    public void stop() {
        producer.close();
        log.info("Manager Kafka Producer stopped.");
    }

    @Override
    public void log(K key, V value) {
        try {
            Future<RecordMetadata> result = producer.send(new ProducerRecord<>(topicName, key, value),
                    (RecordMetadata recordMetadata, Exception e) -> {
                        if (e != null) {
                            log.error("Error producing to topic={}", recordMetadata.topic(), e);
                        }
                    });
            // synchronously wait for the response.
            result.get();
        } catch (Exception exception) {
            throw new IllegalStateException("Could not log key=" + key + " value=" + value + " to kafka",
                    exception);
        }
        log.info("Message pushed to the sourceKStreamProcessorTopic Topic={} with key={} successfully",
                topicName, key.toString());

    }
}
