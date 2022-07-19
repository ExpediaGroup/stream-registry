package com.expediagroup.streamplatform.streamregistry.state;

import com.expediagroup.streamplatform.streamregistry.state.model.Entity;

import java.util.Map;

public class DefaultEntityViewUpdaterTest extends EntityViewUpdaterTest {
  @Override
  public EntityViewUpdater entityViewUpdater(Map<Entity.Key<?>, StateValue> entities) {
    return new DefaultEntityViewUpdater(entities);
  }
}
