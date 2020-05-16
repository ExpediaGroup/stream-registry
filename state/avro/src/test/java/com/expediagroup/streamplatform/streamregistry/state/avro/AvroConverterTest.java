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
package com.expediagroup.streamplatform.streamregistry.state.avro;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Test;

import com.expediagroup.streamplatform.streamregistry.state.model.event.Event;

public class AvroConverterTest {
  private final ObjectMapper mapper = new ObjectMapper();

  private final AvroConverter underTest = new AvroConverter();

  AvroDomainKey domainKey = new AvroDomainKey("domain");
  AvroSchemaKey schemaKey = new AvroSchemaKey(domainKey, "schema");
  AvroStreamKey streamKey = new AvroStreamKey(domainKey, "stream", 1);
  AvroZoneKey zoneKey = new AvroZoneKey("zone");
  AvroInfrastructureKey infrastructureKey = new AvroInfrastructureKey(zoneKey, "infrastructure");
  AvroProducerKey producerKey = new AvroProducerKey(streamKey, zoneKey, "producer");
  AvroConsumerKey consumerKey = new AvroConsumerKey(streamKey, zoneKey, "consumer");
  AvroStreamBindingKey streamBindingKey = new AvroStreamBindingKey(streamKey, infrastructureKey);
  AvroProducerBindingKey producerBindingKey = new AvroProducerBindingKey(producerKey, streamBindingKey);
  AvroConsumerBindingKey consumerBindingKey = new AvroConsumerBindingKey(consumerKey, streamBindingKey);
  AvroObject object = new AvroObject(Map.of("foo", "bar"));

  @Test
  public void test() throws IOException {

    Event event = underTest.toModel(
        new AvroKey(new AvroStatusKey(producerBindingKey, "status")),
        new AvroValue(new AvroStatus(object))
    );
    System.out.println(event);
    AvroEvent avroEvent = underTest.toAvro(event);
    System.out.println(avroEvent);
  }

}
