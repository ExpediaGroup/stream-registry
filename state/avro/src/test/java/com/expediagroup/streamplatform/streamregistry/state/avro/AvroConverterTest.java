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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Test;

import com.expediagroup.streamplatform.streamregistry.state.model.Entity;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.DomainKey;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.StreamKey;
import com.expediagroup.streamplatform.streamregistry.state.model.event.Event;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.DefaultSpecification;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.StreamSpecification;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.Tag;
import com.expediagroup.streamplatform.streamregistry.state.model.status.StatusEntry;

public class AvroConverterTest {
  private final ObjectMapper mapper = new ObjectMapper();

  private final AvroConverter underTest = new AvroConverter();

  private final AvroDomainKey avroDomainKey = new AvroDomainKey("domain");
  private final AvroStreamKey avroStreamKey = new AvroStreamKey(avroDomainKey, "stream", 1);

  private final AvroSpecificationKey avroSpecificationKey = new AvroSpecificationKey(avroDomainKey);
  private final AvroSpecification avroSpecification = new AvroSpecification(
      "description",
      List.of(new AvroTag("name", "value")),
      "type",
      new AvroObject(Map.of("foo", "bar"))
  );
  private final AvroStreamSpecification avroStreamSpecification = new AvroStreamSpecification(
      "description",
      List.of(new AvroTag("name", "value")),
      "type",
      new AvroObject(Map.of("foo", "bar")),
      new AvroSchemaKey(avroDomainKey, "schema")
  );

  private final AvroStatusKey avroStatusKey = new AvroStatusKey(avroDomainKey, "statusName");
  private final AvroObject avroObjectStatus = new AvroObject(Map.of("foo", "baz"));
  private final AvroStatus avroStatus = new AvroStatus(avroObjectStatus);

  private final AvroEvent avroSpecificationEvent = new AvroEvent(new AvroKey(avroSpecificationKey), new AvroValue(avroSpecification));
  private final AvroEvent avroStreamSpecificationEvent = new AvroEvent(new AvroKey(new AvroSpecificationKey(avroStreamKey)), new AvroValue(avroStreamSpecification));
  private final AvroEvent avroStatusEvent = new AvroEvent(new AvroKey(avroStatusKey), new AvroValue(avroStatus));
  private final AvroEvent avroSpecificationDeletionEvent = new AvroEvent(new AvroKey(avroSpecificationKey), null);
  private final AvroEvent avroStatusDeletionEvent = new AvroEvent(new AvroKey(avroStatusKey), null);

  private final DomainKey domainKey = new DomainKey("domain");
  private final DefaultSpecification specification = new DefaultSpecification(
      "description",
      List.of(new Tag("name", "value")),
      "type",
      mapper.createObjectNode().put("foo", "bar")
  );
  private final StatusEntry statusEntry = new StatusEntry("statusName", mapper.createObjectNode().put("foo", "baz"));
  private final StreamKey streamKey = new StreamKey(domainKey, "stream", 1);
  private final StreamSpecification streamSpecification = new StreamSpecification(
      "description",
      List.of(new Tag("name", "value")),
      "type",
      mapper.createObjectNode().put("foo", "bar"),
      new Entity.SchemaKey(domainKey, "schema")
  );

  @Test
  public void specificationToModel() {
    var result = underTest.toModel(avroSpecificationEvent.getKey(), avroSpecificationEvent.getValue());
    assertThat(result, is(Event.of(domainKey, specification)));
  }

  @Test
  public void specificationDeleteToModel() {
    var result = underTest.toModel(new AvroKey(avroSpecificationKey), null);
    assertThat(result, is(Event.of(domainKey)));
  }

  @Test
  public void statusToModel() {
    var result = underTest.toModel(new AvroKey(avroStatusKey), new AvroValue(avroStatus));
    assertThat(result, is(Event.of(domainKey, statusEntry)));
  }

  @Test
  public void statusDeleteToModel() {
    var result = underTest.toModel(new AvroKey(avroStatusKey), null);
    assertThat(result, is(Event.of(domainKey, "statusName")));
  }

  @Test
  public void specificationToAvro() throws IOException {
    var result = underTest.toAvro(Event.of(domainKey, specification));
    assertEquals(avroSpecificationEvent.toByteBuffer(), result.toByteBuffer());
  }

  @Test
  public void specificationDeleteToAvro() throws IOException {
    var result = underTest.toAvro(Event.of(domainKey));
    assertEquals(avroSpecificationDeletionEvent.toByteBuffer(), result.toByteBuffer());
  }

  @Test
  public void statusToAvro() throws IOException {
    var result = underTest.toAvro(Event.of(domainKey, statusEntry));
    assertEquals(avroStatusEvent.toByteBuffer(), result.toByteBuffer());
  }

  @Test
  public void statusDeleteToAvro() throws IOException {
    var result = underTest.toAvro(Event.of(domainKey, "statusName"));
    assertEquals(avroStatusDeletionEvent.toByteBuffer(), result.toByteBuffer());
  }

  @Test
  public void streamSpecificationToModel() {
    var result = underTest.toModel(avroStreamSpecificationEvent.getKey(), avroStreamSpecificationEvent.getValue());
    assertThat(result, is(Event.of(streamKey, streamSpecification)));
  }

  @Test
  public void streamSpecificationToAvro() throws IOException {
    var result = underTest.toAvro(Event.of(streamKey, streamSpecification));
    assertEquals(avroStreamSpecificationEvent.toByteBuffer(), result.toByteBuffer());
  }
}
