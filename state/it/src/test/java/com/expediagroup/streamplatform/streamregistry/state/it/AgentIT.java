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
package com.expediagroup.streamplatform.streamregistry.state.it;

import static com.expediagroup.streamplatform.streamregistry.state.model.event.Event.specificationDeletion;
import static java.util.UUID.randomUUID;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsEmptyIterable.emptyIterable;
import static org.hamcrest.core.IsIterableContaining.hasItem;
import static org.hamcrest.core.IsIterableContaining.hasItems;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;

import org.apache.commons.lang3.tuple.Pair;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionFactory;
import org.junit.Rule;
import org.junit.Test;
import org.testcontainers.containers.KafkaContainer;

import com.expediagroup.streamplatform.streamregistry.state.AgentData;
import com.expediagroup.streamplatform.streamregistry.state.DefaultEntityView;
import com.expediagroup.streamplatform.streamregistry.state.EntityView;
import com.expediagroup.streamplatform.streamregistry.state.EntityViewListener;
import com.expediagroup.streamplatform.streamregistry.state.EventSender;
import com.expediagroup.streamplatform.streamregistry.state.kafka.KafkaEventReceiver;
import com.expediagroup.streamplatform.streamregistry.state.kafka.KafkaEventSender;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.DomainKey;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.Key;
import com.expediagroup.streamplatform.streamregistry.state.model.event.Event;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.DefaultSpecification;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.Specification;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.Tag;

public class AgentIT {

  private final String schemaRegistryUrl = "mock://schemas";

  private final ConditionFactory await = Awaitility.await()
    .atMost(10, SECONDS)
    .pollDelay(1, SECONDS);

  @Rule
  public KafkaContainer kafka = new KafkaContainer();

  @Test
  public void testBootstrapping() {
    var topicName = topicName();
    var kafkaEventSender = kafkaEventSender(topicName);
    var entityView = new DefaultEntityView(kafkaEventReceiver(topicName, "groupId"));
    var dummyAgent = new StoringEntityViewListener();
    var data = AgentData.generateData();

    sendSync(kafkaEventSender, data.getSpecificationEvent());

    startAgent(entityView, dummyAgent);

    // bootstrapping loads the state for the agent
    assertThat(domainEvents(entityView), hasSize(1));
    assertThat(domainEvents(entityView), hasItem(data.getEntity()));
    assertThat(deletedDomainEvents(entityView), is(emptyIterable()));
    // but no events are received during bootstrapping
    assertThat(dummyAgent.events, hasSize(0));

    // new events start after bootstrapping
    var dataTwo = AgentData.generateData();
    sendSync(kafkaEventSender, dataTwo.getSpecificationEvent());

    // after bootstrapping the state continues to be updated
    await.untilAsserted(() -> {
      assertThat(domainEvents(entityView), hasSize(2));
      assertThat(domainEvents(entityView), hasItems(data.getEntity(), dataTwo.getEntity()));
      assertThat(deletedDomainEvents(entityView), is(emptyIterable()));
      // Agents receive all new incoming events
      assertThat(dummyAgent.events, hasSize(1));
      assertThat(dummyAgent.events, hasItem(Pair.of(null, dataTwo.getSpecificationEvent())));
    });
  }

  @Test
  public void testDeletedEntities() {
    var topicName = topicName();
    var kafkaEventSender = kafkaEventSender(topicName);
    var entityView = new DefaultEntityView(kafkaEventReceiver(topicName, "groupId"));
    var dummyAgent = new StoringEntityViewListener();

    startAgent(entityView, dummyAgent);

    var data = AgentData.generateData();
    sendSync(kafkaEventSender, data.getSpecificationEvent());

    await.untilAsserted(() -> {
      assertThat(domainEvents(entityView), hasSize(1));
      assertThat(domainEvents(entityView), hasItem(data.getEntity()));
      assertThat(deletedDomainEvents(entityView), is(emptyIterable()));
      assertThat(dummyAgent.events, hasSize(1));
      assertThat(dummyAgent.events, hasItem(Pair.of(null, data.getSpecificationEvent())));
    });

    sendSync(kafkaEventSender, specificationDeletion(data.getKey()));

    await.untilAsserted(() -> {
      // entity no longer exists
      assertThat(domainEvents(entityView), hasSize(0));
      // entity is marked as deleted
      assertThat(deletedDomainEvents(entityView), hasSize(1));
      assertThat(deletedDomainEvents(entityView), hasItem(data.getEntity()));

      // onEvent has been called for the deleted entity
      assertThat(dummyAgent.events, hasSize(2));
      assertThat(dummyAgent.events, hasItem(Pair.of(null, data.getSpecificationEvent())));
      assertThat(dummyAgent.events, hasItem(Pair.of(data.getEntity(), specificationDeletion(data.getKey()))));  // the agent would be expected to handle the deletion event.
    });

    // purge would be called by the agent after the delete has been handled
    entityView.purgeDeleted(data.getKey());
    // the delete is removed from everywhere
    assertThat(domainEvents(entityView), hasSize(0));
    assertThat(deletedDomainEvents(entityView), hasSize(0));
  }

  @Test
  public void testOfflineDeletes() {
    var topicName = topicName();
    var kafkaEventSender = kafkaEventSender(topicName);
    var entityView = new DefaultEntityView(kafkaEventReceiver(topicName, "groupId"));
    var dummyAgent = new StoringEntityViewListener();

    var data = AgentData.generateData();
    sendSync(kafkaEventSender, data.getSpecificationEvent());
    sendSync(kafkaEventSender, specificationDeletion(data.getKey()));

    startAgent(entityView, dummyAgent);

    assertThat(dummyAgent.events, hasSize(0));
    assertThat(domainEvents(entityView), hasSize(0));
    assertThat(deletedDomainEvents(entityView), hasSize(1));
    assertThat(deletedDomainEvents(entityView), hasItem(data.getEntity()));

    // agent would run a scheduled task to handle deletes that were issued while offline and then purge those deletes
    entityView.purgeDeleted(data.getKey());

    assertThat(domainEvents(entityView), hasSize(0));
    assertThat(deletedDomainEvents(entityView), hasSize(0));

    // simulate the restart of an Agent
    var restartedEntityView = new DefaultEntityView(kafkaEventReceiver(topicName, "groupId"));
    var restartedAgent = new StoringEntityViewListener();
    startAgent(restartedEntityView, restartedAgent);

    // after a restart the delete will appear in the EntityView again. Agents need to handle this
    assertThat(restartedAgent.events, hasSize(0));
    assertThat(domainEvents(restartedEntityView), hasSize(0));
    assertThat(deletedDomainEvents(restartedEntityView), hasSize(1));
    assertThat(deletedDomainEvents(restartedEntityView), hasItem(data.getEntity()));

    // Agent should check to see if handling the delete is required, then purge the deleted entity
    restartedEntityView.purgeDeleted(data.getKey());
    assertThat(domainEvents(restartedEntityView), hasSize(0));
    assertThat(deletedDomainEvents(restartedEntityView), hasSize(0));
  }

  @Test
  public void testOfflineDeletesFollowedByCreates() {
    var topicName = topicName();
    var kafkaEventSender = kafkaEventSender(topicName);
    var entityView = new DefaultEntityView(kafkaEventReceiver(topicName, "groupId"));
    var dummyAgent = new StoringEntityViewListener();

    var data = AgentData.generateData();
    sendSync(kafkaEventSender, data.getSpecificationEvent());
    sendSync(kafkaEventSender, specificationDeletion(data.getKey()));

    var updatedData = data.withTags(List.of(new Tag("dummyTag", "dummyValue")));
    sendSync(kafkaEventSender, updatedData.getSpecificationEvent());

    startAgent(entityView, dummyAgent);

    assertThat(dummyAgent.events, hasSize(0));
    assertThat(domainEvents(entityView), hasSize(1));
    assertThat(domainEvents(entityView), hasItem(updatedData.getEntity()));
    assertThat(domainEvents(entityView), not(hasItem(data.getEntity())));
    assertThat(deletedDomainEvents(entityView), hasSize(1));
    assertThat(deletedDomainEvents(entityView), hasItem(data.getEntity()));

    // agent should check to ensure that the deleted entity hasn't been recreated (and doesn't now exist)
    var deletedKey = deletedDomainEvents(entityView).stream().map(Entity::getKey).findFirst().orElse(null);
    assertThat(domainEvents(entityView).stream().anyMatch(it -> it.getKey().equals(deletedKey)), is(true));
    // agents should just purge the deleted entity without actioning
    entityView.purgeDeleted(data.getKey());

    assertThat(domainEvents(entityView), hasSize(1));
    assertThat(deletedDomainEvents(entityView), hasSize(0));
  }

  private KafkaEventReceiver kafkaEventReceiver(String topic, String consumerGroupId) {
    return new KafkaEventReceiver(KafkaEventReceiver.Config.builder()
      .bootstrapServers(kafka.getBootstrapServers())
      .schemaRegistryUrl(schemaRegistryUrl)
      .topic(topic)
      .groupId(consumerGroupId)
      .build());
  }

  private KafkaEventSender kafkaEventSender(String topic) {
    return new KafkaEventSender(KafkaEventSender.Config.builder()
      .bootstrapServers(kafka.getBootstrapServers())
      .schemaRegistryUrl(schemaRegistryUrl)
      .topic(topic)
      .build());
  }

  @SneakyThrows
  private <K extends Key<S>, S extends Specification> void sendSync(EventSender eventSender, Event<K, S> event) {
    eventSender.send(event).get(10, SECONDS);
  }

  @SneakyThrows
  private void startAgent(EntityView entityView, EntityViewListener entityViewListener) {
    entityView.load(entityViewListener).get(1, MINUTES);
  }

  private String topicName() {
    return randomUUID().toString();
  }

  private List<Entity<DomainKey, DefaultSpecification>> domainEvents(EntityView entityView) {
    return entityView.all(DomainKey.class).collect(Collectors.toList());
  }

  private List<Entity<DomainKey, DefaultSpecification>> deletedDomainEvents(EntityView entityView) {
    return entityView.allDeleted(DomainKey.class).collect(Collectors.toList());
  }

  /**
   * Dummy "Agent" that only stores the events that it is handling.
   */
  @Getter
  private static final class StoringEntityViewListener implements EntityViewListener {
    private final List<Pair<Entity<?, ?>, Event<?, ?>>> events = new CopyOnWriteArrayList<>();

    @Override
    public <K extends Key<S>, S extends Specification> void onEvent(Entity<K, S> oldEntity, @NonNull Event<K, S> event) {
      events.add(Pair.of(oldEntity, event));
    }
  }
}
