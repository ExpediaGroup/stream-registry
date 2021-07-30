/**
 * Copyright (C) 2018-2021 Expedia, Inc.
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

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import lombok.val;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Test;

import com.expediagroup.streamplatform.streamregistry.state.model.Entity;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.ConsumerBindingKey;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.ConsumerKey;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.DomainKey;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.InfrastructureKey;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.ProcessBindingKey;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.ProcessKey;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.ProducerBindingKey;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.ProducerKey;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.StreamBindingKey;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.StreamKey;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.ZoneKey;
import com.expediagroup.streamplatform.streamregistry.state.model.event.Event;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.*;
import com.expediagroup.streamplatform.streamregistry.state.model.status.StatusEntry;

public class AvroConverterTest {
  private final ObjectMapper mapper = new ObjectMapper();

  private final AvroConverter underTest = new AvroConverter();

  private final AvroDomainKey avroDomainKey = new AvroDomainKey("domain");
  private final AvroStreamKey avroStreamKey = new AvroStreamKey(avroDomainKey, "stream", 1);
  private final AvroZoneKey avroZoneKey = new AvroZoneKey("zone");
  private final AvroInfrastructureKey avroInfrastructureKey = new AvroInfrastructureKey(avroZoneKey, "infrastructure");
  private final AvroConsumerKey avroConsumerKey = new AvroConsumerKey(avroStreamKey, avroZoneKey, "consumer");
  private final AvroProducerKey avroProducerKey = new AvroProducerKey(avroStreamKey, avroZoneKey, "producer");
  private final AvroProcessKey avroProcessKey = new AvroProcessKey(avroDomainKey, "process");
  private final AvroStreamBindingKey avroStreamBindingKey = new AvroStreamBindingKey(avroStreamKey, avroInfrastructureKey);
  private final AvroConsumerBindingKey avroConsumerBindingKey = new AvroConsumerBindingKey(avroConsumerKey, avroStreamBindingKey);
  private final AvroProducerBindingKey avroProducerBindingKey = new AvroProducerBindingKey(avroProducerKey, avroStreamBindingKey);
  private final AvroProcessBindingKey avroProcessBindingKey = new AvroProcessBindingKey(avroProcessKey, avroZoneKey);

  private final AvroSpecificationKey avroSpecificationKey = new AvroSpecificationKey(avroDomainKey);
  private final AvroSpecification avroSpecification = new AvroSpecification(
      "description",
      singletonList(new AvroTag("name", "value")),
      "type",
      new AvroObject(Collections.singletonMap("foo", "bar")),
      new HashMap<String, List<AvroPrincipal>>() {{
        put("admin", singletonList(new AvroPrincipal("user1")));
        put("creator", asList(new AvroPrincipal("user2"), new AvroPrincipal("user3")));
      }}
  );
  private final AvroStreamSpecification avroStreamSpecification = new AvroStreamSpecification(
      "description",
      singletonList(new AvroTag("name", "value")),
      "type",
      new AvroObject(Collections.singletonMap("foo", "bar")),
      new HashMap<String, List<AvroPrincipal>>() {{
        put("admin", singletonList(new AvroPrincipal("user1")));
        put("creator", asList(new AvroPrincipal("user2"), new AvroPrincipal("user3")));
      }},
      new AvroSchemaKey(avroDomainKey, "schema")
  );
  private final AvroProcessSpecification avroProcessSpecification = new AvroProcessSpecification(
    singletonList(avroZoneKey),
    "description",
    singletonList(new AvroTag("name", "value")),
    "type",
    new AvroObject(Collections.singletonMap("foo", "bar")),
    new HashMap<String, List<AvroPrincipal>>() {{
      put("admin", singletonList(new AvroPrincipal("user1")));
      put("creator", asList(new AvroPrincipal("user2"), new AvroPrincipal("user3")));
    }},
    singletonList(new AvroProcessInputStream(avroStreamKey)),
    singletonList(new AvroProcessOutputStream(avroStreamKey))
  );
  private final AvroProcessBindingSpecification avroProcessBindingSpecification = new AvroProcessBindingSpecification(
    avroZoneKey,
    "description",
    singletonList(new AvroTag("name", "value")),
    "type",
    new AvroObject(Collections.singletonMap("foo", "bar")),
    new HashMap<String, List<AvroPrincipal>>() {{
      put("admin", singletonList(new AvroPrincipal("user1")));
      put("creator", asList(new AvroPrincipal("user2"), new AvroPrincipal("user3")));
    }},
    singletonList(avroConsumerBindingKey),
    singletonList(avroProducerBindingKey)
  );

  private final AvroStatusKey avroStatusKey = new AvroStatusKey(avroDomainKey, "statusName");
  private final AvroObject avroObjectStatus = new AvroObject(Collections.singletonMap("foo", "baz"));
  private final AvroStatus avroStatus = new AvroStatus(avroObjectStatus);

  private final AvroEvent avroSpecificationEvent = new AvroEvent(new AvroKey(avroSpecificationKey), new AvroValue(avroSpecification));
  private final AvroEvent avroStreamSpecificationEvent = new AvroEvent(new AvroKey(new AvroSpecificationKey(avroStreamKey)), new AvroValue(avroStreamSpecification));
  private final AvroEvent avroProcessSpecificationEvent = new AvroEvent(new AvroKey(new AvroSpecificationKey(avroProcessKey)), new AvroValue(avroProcessSpecification));
  private final AvroEvent avroProcessBindingSpecificationEvent = new AvroEvent(new AvroKey(new AvroSpecificationKey(avroProcessBindingKey)), new AvroValue(avroProcessBindingSpecification));
  private final AvroEvent avroStatusEvent = new AvroEvent(new AvroKey(avroStatusKey), new AvroValue(avroStatus));
  private final AvroEvent avroSpecificationDeletionEvent = new AvroEvent(new AvroKey(avroSpecificationKey), null);
  private final AvroEvent avroStatusDeletionEvent = new AvroEvent(new AvroKey(avroStatusKey), null);

  private final DomainKey domainKey = new DomainKey("domain");
  private final ZoneKey zoneKey = new ZoneKey("zone");
  private final InfrastructureKey infrastructureKey = new InfrastructureKey(zoneKey, "infrastructure");
  private final DefaultSpecification specification = new DefaultSpecification(
      "description",
      singletonList(new Tag("name", "value")),
      "type",
      mapper.createObjectNode().put("foo", "bar"),
      new HashMap<String, List<Principal>>() {{
        put("admin", singletonList(new Principal("user1")));
        put("creator", asList(new Principal("user2"), new Principal("user3")));
      }}
  );
  private final StatusEntry statusEntry = new StatusEntry("statusName", mapper.createObjectNode().put("foo", "baz"));
  private final StreamKey streamKey = new StreamKey(domainKey, "stream", 1);
  private final StreamBindingKey streamBindingKey = new StreamBindingKey(streamKey, infrastructureKey);
  private final StreamSpecification streamSpecification = new StreamSpecification(
      "description",
      singletonList(new Tag("name", "value")),
      "type",
      mapper.createObjectNode().put("foo", "bar"),
      new HashMap<String, List<Principal>>() {{
        put("admin", singletonList(new Principal("user1")));
        put("creator", asList(new Principal("user2"), new Principal("user3")));
      }},
      new Entity.SchemaKey(domainKey, "schema")
  );
  private final ConsumerKey consumerKey = new ConsumerKey(streamKey, zoneKey, "consumer");
  private final ConsumerBindingKey consumerBindingKey = new ConsumerBindingKey(consumerKey, streamBindingKey);
  private final ProducerKey producerKey = new ProducerKey(streamKey, zoneKey, "producer");
  private final ProducerBindingKey producerBindingKey = new ProducerBindingKey(producerKey, streamBindingKey);
  private final ProcessKey processKey = new ProcessKey(domainKey, "process");
  private final ProcessSpecification processSpecification = new ProcessSpecification(
    singletonList(zoneKey),
    "description",
    singletonList(new Tag("name", "value")),
    "type",
    mapper.createObjectNode().put("foo", "bar"),
    new HashMap<String, List<Principal>>() {{
      put("admin", singletonList(new Principal("user1")));
      put("creator", asList(new Principal("user2"), new Principal("user3")));
    }},
    singletonList(new ProcessInputStream(streamKey)),
    singletonList(new ProcessOutputStream(streamKey))
  );
  private final ProcessBindingKey processBindingKey = new ProcessBindingKey(processKey, zoneKey);
  private final ProcessBindingSpecification processBindingSpecification = new ProcessBindingSpecification(
    zoneKey,
    "description",
    singletonList(new Tag("name", "value")),
    "type",
    mapper.createObjectNode().put("foo", "bar"),
    new HashMap<String, List<Principal>>() {{
      put("admin", singletonList(new Principal("user1")));
      put("creator", asList(new Principal("user2"), new Principal("user3")));
    }},
    singletonList(consumerBindingKey),
    singletonList(producerBindingKey)
  );

  @Test
  public void specificationToModel() {
    val result = underTest.toModel(avroSpecificationEvent.getKey(), avroSpecificationEvent.getValue());
    assertThat(result, is(Event.specification(domainKey, specification)));
  }

  @Test
  public void specificationDeleteToModel() {
    val result = underTest.toModel(new AvroKey(avroSpecificationKey), null);
    assertThat(result, is(Event.specificationDeletion(domainKey)));
  }

  @Test
  public void statusToModel() {
    val result = underTest.toModel(new AvroKey(avroStatusKey), new AvroValue(avroStatus));
    assertThat(result, is(Event.status(domainKey, statusEntry)));
  }

  @Test
  public void statusDeleteToModel() {
    val result = underTest.toModel(new AvroKey(avroStatusKey), null);
    assertThat(result, is(Event.statusDeletion(domainKey, "statusName")));
  }

  @Test
  public void specificationToAvro() throws IOException {
    val result = underTest.toAvro(Event.specification(domainKey, specification));
    assertEquals(avroSpecificationEvent.toByteBuffer(), result.toByteBuffer());
  }

  @Test
  public void specificationDeleteToAvro() throws IOException {
    val result = underTest.toAvro(Event.specificationDeletion(domainKey));
    assertEquals(avroSpecificationDeletionEvent.toByteBuffer(), result.toByteBuffer());
  }

  @Test
  public void statusToAvro() throws IOException {
    val result = underTest.toAvro(Event.status(domainKey, statusEntry));
    assertEquals(avroStatusEvent.toByteBuffer(), result.toByteBuffer());
  }

  @Test
  public void statusDeleteToAvro() throws IOException {
    val result = underTest.toAvro(Event.statusDeletion(domainKey, "statusName"));
    assertEquals(avroStatusDeletionEvent.toByteBuffer(), result.toByteBuffer());
  }

  @Test
  public void streamSpecificationToModel() {
    val result = underTest.toModel(avroStreamSpecificationEvent.getKey(), avroStreamSpecificationEvent.getValue());
    assertThat(result, is(Event.specification(streamKey, streamSpecification)));
  }

  @Test
  public void streamSpecificationToAvro() throws IOException {
    val result = underTest.toAvro(Event.specification(streamKey, streamSpecification));
    assertEquals(avroStreamSpecificationEvent.toByteBuffer(), result.toByteBuffer());
  }

  @Test
  public void processSpecificationToModel() {
    val result = underTest.toModel(avroProcessSpecificationEvent.getKey(), avroProcessSpecificationEvent.getValue());
    assertThat(result, is(Event.specification(processKey, processSpecification)));
  }

  @Test
  public void processSpecificationToAvro() throws IOException {
    val result = underTest.toAvro(Event.specification(processKey, processSpecification));
    assertEquals(avroProcessSpecificationEvent.toByteBuffer(), result.toByteBuffer());
  }

  @Test
  public void processBindingSpecificationToModel() {
    val result = underTest.toModel(avroProcessBindingSpecificationEvent.getKey(), avroProcessBindingSpecificationEvent.getValue());
    assertThat(result, is(Event.specification(processBindingKey, processBindingSpecification)));
  }

  @Test
  public void processBindingSpecificationToAvro() throws IOException {
    val result = underTest.toAvro(Event.specification(processBindingKey, processBindingSpecification));
    assertEquals(avroProcessBindingSpecificationEvent.toByteBuffer(), result.toByteBuffer());
  }
}
