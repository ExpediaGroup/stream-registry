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
package com.expediagroup.streamplatform.streamregistry.state;

import com.expediagroup.streamplatform.streamregistry.state.model.Entity;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.After;
import org.junit.Test;

import java.util.Map;

import static com.expediagroup.streamplatform.streamregistry.state.SampleEntities.specificationEvent;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

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
    assertThat(meterRegistry.counter("stream_registry_state.receiver.update", "event", "specificationevent").count(), is(1.0D));
  }

  @Test
  public void purgesAreRecorded() {
    underTest.purge(specificationEvent.getKey());
    assertThat(meterRegistry.counter("stream_registry_state.receiver.purge", "type", "domainkey").count(), is(1.0D));
  }
}
