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
package com.expediagroup.streamplatform.streamregistry.state.graphql;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.val;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.junit.Test;

import com.expediagroup.streamplatform.streamregistry.state.model.Entity;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.DomainKey;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.SchemaKey;
import com.expediagroup.streamplatform.streamregistry.state.model.event.Event;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.DefaultSpecification;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.Principal;
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
  private final Map<String, List<Principal>> security = Stream.of(
    new AbstractMap.SimpleEntry<>("admin", Arrays.asList(new Principal("user1"))),
    new AbstractMap.SimpleEntry<>("creator", Arrays.asList(new Principal("user2"), new Principal("user3")))
  ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  private final DefaultSpecification specification = new DefaultSpecification("description", Collections.singletonList(tag), "type", configuration, security);
  private final StreamSpecification streamSpecification = new StreamSpecification("description", Collections.singletonList(tag), "type", configuration, security, schemaKey);
  private final StatusEntry statusEntry = new StatusEntry("agentStatus", mapper.createObjectNode());

  @Test(expected = IllegalArgumentException.class)
  public void unknownKey() {
    val key = new Entity.Key<DefaultSpecification>() {};
    underTest.convert(Event.specification(key, specification));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void statusDeletion() {
    underTest.convert(Event.statusDeletion(domainKey, "statusName"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void unknownEvent() {
    underTest.convert(() -> domainKey);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void unsupportedStatusName() {
    underTest.convert(Event.status(domainKey, new StatusEntry("foo", mapper.createObjectNode())));
  }

  @Test
  public void domainSpecification() {
    val mutation = underTest.convert(Event.specification(domainKey, specification));
    assertThat(mutation, is(instanceOf(DomainSpecificationMutation.class)));
  }

  @Test
  public void domainStatus() {
    val mutation = underTest.convert(Event.status(domainKey, statusEntry));
    assertThat(mutation, is(instanceOf(DomainStatusMutation.class)));
  }

  @Test
  public void domainDeletion() {
    val mutation = underTest.convert(Event.specificationDeletion(domainKey));
    assertThat(mutation, is(instanceOf(DomainDeletionMutation.class)));
  }

  @Test
  public void schemaSpecification() {
    val mutation = underTest.convert(Event.specification(schemaKey, specification));
    assertThat(mutation, is(instanceOf(SchemaSpecificationMutation.class)));
  }

  @Test
  public void schemaStatus() {
    val mutation = underTest.convert(Event.status(schemaKey, statusEntry));
    assertThat(mutation, is(instanceOf(SchemaStatusMutation.class)));
  }

  @Test
  public void schemaDeletion() {
    val mutation = underTest.convert(Event.specificationDeletion(schemaKey));
    assertThat(mutation, is(instanceOf(SchemaDeletionMutation.class)));
  }

  @Test
  public void streamSpecification() {
    val mutation = underTest.convert(Event.specification(streamkey, streamSpecification));
    assertThat(mutation, is(instanceOf(StreamSpecificationMutation.class)));
  }

  @Test
  public void streamStatus() {
    val mutation = underTest.convert(Event.status(streamkey, statusEntry));
    assertThat(mutation, is(instanceOf(StreamStatusMutation.class)));
  }

  @Test
  public void streamDeletion() {
    val mutation = underTest.convert(Event.specificationDeletion(streamkey));
    assertThat(mutation, is(instanceOf(StreamDeletionMutation.class)));
  }

  @Test
  public void zoneSpecification() {
    val mutation = underTest.convert(Event.specification(zoneKey, specification));
    assertThat(mutation, is(instanceOf(ZoneSpecificationMutation.class)));
  }

  @Test
  public void zoneStatus() {
    val mutation = underTest.convert(Event.status(zoneKey, statusEntry));
    assertThat(mutation, is(instanceOf(ZoneStatusMutation.class)));
  }

  @Test
  public void zoneDeletion() {
    val mutation = underTest.convert(Event.specificationDeletion(zoneKey));
    assertThat(mutation, is(instanceOf(ZoneDeletionMutation.class)));
  }

  @Test
  public void infrastructureSpecification() {
    val mutation = underTest.convert(Event.specification(infrastructureKey, specification));
    assertThat(mutation, is(instanceOf(InfrastructureSpecificationMutation.class)));
  }

  @Test
  public void infrastructureStatus() {
    val mutation = underTest.convert(Event.status(infrastructureKey, statusEntry));
    assertThat(mutation, is(instanceOf(InfrastructureStatusMutation.class)));
  }

  @Test
  public void infrastructureDeletion() {
    val mutation = underTest.convert(Event.specificationDeletion(infrastructureKey));
    assertThat(mutation, is(instanceOf(InfrastructureDeletionMutation.class)));
  }

  @Test
  public void producerSpecification() {
    val mutation = underTest.convert(Event.specification(producerKey, specification));
    assertThat(mutation, is(instanceOf(ProducerSpecificationMutation.class)));
  }

  @Test
  public void producerStatus() {
    val mutation = underTest.convert(Event.status(producerKey, statusEntry));
    assertThat(mutation, is(instanceOf(ProducerStatusMutation.class)));
  }

  @Test
  public void producerDeletion() {
    val mutation = underTest.convert(Event.specificationDeletion(producerKey));
    assertThat(mutation, is(instanceOf(ProducerDeletionMutation.class)));
  }

  @Test
  public void consumerSpecification() {
    val mutation = underTest.convert(Event.specification(consumerKey, specification));
    assertThat(mutation, is(instanceOf(ConsumerSpecificationMutation.class)));
  }

  @Test
  public void consumerStatus() {
    val mutation = underTest.convert(Event.status(consumerKey, statusEntry));
    assertThat(mutation, is(instanceOf(ConsumerStatusMutation.class)));
  }

  @Test
  public void consumerDeletion() {
    val mutation = underTest.convert(Event.specificationDeletion(consumerKey));
    assertThat(mutation, is(instanceOf(ConsumerDeletionMutation.class)));
  }

  @Test
  public void streamBindingSpecification() {
    val mutation = underTest.convert(Event.specification(streamBindingKey, specification));
    assertThat(mutation, is(instanceOf(StreamBindingSpecificationMutation.class)));
  }

  @Test
  public void streamBindingStatus() {
    val mutation = underTest.convert(Event.status(streamBindingKey, statusEntry));
    assertThat(mutation, is(instanceOf(StreamBindingStatusMutation.class)));
  }

  @Test
  public void streamBindingDeletion() {
    val mutation = underTest.convert(Event.specificationDeletion(streamBindingKey));
    assertThat(mutation, is(instanceOf(StreamBindingDeletionMutation.class)));
  }

  @Test
  public void producerBindingSpecification() {
    val mutation = underTest.convert(Event.specification(producerBindingKey, specification));
    assertThat(mutation, is(instanceOf(ProducerBindingSpecificationMutation.class)));
  }

  @Test
  public void producerBindingStatus() {
    val mutation = underTest.convert(Event.status(producerBindingKey, statusEntry));
    assertThat(mutation, is(instanceOf(ProducerBindingStatusMutation.class)));
  }

  @Test
  public void producerBindingDeletion() {
    val mutation = underTest.convert(Event.specificationDeletion(producerBindingKey));
    assertThat(mutation, is(instanceOf(ProducerBindingDeletionMutation.class)));
  }

  @Test
  public void consumerBindingSpecification() {
    val mutation = underTest.convert(Event.specification(consumerBindingKey, specification));
    assertThat(mutation, is(instanceOf(ConsumerBindingSpecificationMutation.class)));
  }

  @Test
  public void consumerBindingStatus() {
    val mutation = underTest.convert(Event.status(consumerBindingKey, statusEntry));
    assertThat(mutation, is(instanceOf(ConsumerBindingStatusMutation.class)));
  }

  @Test
  public void consumerBindingDeletion() {
    val mutation = underTest.convert(Event.specificationDeletion(consumerBindingKey));
    assertThat(mutation, is(instanceOf(ConsumerBindingDeletionMutation.class)));
  }
}
