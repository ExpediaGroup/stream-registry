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
package com.expediagroup.streamplatform.streamregistry.state;



import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class DefaultEntityViewTest {
//  @Mock private EventReceiver.Factory receiverFactory;
//  @Mock private EventReceiver receiver;
//  @Mock private EventReceiver.Listener receiverListener;
//  @Mock private EntityViewListener listener;
//  @Mock private DefaultEventReceiverListener.Factory receiverListenerFactory;
//
//  private final Map<Entity.Key<?>, Entity<?, ?>> entities = new HashMap<>();
//
//  private EntityView underTest;
//
//  @Before
//  public void before() {
//    underTest = new EntityView(receiverFactory, entities, receiverListenerFactory);
//
//    when(receiverFactory.create()).thenReturn(receiver);
//  }
//
//  @Test
//  public void load() {
//    when(receiverListenerFactory.create(eq(listener), any())).thenReturn(receiverListener);
//
//    CompletableFuture<Void> future = underTest.load(listener);
//
//    ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
//    verify(receiverListenerFactory).create(eq(listener), runnableCaptor.capture());
//    verify(receiver).receive(receiverListener);
//
//    Runnable runnable = runnableCaptor.getValue();
//    assertThat(future.isDone(), is(false));
//    runnable.run();
//    assertThat(future.isDone(), is(true));
//  }
//
//  @Test
//  public void getPresent() {
//    entities.put(key, entity);
//
//    Optional<Entity<DomainKey, DefaultSpecification>> result = underTest.get(key);
//
//    assertThat(result.isPresent(), is(true));
//    assertThat(result.get(), is(entity));
//  }
//
//  @Test
//  public void getAbsent() {
//    Optional<Entity<DomainKey, DefaultSpecification>> result = underTest.get(key);
//
//    assertThat(result.isPresent(), is(false));
//  }
//
//  @Test
//  public void allPresent() {
//    entities.put(key, entity);
//
//    List<Entity<DomainKey, DefaultSpecification>> result = underTest.all(DomainKey.class).collect(toList());
//
//    assertThat(result.size(), is(1));
//    assertThat(result.get(0), is(entity));
//  }
//
//  @Test
//  public void allAbsent() {
//    List<Entity<DomainKey, DefaultSpecification>> result = underTest.all(DomainKey.class).collect(toList());
//
//    assertThat(result.size(), is(0));
//  }
//
//  @Test
//  public void close() throws Exception {
//    underTest.load(listener);
//    underTest.close();
//
//    verify(receiver).close();
//  }
//
//  @Test
//  public void closeWithoutLoad() throws Exception {
//    underTest.close();
//
//    verify(receiver, never()).close();
//  }
}
