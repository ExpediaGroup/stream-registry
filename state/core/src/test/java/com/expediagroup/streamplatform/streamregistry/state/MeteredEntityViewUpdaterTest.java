/**
 * Copyright (C) 2018-2023 Expedia, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.expediagroup.streamplatform.streamregistry.state;

import com.expediagroup.streamplatform.streamregistry.state.model.Entity;
import com.expediagroup.streamplatform.streamregistry.state.model.event.Event;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.After;
import org.junit.Test;

import java.util.Map;

import static com.expediagroup.streamplatform.streamregistry.state.SampleEntities.specificationEvent;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

public class MeteredEntityViewUpdaterTest extends EntityViewUpdaterTest {

  private static final MeterRegistry meterRegistry = new SimpleMeterRegistry();

  @After
  public void cleanMeterRegistry() {
    meterRegistry.clear();
  }

  @Override
  public EntityViewUpdater entityViewUpdater(Map<Entity.Key<?>, StateValue> entities) {
    return new EntityViews.MeteredEntityViewUpdater(new DefaultEntityViewUpdater(entities), meterRegistry);
  }

  @Test
  public void updatesAreRecorded() {
    underTest.update(specificationEvent);
    assertThat(meterRegistry.counter("stream_registry_state.receiver.update", "event", "specificationevent", "key", "domainkey").count(), is(1.0D));
  }

  @SuppressWarnings("rawtypes")
  @Test
  public void nullKeysAndSpecAreHandled() {
    Event<?, ?> event = (Event) () -> null;
    assertThrows(IllegalArgumentException.class, () -> underTest.update(event));

    Counter counter = meterRegistry.find("stream_registry_state.receiver.update").counter();
    assertNotNull(counter);
    assertThat(counter.count(), is(1.0D));
  }

  public void nullsArePassedThrough() {
    assertThrows(IllegalArgumentException.class, () -> underTest.update(null));
    assertThat(meterRegistry.counter("stream_registry_state.receiver.update", "event", "null", "key", "null").count(), is(1.0D));
  }

  @Test
  public void purgesAreRecorded() {
    underTest.purge(specificationEvent.getKey());
    assertThat(meterRegistry.counter("stream_registry_state.receiver.purge", "key", "domainkey").count(), is(1.0D));
  }
}
