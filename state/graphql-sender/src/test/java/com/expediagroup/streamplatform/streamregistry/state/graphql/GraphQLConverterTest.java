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
package com.expediagroup.streamplatform.streamregistry.state.graphql;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.junit.Test;

import com.expediagroup.streamplatform.streamregistry.state.model.Entity;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.DomainKey;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.SchemaKey;
import com.expediagroup.streamplatform.streamregistry.state.model.event.Event;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.DefaultSpecification;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.StreamSpecification;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.Tag;
import com.expediagroup.streamplatform.streamregistry.state.model.status.StatusEntry;

public class GraphQLConverterTest {

  private final GraphQLConverter underTest = new GraphQLConverter();

  private final DomainKey domainKey = new DomainKey("domain");
  private final SchemaKey schemaKey = new SchemaKey(domainKey, "schema");
  private final Entity.StreamKey streamkey = new Entity.StreamKey(domainKey, "stream", 1);
  private final Entity.ZoneKey zoneKey = new Entity.ZoneKey("zone");
  private final Entity.InfrastructureKey infrastructureKey = new Entity.InfrastructureKey(zoneKey, "zone");
  private final Entity.ProducerKey producerKey = new Entity.ProducerKey(streamkey, zoneKey, "producer");
  private final Entity.ConsumerKey consumerKey = new Entity.ConsumerKey(streamkey, zoneKey, "consumer");
  private final Entity.StreamBindingKey streamBindingKey = new Entity.StreamBindingKey(streamkey, infrastructureKey);
  private final Entity.ProducerBindingKey producerBindingKey = new Entity.ProducerBindingKey(producerKey, streamBindingKey);
  private final Entity.ConsumerBindingKey consumerBindingKey = new Entity.ConsumerBindingKey(consumerKey, streamBindingKey);

  private final ObjectMapper mapper = new ObjectMapper();
  private final Tag tag = new Tag("name", "value");
  private final ObjectNode configuration = mapper.createObjectNode();
  private final DefaultSpecification specification = new DefaultSpecification("description", List.of(tag), "type", configuration);
  private final StreamSpecification streamSpecification = new StreamSpecification("description", List.of(tag), "type", configuration, schemaKey);
  private final StatusEntry statusEntry = new StatusEntry("agentStatus", mapper.createObjectNode());

  @Test(expected = IllegalArgumentException.class)
  public void unknownKey() {
    var key = new Entity.Key<>() {};
    underTest.convert(Event.of(key, specification));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void statusDeletion() {
    underTest.convert(Event.of(domainKey, "statusName"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void unknownEvent() {
    underTest.convert(() -> domainKey);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void unsupportedStatusName() {
    underTest.convert(Event.of(domainKey, new StatusEntry("foo", mapper.createObjectNode())));
  }

  @Test
  public void domainSpecification() {
    var mutation = underTest.convert(Event.of(domainKey, specification));
    assertThat(mutation, is(instanceOf(DomainSpecificationMutation.class)));
  }

  @Test
  public void domainStatus() {
    var mutation = underTest.convert(Event.of(domainKey, statusEntry));
    assertThat(mutation, is(instanceOf(DomainStatusMutation.class)));
  }

  @Test
  public void domainDeletion() {
    var mutation = underTest.convert(Event.of(domainKey));
    assertThat(mutation, is(instanceOf(DomainDeletionMutation.class)));
  }

  @Test
  public void schemaSpecification() {
    var mutation = underTest.convert(Event.of(schemaKey, specification));
    assertThat(mutation, is(instanceOf(SchemaSpecificationMutation.class)));
  }

  @Test
  public void schemaStatus() {
    var mutation = underTest.convert(Event.of(schemaKey, statusEntry));
    assertThat(mutation, is(instanceOf(SchemaStatusMutation.class)));
  }

  @Test
  public void schemaDeletion() {
    var mutation = underTest.convert(Event.of(schemaKey));
    assertThat(mutation, is(instanceOf(SchemaDeletionMutation.class)));
  }

  @Test
  public void streamSpecification() {
    var mutation = underTest.convert(Event.of(streamkey, streamSpecification));
    assertThat(mutation, is(instanceOf(StreamSpecificationMutation.class)));
  }

  @Test
  public void streamStatus() {
    var mutation = underTest.convert(Event.of(streamkey, statusEntry));
    assertThat(mutation, is(instanceOf(StreamStatusMutation.class)));
  }

  @Test
  public void streamDeletion() {
    var mutation = underTest.convert(Event.of(streamkey));
    assertThat(mutation, is(instanceOf(StreamDeletionMutation.class)));
  }

  @Test
  public void zoneSpecification() {
    var mutation = underTest.convert(Event.of(zoneKey, specification));
    assertThat(mutation, is(instanceOf(ZoneSpecificationMutation.class)));
  }

  @Test
  public void zoneStatus() {
    var mutation = underTest.convert(Event.of(zoneKey, statusEntry));
    assertThat(mutation, is(instanceOf(ZoneStatusMutation.class)));
  }

  @Test
  public void zoneDeletion() {
    var mutation = underTest.convert(Event.of(zoneKey));
    assertThat(mutation, is(instanceOf(ZoneDeletionMutation.class)));
  }

  @Test
  public void infrastructureSpecification() {
    var mutation = underTest.convert(Event.of(infrastructureKey, specification));
    assertThat(mutation, is(instanceOf(InfrastructureSpecificationMutation.class)));
  }

  @Test
  public void infrastructureStatus() {
    var mutation = underTest.convert(Event.of(infrastructureKey, statusEntry));
    assertThat(mutation, is(instanceOf(InfrastructureStatusMutation.class)));
  }

  @Test
  public void infrastructureDeletion() {
    var mutation = underTest.convert(Event.of(infrastructureKey));
    assertThat(mutation, is(instanceOf(InfrastructureDeletionMutation.class)));
  }

  @Test
  public void producerSpecification() {
    var mutation = underTest.convert(Event.of(producerKey, specification));
    assertThat(mutation, is(instanceOf(ProducerSpecificationMutation.class)));
  }

  @Test
  public void producerStatus() {
    var mutation = underTest.convert(Event.of(producerKey, statusEntry));
    assertThat(mutation, is(instanceOf(ProducerStatusMutation.class)));
  }

  @Test
  public void producerDeletion() {
    var mutation = underTest.convert(Event.of(producerKey));
    assertThat(mutation, is(instanceOf(ProducerDeletionMutation.class)));
  }

  @Test
  public void consumerSpecification() {
    var mutation = underTest.convert(Event.of(consumerKey, specification));
    assertThat(mutation, is(instanceOf(ConsumerSpecificationMutation.class)));
  }

  @Test
  public void consumerStatus() {
    var mutation = underTest.convert(Event.of(consumerKey, statusEntry));
    assertThat(mutation, is(instanceOf(ConsumerStatusMutation.class)));
  }

  @Test
  public void consumerDeletion() {
    var mutation = underTest.convert(Event.of(consumerKey));
    assertThat(mutation, is(instanceOf(ConsumerDeletionMutation.class)));
  }

  @Test
  public void streamBindingSpecification() {
    var mutation = underTest.convert(Event.of(streamBindingKey, specification));
    assertThat(mutation, is(instanceOf(StreamBindingSpecificationMutation.class)));
  }

  @Test
  public void streamBindingStatus() {
    var mutation = underTest.convert(Event.of(streamBindingKey, statusEntry));
    assertThat(mutation, is(instanceOf(StreamBindingStatusMutation.class)));
  }

  @Test
  public void streamBindingDeletion() {
    var mutation = underTest.convert(Event.of(streamBindingKey));
    assertThat(mutation, is(instanceOf(StreamBindingDeletionMutation.class)));
  }

  @Test
  public void producerBindingSpecification() {
    var mutation = underTest.convert(Event.of(producerBindingKey, specification));
    assertThat(mutation, is(instanceOf(ProducerBindingSpecificationMutation.class)));
  }

  @Test
  public void producerBindingStatus() {
    var mutation = underTest.convert(Event.of(producerBindingKey, statusEntry));
    assertThat(mutation, is(instanceOf(ProducerBindingStatusMutation.class)));
  }

  @Test
  public void producerBindingDeletion() {
    var mutation = underTest.convert(Event.of(producerBindingKey));
    assertThat(mutation, is(instanceOf(ProducerBindingDeletionMutation.class)));
  }

  @Test
  public void consumerBindingSpecification() {
    var mutation = underTest.convert(Event.of(consumerBindingKey, specification));
    assertThat(mutation, is(instanceOf(ConsumerBindingSpecificationMutation.class)));
  }

  @Test
  public void consumerBindingStatus() {
    var mutation = underTest.convert(Event.of(consumerBindingKey, statusEntry));
    assertThat(mutation, is(instanceOf(ConsumerBindingStatusMutation.class)));
  }

  @Test
  public void consumerBindingDeletion() {
    var mutation = underTest.convert(Event.of(consumerBindingKey));
    assertThat(mutation, is(instanceOf(ConsumerBindingDeletionMutation.class)));
  }
}
