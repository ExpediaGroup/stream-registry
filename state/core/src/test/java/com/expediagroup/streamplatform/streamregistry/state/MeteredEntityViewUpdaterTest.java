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
