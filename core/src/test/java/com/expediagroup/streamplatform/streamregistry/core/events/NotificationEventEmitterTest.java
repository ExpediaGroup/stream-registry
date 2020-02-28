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

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import lombok.Builder;
import lombok.EqualsAndHashCode;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class NotificationEventEmitterTest {
  private static String DUMMY_SOURCE_EVENT_PREFIX = "dummytype-";

  AtomicReference<NotificationEvent<DummyType>> notificationChecker = new AtomicReference<>();
  AtomicReference<Throwable> exceptionChecker = new AtomicReference<>();

  @Before
  public void before() {
    notificationChecker.set(null);
    exceptionChecker.set(null);
  }

  @Test
  public void givenAnEntityForProcessFlow_EmitANotification() throws InterruptedException {
    MyService myService = Mockito.spy(MyService.builder()
        .emitter(nt -> notificationChecker.set(nt))
        .build());

    final DummyType entity = getRandomDummyType();
    final EventType type = EventType.CREATE;
    final String source = DUMMY_SOURCE_EVENT_PREFIX + "create";

    final NotificationEvent<DummyType> event = NotificationEvent.<DummyType>builder()
        .source(source)
        .eventType(type)
        .entity(entity)
        .build();

    Mockito.lenient().when(myService.create(entity)).thenCallRealMethod();
    Mockito.verify(myService, Mockito.times(1)).create(entity);
    Mockito.verify(myService, Mockito.times(1)).emitEventOnProcessedEntity(type, entity);

    Thread.sleep(1000);

    // For some reason mockito is ignoring interface's default method. That's why I used lenient() and prepared
    // following assertions.

    // If our checker has a notification it means that default emitEvent method was executed with a non-null entity.
    Assert.assertNotNull(notificationChecker.get());
    Assert.assertEquals(notificationChecker.get(), event);
  }

  @Test
  public void givenANullEntityForProcessFlow_IgnoreEmitEvent() throws InterruptedException {
    MyService myService = Mockito.spy(MyService.builder()
        .emitter(nt -> notificationChecker.set(nt))
        .build());

    final DummyType entity = getRandomDummyType();
    final EventType type = EventType.CREATE;
    final String source = DUMMY_SOURCE_EVENT_PREFIX + "create";

    final NotificationEvent<DummyType> event = NotificationEvent.<DummyType>builder()
        .source(source)
        .eventType(type)
        .entity(entity)
        .build();

    Mockito.lenient().when(myService.create(null)).thenCallRealMethod();
    Mockito.verify(myService, Mockito.times(1)).create(null);
    Mockito.verify(myService, Mockito.times(1)).emitEventOnProcessedEntity(type, null);

    Thread.sleep(1000);

    // For some reason mockito is ignoring interface's default method. That's why I used lenient() and prepared
    // following assertions.

    // If our checker doesn't has a notification it means that default emitEvent method was executed with a null
    // entity.
    Assert.assertNull(notificationChecker.get());
  }

  @Test
  public void givenAnEntityForProcessFlow_EmitANotificationAndHandleTheException() throws InterruptedException {
    MyService myService = Mockito.spy(MyService.builder()
        .emitter(nt -> {
          throw new IllegalStateException("BOOOOOM!");
        })
        .onError((ex, ne) -> {
          exceptionChecker.set(ex);
          notificationChecker.set(ne);
        })
        .build());

    final DummyType entity = getRandomDummyType();
    final EventType type = EventType.CREATE;
    final String source = DUMMY_SOURCE_EVENT_PREFIX + "create";

    final NotificationEvent<DummyType> event = NotificationEvent.<DummyType>builder()
        .source(source)
        .eventType(type)
        .entity(entity)
        .build();

    Mockito.lenient().when(myService.create(entity)).thenCallRealMethod();
    Mockito.verify(myService, Mockito.times(1)).create(entity);
    Mockito.verify(myService, Mockito.times(1)).emitEventOnProcessedEntity(type, entity);
    Mockito.verify(myService, Mockito.timeout(5000).times(1)).onFailedEmitting(Mockito.any(), Mockito.eq(event));

    Thread.sleep(1000);

    // For some reason mockito is ignoring interface's default method. That's why I used lenient() and prepared
    // following assertions.

    // If our checker has a notification it means that default emitEvent method was executed with a non-null entity.
    Assert.assertNotNull(notificationChecker.get());
    Assert.assertEquals(notificationChecker.get(), event);

    // If our exception checker has a notification it means that default emitEvent method thrown an exception.
    Assert.assertNotNull(exceptionChecker.get());
    Assert.assertTrue(exceptionChecker.get() instanceof Throwable);
  }

  @Builder
  public static class MyService implements NotificationEventEmitter<DummyType> {

    private final Consumer<NotificationEvent<DummyType>> emitter;
    private final BiConsumer<Throwable, NotificationEvent<DummyType>> onError;

    public Optional<DummyType> create(DummyType dummyType) {
      return emitEventOnProcessedEntity(EventType.CREATE, dummyType);
    }

    @Override
    public Optional<DummyType> emitEventOnProcessedEntity(EventType type, DummyType entity) {
      return this.emitEvent(emitter, DUMMY_SOURCE_EVENT_PREFIX, type, entity);
    }

    @Override
    public void onFailedEmitting(Throwable ex, NotificationEvent event) {
      onError.accept(ex, event);
    }
  }

  @Builder
  @EqualsAndHashCode
  public static class DummyType {
    private final Long id;
    private final String payload;
  }

  public NotificationEventEmitterTest.DummyType getRandomDummyType() {
    return NotificationEventEmitterTest.DummyType.builder()
        .id(Instant.now().getEpochSecond())
        .payload("" + Instant.now().getEpochSecond())
        .build();
  }
}