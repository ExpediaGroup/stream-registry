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
package com.expediagroup.streamplatform.streamregistry.state;

import static com.expediagroup.streamplatform.streamregistry.state.SampleEntities.entity;
import static com.expediagroup.streamplatform.streamregistry.state.SampleEntities.key;
import static com.expediagroup.streamplatform.streamregistry.state.SampleEntities.specificationEvent;
import static com.expediagroup.streamplatform.streamregistry.state.StateKey.deleted;
import static com.expediagroup.streamplatform.streamregistry.state.StateKey.existing;
import static com.expediagroup.streamplatform.streamregistry.state.model.event.Event.LOAD_COMPLETE;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsMapWithSize.aMapWithSize;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.expediagroup.streamplatform.streamregistry.state.DefaultEntityView.ReceiverListener;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.DomainKey;

@RunWith(MockitoJUnitRunner.class)
public class DefaultEntityViewTest {
  @Mock private EventReceiver receiver;
  @Mock private EntityViewUpdater updater;
  @Mock private EntityViewListener listener;

  private final Map<StateKey, Entity<?, ?>> entities = new HashMap<>();

  private EntityView underTest;

  @Before
  public void before() {
    underTest = new DefaultEntityView(receiver, entities, updater);
  }

  @Test
  public void load() {
    var future = underTest.load(listener);
    var captor = ArgumentCaptor.forClass(ReceiverListener.class);
    verify(receiver).receive(captor.capture());
    var receiverListener = captor.getValue();
    assertThat(receiverListener.getListener(), is(listener));
    assertThat(receiverListener.getFuture(), is(future));
  }

  @Test
  public void loadNoArgs() {
    var future = underTest.load();

    var captor = ArgumentCaptor.forClass(ReceiverListener.class);
    verify(receiver).receive(captor.capture());
    var receiverListener = captor.getValue();
    assertThat(receiverListener.getListener(), is(EntityViewListener.NULL));
    assertThat(receiverListener.getFuture(), is(future));
  }

  @Test
  public void updateNotLoaded() {
    var future = underTest.load(listener);

    var captor = ArgumentCaptor.forClass(ReceiverListener.class);
    verify(receiver).receive(captor.capture());
    var receiverListener = captor.getValue();
    receiverListener.onEvent(specificationEvent);
    verify(updater).update(specificationEvent);
    assertThat(future.isDone(), is(false));
    verify(listener, never()).onEvent(null, specificationEvent);
  }

  @Test
  public void updateLoaded() {
    var future = underTest.load(listener);

    var captor = ArgumentCaptor.forClass(ReceiverListener.class);
    verify(receiver).receive(captor.capture());
    var receiverListener = captor.getValue();
    receiverListener.onEvent(LOAD_COMPLETE);
    verify(updater, never()).update(any());
    assertThat(future.isDone(), is(true));
    verify(listener, never()).onEvent(null, specificationEvent);
  }

  @Test
  public void updateLoadedListenerInvoked() {
    underTest.load(listener);

    var captor = ArgumentCaptor.forClass(ReceiverListener.class);
    verify(receiver).receive(captor.capture());
    var receiverListener = captor.getValue();
    receiverListener.onEvent(LOAD_COMPLETE);
    receiverListener.onEvent(specificationEvent);
    verify(updater).update(specificationEvent);
    verify(listener).onEvent(null, specificationEvent);
  }

  @Test
  public void getPresent() {
    entities.put(existing(key), entity);

    var result = underTest.get(key);

    assertThat(result.isPresent(), is(true));
    assertThat(result.get(), is(entity));
  }

  @Test
  public void getAbsent() {
    var result = underTest.get(key);

    assertThat(result.isPresent(), is(false));
  }

  @Test
  public void allPresent() {
    entities.put(existing(key), entity);

    var result = underTest.all(DomainKey.class).collect(toList());

    assertThat(result.size(), is(1));
    assertThat(result.get(0), is(entity));
  }

  @Test
  public void allAbsent() {
    var result = underTest.all(DomainKey.class).collect(toList());

    assertThat(result.size(), is(0));
  }

  @Test
  public void allDeletedEntities() {
    entities.put(deleted(key), entity);

    var existingEntities = underTest.all(DomainKey.class).collect(toList());
    var deletedEntities = underTest.allDeleted(DomainKey.class).collect(toList());

    assertThat(existingEntities, hasSize(0));
    assertThat(deletedEntities, hasSize(1));
  }

  @Test
  public void allPurgeEntities() {
    entities.put(deleted(key), entity);
    var deletedEntities = underTest.allDeleted(DomainKey.class).collect(toList());
    assertThat(deletedEntities, hasSize(1));

    when(updater.purge(key)).thenAnswer(i -> Optional.ofNullable(entities.remove(deleted(key))));
    var purged = underTest.purgeDeleted(key);

    var deletedEntitiesPostPurge = underTest.allDeleted(DomainKey.class).collect(toList());
    assertThat(purged, is(Optional.of(entity)));
    assertThat(deletedEntitiesPostPurge, hasSize(0));
    assertThat(entities, is(aMapWithSize(0)));
  }
}
