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

import static com.expediagroup.streamplatform.streamregistry.state.SampleEntities.entity;
import static com.expediagroup.streamplatform.streamregistry.state.SampleEntities.key;
import static com.expediagroup.streamplatform.streamregistry.state.SampleEntities.specification;
import static com.expediagroup.streamplatform.streamregistry.state.SampleEntities.specificationEvent;
import static com.expediagroup.streamplatform.streamregistry.state.SampleEntities.status;
import static com.expediagroup.streamplatform.streamregistry.state.SampleEntities.statusEvent;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.expediagroup.streamplatform.streamregistry.state.model.Entity;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.DomainKey;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.DefaultSpecification;
import com.expediagroup.streamplatform.streamregistry.state.model.status.DefaultStatus;

public class EntityViewUpdaterTest {

  private final Map<Entity.Key<?>, Entity<?, ?>> entities = new HashMap<>();

  private final EntityViewUpdater underTest = new EntityViewUpdater(entities);

  private final DefaultSpecification oldSpecification = specification.withDescription("old-description");
  private final DefaultStatus oldStatus = new DefaultStatus();
  private final Entity<DomainKey, DefaultSpecification> oldEntity = entity
      .withSpecification(oldSpecification)
      .withStatus(oldStatus);

  @Test
  public void newSpecification() {
    var result = underTest.update(specificationEvent);

    assertThat(result, is(nullValue()));
    assertThat(entities.get(key), is(entity.withStatus(oldStatus)));
  }

  @Test
  public void updateSpecification() {
    entities.put(key, oldEntity);
    var result = underTest.update(specificationEvent);

    assertThat(result, is(oldEntity));
    assertThat(entities.get(key), is(entity.withStatus(oldStatus)));
  }

  @Test
  public void newStatusExistingEntity() {
    entities.put(key, oldEntity);
    var result = underTest.update(statusEvent);

    assertThat(result, is(oldEntity));
    assertThat(entities.get(key), is(oldEntity.withStatus(status)));
  }

  @Test
  public void statusNoEntity() {
    var result = underTest.update(statusEvent);

    assertThat(result, is(nullValue()));
    assertThat(entities.get(key), is(nullValue()));
  }
}
