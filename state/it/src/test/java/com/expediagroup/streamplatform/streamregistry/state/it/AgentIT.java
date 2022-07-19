/**
 * Copyright (C) 2018-2022 Expedia, Inc.
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

import static com.expediagroup.streamplatform.streamregistry.state.AgentData.generateData;
import static com.expediagroup.streamplatform.streamregistry.state.model.event.Event.specificationDeletion;
import static java.util.UUID.randomUUID;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.core.IsIterableContaining.hasItem;
import static org.hamcrest.core.IsIterableContaining.hasItems;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import org.apache.commons.lang3.tuple.Pair;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.testcontainers.containers.KafkaContainer;

import com.expediagroup.streamplatform.streamregistry.state.AgentData;
import com.expediagroup.streamplatform.streamregistry.state.EntityView;
import com.expediagroup.streamplatform.streamregistry.state.EntityViewListener;
import com.expediagroup.streamplatform.streamregistry.state.EntityViews;
import com.expediagroup.streamplatform.streamregistry.state.EventSender;
import com.expediagroup.streamplatform.streamregistry.state.kafka.KafkaEventReceiver;
import com.expediagroup.streamplatform.streamregistry.state.kafka.KafkaEventSender;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.DomainKey;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.Key;
import com.expediagroup.streamplatform.streamregistry.state.model.event.Event;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.DefaultSpecification;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.Specification;

public class AgentIT {

  private final MeterRegistry meterRegistry = new SimpleMeterRegistry();

  private final String schemaRegistryUrl = "mock://schemas";

  private final ConditionFactory await = Awaitility.await()
    .atMost(10, SECONDS)
    .pollDelay(1, SECONDS);

  @Rule
  public KafkaContainer kafka = new KafkaContainer();

  private String topicName;
  private KafkaEventSender kafkaEventSender;
  private EntityView entityView;
  private StoringEntityViewListener dummyAgent;
  private AgentData data;

  @Before
  public void setUp() {
    topicName = topicName();
    kafkaEventSender = kafkaEventSender(topicName);
    entityView = EntityViews.meteredEntityView(kafkaEventReceiver(topicName, "groupId"), meterRegistry);
    dummyAgent = new StoringEntityViewListener();
    data = generateData();
  }

  @Test
  public void concurrentModification() {
    sendSync(kafkaEventSender, data.getSpecificationEvent());
    startAgent(entityView, dummyAgent);

    val stream = entityView.all(Entity.DomainKey.class);
    AtomicInteger size = new AtomicInteger(1);

    stream.forEach((it) -> {
      if (size.get() < 10) {
        // receiving an event in the middle of processing should not blow anything up
        sendSync(kafkaEventSender, generateData().getSpecificationEvent());
        size.incrementAndGet();
        await.untilAsserted(() -> {
          val list = entityView.all(Entity.DomainKey.class)
            .collect(Collectors.toList());
          assertThat(list, hasSize(size.get()));
        });
      }
    });
  }

  @Test
  public void testBootstrapping() {
    sendSync(kafkaEventSender, data.getSpecificationEvent());

    startAgent(entityView, dummyAgent);

    // bootstrapping loads the state for the agent
    assertThat(domainEvents(entityView), hasSize(1));
    assertThat(domainEvents(entityView), hasItem(data.getEntity()));
    assertThat(deletedDomainEvents(entityView), is(aMapWithSize(0)));
    // but no events are received during bootstrapping
    assertThat(dummyAgent.events, hasSize(0));

    // new events start after bootstrapping
    val dataTwo = generateData();
    sendSync(kafkaEventSender, dataTwo.getSpecificationEvent());

    // after bootstrapping the state continues to be updated
    await.untilAsserted(() -> {
      assertThat(domainEvents(entityView), hasSize(2));
      assertThat(domainEvents(entityView), hasItems(data.getEntity(), dataTwo.getEntity()));
      assertThat(deletedDomainEvents(entityView), is(aMapWithSize(0)));
      // Agents receive all new incoming events
      assertThat(dummyAgent.events, hasSize(1));
      assertThat(dummyAgent.events, hasItem(Pair.of(null, dataTwo.getSpecificationEvent())));
    });

    // updating event causes agent to receive the old entity
    sendSync(kafkaEventSender, dataTwo.getSpecificationEvent());
    await.untilAsserted(() -> {
      assertThat(dummyAgent.events, hasSize(2));
      assertThat(dummyAgent.events, contains(
        Pair.of(null, dataTwo.getSpecificationEvent()), Pair.of(dataTwo.getEntity(), dataTwo.getSpecificationEvent())
      ));
    });
  }

  @Test
  public void testDeletedEntities() {
    startAgent(entityView, dummyAgent);

    val data = generateData();
    sendSync(kafkaEventSender, data.getSpecificationEvent());

    await.untilAsserted(() -> {
      assertThat(domainEvents(entityView), hasSize(1));
      assertThat(domainEvents(entityView), hasItem(data.getEntity()));
      assertThat(deletedDomainEvents(entityView), is(aMapWithSize(0)));
      assertThat(dummyAgent.events, hasSize(1));
      assertThat(dummyAgent.events, hasItem(Pair.of(null, data.getSpecificationEvent())));
    });

    sendSync(kafkaEventSender, specificationDeletion(data.getKey()));

    await.untilAsserted(() -> {
      // entity no longer exists
      assertThat(domainEvents(entityView), hasSize(0));
      // entity is marked as deleted
      assertThat(deletedDomainEvents(entityView), is(aMapWithSize(1)));
      assertThat(deletedDomainEvents(entityView), hasEntry(data.getKey(), Optional.of(data.getEntity())));

      // onEvent has been called for the deleted entity
      assertThat(dummyAgent.events, hasSize(2));
      assertThat(dummyAgent.events, hasItem(Pair.of(null, data.getSpecificationEvent())));
      assertThat(dummyAgent.events, hasItem(Pair.of(data.getEntity(), specificationDeletion(data.getKey()))));  // the agent would be expected to handle the deletion event.
    });

    // purge would be called by the agent after the delete has been handled
    entityView.purgeDeleted(data.getKey());
    // the delete is removed from everywhere
    assertThat(domainEvents(entityView), hasSize(0));
    assertThat(deletedDomainEvents(entityView), is(aMapWithSize(0)));
  }

  @Test
  public void testOfflineDeletes() {
    val data = generateData();
    sendSync(kafkaEventSender, data.getSpecificationEvent());
    sendSync(kafkaEventSender, specificationDeletion(data.getKey()));

    startAgent(entityView, dummyAgent);

    assertThat(dummyAgent.events, hasSize(0));
    assertThat(domainEvents(entityView), hasSize(0));
    assertThat(deletedDomainEvents(entityView), is(aMapWithSize(1)));
    assertThat(deletedDomainEvents(entityView), hasEntry(data.getKey(), Optional.of(data.getEntity())));

    // agent would run a scheduled task to handle deletes that were issued while offline and then purge those deletes
    entityView.purgeDeleted(data.getKey());

    assertThat(domainEvents(entityView), hasSize(0));
    assertThat(deletedDomainEvents(entityView), is(aMapWithSize(0)));

    // simulate the restart of an Agent
    val restartedEntityView = EntityViews.meteredEntityView(kafkaEventReceiver(topicName, "groupId"), meterRegistry);
    val restartedAgent = new StoringEntityViewListener();
    startAgent(restartedEntityView, restartedAgent);

    // after a restart the delete will appear in the EntityView again. Agents need to handle this
    assertThat(restartedAgent.events, hasSize(0));
    assertThat(domainEvents(restartedEntityView), hasSize(0));
    assertThat(deletedDomainEvents(restartedEntityView), is(aMapWithSize(1)));
    assertThat(deletedDomainEvents(restartedEntityView), hasEntry(data.getKey(), Optional.of(data.getEntity())));

    // Agent should check to see if handling the delete is required, then purge the deleted entity
    restartedEntityView.purgeDeleted(data.getKey());
    assertThat(domainEvents(restartedEntityView), hasSize(0));
    assertThat(deletedDomainEvents(restartedEntityView), is(aMapWithSize(0)));
  }

  @Test
  public void testDeleteWithMissingEntity() {
    startAgent(entityView, dummyAgent);

    // backing topic will contain only the deletion and not the original updated entity
    sendSync(kafkaEventSender, specificationDeletion(data.getKey()));

    await.untilAsserted(() -> {
      assertThat(dummyAgent.events, hasItem(Pair.of(null, specificationDeletion(data.getKey()))));
      assertThat(deletedDomainEvents(entityView), hasEntry(data.getKey(), Optional.empty()));
    });

    // purge would be called by the agent after the delete has been handled
    entityView.purgeDeleted(data.getKey());
    // the delete is removed from everywhere
    assertThat(domainEvents(entityView), hasSize(0));
    assertThat(deletedDomainEvents(entityView), is(aMapWithSize(0)));
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

  private Map<DomainKey, Optional<Entity<DomainKey, DefaultSpecification>>> deletedDomainEvents(EntityView entityView) {
    return entityView.allDeleted(DomainKey.class);
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
