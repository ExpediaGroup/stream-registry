/**
 * Copyright (C) 2018-2024 Expedia, Inc.
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

import static com.expediagroup.streamplatform.streamregistry.state.SampleEntities.*;
import static com.expediagroup.streamplatform.streamregistry.state.SampleEntities.statusEvent;
import static com.expediagroup.streamplatform.streamregistry.state.StateValue.existing;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.collection.IsMapWithSize.aMapWithSize;
import static org.junit.Assert.*;

import java.util.Map;

import lombok.val;

import org.junit.Test;

import com.expediagroup.streamplatform.streamregistry.state.model.Entity;

public class DefaultEntityViewUpdaterTest extends EntityViewUpdaterTest {
  @Override
  public EntityViewUpdater entityViewUpdater(Map<Entity.Key<?>, StateValue> entities) {
    return new DefaultEntityViewUpdater(entities);
  }

  @Test
  public void statusNotPersistedForExistingEntityWhenEntityStatusDisabled() {
    EntityViewUpdater updater = new DefaultEntityViewUpdater(entities, false);

    entities.put(key, existing(oldEntity));
    val result = updater.update(statusEvent);

    assertEquals(oldEntity, result);
    assertEquals(oldStatus, entities.get(key).entity.getStatus());
  }

  @Test
  public void statusNotPersistedForMissingEntityWhenEntityStatusDisabled() {
    EntityViewUpdater updater = new DefaultEntityViewUpdater(entities, false);

    val result = updater.update(statusEvent);

    assertNull(result);
  }

  @Test
  public void statusNoEntity() {
    val result = underTest.update(statusEvent);

    assertThat(result, is(nullValue()));
    assertThat(entities, is(aMapWithSize(0)));
  }
}
