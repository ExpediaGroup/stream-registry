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

import static com.expediagroup.streamplatform.streamregistry.core.events.ObjectNodeMapper.deserialise;

import java.util.Collections;
import java.util.function.Function;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import org.junit.Assert;
import org.junit.Test;

import com.expediagroup.streamplatform.streamregistry.avro.AvroEvent;
import com.expediagroup.streamplatform.streamregistry.avro.AvroKey;
import com.expediagroup.streamplatform.streamregistry.avro.AvroKeyType;
import com.expediagroup.streamplatform.streamregistry.model.Specification;
import com.expediagroup.streamplatform.streamregistry.model.Status;
import com.expediagroup.streamplatform.streamregistry.model.StreamBinding;
import com.expediagroup.streamplatform.streamregistry.model.Tag;
import com.expediagroup.streamplatform.streamregistry.model.keys.StreamBindingKey;

@Slf4j
public class StreamBindingNotificationEventUtilsTest {

  @Test
  public void having_a_complete_streamBinding_verify_that_is_correctly_built() {
    Function<StreamBinding, AvroKey> toKeyRecord = StreamBindingNotificationEventUtils::toAvroKeyRecord;

    Function<StreamBinding, AvroEvent> toValueRecord = StreamBindingNotificationEventUtils::toAvroValueRecord;

    val name = "name";
    val domain = "domain";
    val description = "description";
    val type = "type";
    val configJson = "{}";
    val statusJson = "{\"foo\":\"bar\"}";
    val tags = Collections.singletonList(new Tag("tag-name", "tag-value"));
    val version = 1;
    val zone = "aws_us_east_1";
    val infrastructureName = "kafka_1a";

    // Key
    val key = new StreamBindingKey();
    key.setStreamName(name);
    key.setStreamDomain(domain);
    key.setStreamVersion(version);
    key.setInfrastructureName(infrastructureName);
    key.setInfrastructureZone(zone);

    // Spec
    val spec = new Specification();
    spec.setDescription(description);
    spec.setType(type);
    spec.setConfiguration(deserialise(configJson));
    spec.setTags(tags);

    // Status
    val status = new Status(deserialise(statusJson));

    val streamBinding = new StreamBinding();
    streamBinding.setKey(key);
    streamBinding.setSpecification(spec);
    streamBinding.setStatus(status);

    AvroKey avroKey = toKeyRecord.apply(streamBinding);
    log.info("Obtained avro key {}", avroKey);

    Assert.assertNotNull("Avro key shouldn't be null", avroKey);
    Assert.assertNotNull("Key id shouldn't be null", avroKey.getId());
    Assert.assertEquals("Version should be the same as the id", String.valueOf(version), avroKey.getId());
    Assert.assertEquals("Name should be the same as the id", name, avroKey.getParent().getId());
    Assert.assertEquals("streamKey parent id should be same as domain", domain, avroKey.getParent().getParent().getId());

    Assert.assertEquals("Physical parent id should be same as infrastructureName", infrastructureName, avroKey.getPhysical().getId());
    Assert.assertEquals("infrastructure's parent id should be same as zone", zone, avroKey.getPhysical().getParent().getId());

    Assert.assertEquals("zone's parent id should be same as domain", domain, avroKey.getParent().getParent().getId());

    Assert.assertEquals("Avro key type should be STREAM_BINDING", AvroKeyType.STREAM_BINDING, avroKey.getType());

    AvroEvent avroEvent = toValueRecord.apply(streamBinding);
    log.info("Obtained avro event {}", avroEvent);

    Assert.assertNotNull("Avro event shouldn't be null", avroEvent);
    Assert.assertNotNull("StreamBinding entity shouldn't be null", avroEvent.getStreamBindingEntity());
    Assert.assertEquals(name, avroEvent.getStreamBindingEntity().getStreamName());
    Assert.assertEquals(domain, avroEvent.getStreamBindingEntity().getStreamDomain());
    Assert.assertEquals(zone, avroEvent.getStreamBindingEntity().getInfrastructureZone());
    Assert.assertEquals(infrastructureName, avroEvent.getStreamBindingEntity().getInfrastructureName());
    Assert.assertEquals(version, avroEvent.getStreamBindingEntity().getStreamVersion().intValue());

    Assert.assertEquals(description, avroEvent.getStreamBindingEntity().getDescription());
    Assert.assertEquals(type, avroEvent.getStreamBindingEntity().getType());
    Assert.assertEquals(configJson, avroEvent.getStreamBindingEntity().getConfigurationString());
    Assert.assertEquals(statusJson, avroEvent.getStreamBindingEntity().getStatusString());
  }

}
