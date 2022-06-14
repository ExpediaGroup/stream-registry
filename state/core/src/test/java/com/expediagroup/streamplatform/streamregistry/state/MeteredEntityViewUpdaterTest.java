package com.expediagroup.streamplatform.streamregistry.state;

import com.expediagroup.streamplatform.streamregistry.state.model.Entity;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import java.util.Map;

public class MeteredEntityViewUpdaterTest extends EntityViewUpdaterTest {

  private MeterRegistry meterRegistry = new SimpleMeterRegistry();

  @Override
  public EntityViewUpdater entityViewUpdater(Map<Entity.Key<?>, StateValue> entities) {
    return new EntityViews.MeteredEntityViewUpdater(new DefaultEntityViewUpdater(entities), meterRegistry);
  }
}
