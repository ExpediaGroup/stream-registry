/**
 * Copyright (C) 2018-2020 Expedia, Inc.
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
package com.expediagroup.streamplatform.streamregistry.core.events;

import static java.lang.String.valueOf;

import java.util.Collections;
import java.util.function.Function;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import org.junit.Assert;
import org.junit.Test;

import com.expediagroup.streamplatform.streamregistry.avro.AvroEvent;
import com.expediagroup.streamplatform.streamregistry.avro.AvroKey;
import com.expediagroup.streamplatform.streamregistry.model.Consumer;
import com.expediagroup.streamplatform.streamregistry.model.Specification;
import com.expediagroup.streamplatform.streamregistry.model.Status;
import com.expediagroup.streamplatform.streamregistry.model.Tag;
import com.expediagroup.streamplatform.streamregistry.model.keys.ConsumerKey;

@Slf4j
public class ConsumerNotificationEventUtilsTest {

  @Test
  public void having_a_complete_consumer_verify_that_is_correctly_built() {
    Function<Consumer, AvroKey> toKeyRecord = ConsumerNotificationEventUtils::toAvroKeyRecord;

    Function<Consumer, AvroEvent> toValueRecord = ConsumerNotificationEventUtils::toAvroValueRecord;

    val consumerName = "consumer1";
    val streamName = "name";
    val domain = "domain";
    val description = "description";
    val type = "type";
    val configJson = "{}";
    val statusJson = "{foo:bar}";
    val tags = Collections.singletonList(new Tag("tag-name", "tag-value"));
    val version = 1;
    val zone = "aws_us_east_1";

    // Key
    val key = new ConsumerKey();
    key.setName(consumerName);
    key.setStreamName(streamName);
    key.setStreamDomain(domain);
    key.setStreamVersion(version);
    key.setZone(zone);

    // Spec
    val spec = new Specification();
    spec.setDescription(description);
    spec.setType(type);
    spec.setConfigJson(configJson);
    spec.setTags(tags);

    // Status
    val status = new Status();
    status.setStatusJson(statusJson);

    val consumer = new Consumer();
    consumer.setKey(key);
    consumer.setSpecification(spec);
    consumer.setStatus(status);

    AvroKey avroKey = toKeyRecord.apply(consumer);
    log.info("Obtained avro key {}", avroKey);

    Assert.assertNotNull("Avro key shouldn't be null", avroKey);
    Assert.assertNotNull("Key id shouldn't be null", avroKey.getId());
    Assert.assertEquals("key's is should be the same as consumerName", consumerName, avroKey.getId());
    Assert.assertEquals("key's parent should be the same as version", valueOf(version), avroKey.getParent().getId());
    Assert.assertEquals("versionKey's parent should be the same as streamName", streamName, avroKey.getParent().getParent().getId());

    AvroEvent avroEvent = toValueRecord.apply(consumer);
    log.info("Obtained avro event {}", avroEvent);

    Assert.assertNotNull("Avro event shouldn't be null", avroEvent);
    Assert.assertNotNull("consumer entity shouldn't be null", avroEvent.getConsumerEntity());
    Assert.assertEquals(consumerName, avroEvent.getConsumerEntity().getName());
    Assert.assertEquals(streamName, avroEvent.getConsumerEntity().getStreamName());
    Assert.assertEquals(domain, avroEvent.getConsumerEntity().getStreamDomain());
    Assert.assertEquals(zone, avroEvent.getConsumerEntity().getZone());
    Assert.assertEquals(description, avroEvent.getConsumerEntity().getDescription());
    Assert.assertEquals(type, avroEvent.getConsumerEntity().getType());
    Assert.assertEquals(configJson, avroEvent.getConsumerEntity().getConfigurationString());
    Assert.assertEquals(statusJson, avroEvent.getConsumerEntity().getStatusString());
    Assert.assertEquals(version, avroEvent.getConsumerEntity().getStreamVersion().intValue());
  }

}