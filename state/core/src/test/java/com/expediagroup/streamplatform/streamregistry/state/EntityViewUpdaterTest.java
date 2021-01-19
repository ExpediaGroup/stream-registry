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
import static com.expediagroup.streamplatform.streamregistry.state.SampleEntities.specification;
import static com.expediagroup.streamplatform.streamregistry.state.SampleEntities.specificationDeletionEvent;
import static com.expediagroup.streamplatform.streamregistry.state.SampleEntities.specificationEvent;
import static com.expediagroup.streamplatform.streamregistry.state.SampleEntities.status;
import static com.expediagroup.streamplatform.streamregistry.state.SampleEntities.statusDeletionEvent;
import static com.expediagroup.streamplatform.streamregistry.state.SampleEntities.statusEvent;
import static com.expediagroup.streamplatform.streamregistry.state.StateKey.deleted;
import static com.expediagroup.streamplatform.streamregistry.state.StateKey.existing;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.collection.IsMapContaining.hasKey;
import static org.hamcrest.collection.IsMapWithSize.aMapWithSize;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Test;

import com.expediagroup.streamplatform.streamregistry.state.model.Entity;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.DomainKey;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.DefaultSpecification;
import com.expediagroup.streamplatform.streamregistry.state.model.status.DefaultStatus;

public class EntityViewUpdaterTest {

  private final Map<StateKey, Entity<?, ?>> entities = new HashMap<>();

  private final EntityViewUpdater underTest = new EntityViewUpdater(entities);

  private final DefaultSpecification oldSpecification = specification.withDescription("old-description");
  private final DefaultStatus oldStatus = new DefaultStatus();
  private final Entity<DomainKey, DefaultSpecification> oldEntity = entity
      .withSpecification(oldSpecification)
      .withStatus(oldStatus);

  @After
  public void clean() {
    entities.clear();
  }

  @Test
  public void newSpecification() {
    var result = underTest.update(specificationEvent);

    assertThat(result, is(nullValue()));
    assertThat(entities.get(existing(key)), is(entity.withStatus(oldStatus)));
  }

  @Test
  public void updateSpecification() {
    entities.put(existing(key), oldEntity);
    var result = underTest.update(specificationEvent);

    assertThat(result, is(oldEntity));
    assertThat(entities.get(existing(key)), is(entity.withStatus(oldStatus)));
  }

  @Test
  public void newStatusExistingEntity() {
    entities.put(existing(key), oldEntity);
    var result = underTest.update(statusEvent);

    assertThat(result, is(oldEntity));
    assertThat(entities.get(existing(key)), is(oldEntity.withStatus(status)));
  }

  @Test
  public void statusNoEntity() {
    var result = underTest.update(statusEvent);

    assertThat(result, is(nullValue()));
    assertThat(entities.get(existing(key)), is(nullValue()));
  }

  @Test
  public void deleteSpecificationEvent() {
    var previousEntity = underTest.update(specificationEvent);

    assertThat(previousEntity, is(nullValue()));
    assertThat(entities.get(existing(key)), is(entity.withStatus(oldStatus)));
    assertThat(entities, hasEntry(existing(key), entity.withStatus(oldStatus)));
    assertThat(entities, is(aMapWithSize(1)));

    var deletedEntity = underTest.update(specificationDeletionEvent);
    assertThat(deletedEntity, is(entity.withStatus(oldStatus)));
    assertThat(entities.get(existing(key)), is(nullValue()));
    assertThat(entities, hasEntry(deleted(key), entity.withStatus(oldStatus)));
    assertThat(entities, is(aMapWithSize(1)));
  }

  @Test
  public void deleteStatusEvent() {
    entities.put(existing(key), oldEntity);

    var updatedStatusEventResult = underTest.update(statusEvent);
    assertThat(updatedStatusEventResult, is(oldEntity));
    assertThat(entities, hasEntry(existing(key), oldEntity.withStatus(status)));
    assertThat(entities, is(aMapWithSize(1)));

    var deletedStatusEventResult = underTest.update(statusDeletionEvent);
    assertThat(deletedStatusEventResult, is(oldEntity.withStatus(status)));
    assertThat(entities, hasEntry(existing(key), oldEntity));
    assertThat(entities.get(deleted(key)), is(nullValue()));
    assertThat(entities, not(hasKey(deleted(key))));
    assertThat(entities, is(aMapWithSize(1)));
  }

  // This would happen when an agent bootstraps after being offline and the backing kafka topic has compacted
  @Test
  public void deleteSpecificationEventWithNoPreviousEntity() {
    var deletedEntity = underTest.update(specificationDeletionEvent);
    assertThat(deletedEntity, is(nullValue()));
    assertThat(entities.get(deleted(key)), is(nullValue()));
    assertThat(entities, hasKey(deleted(key)));
    assertThat(entities, not(hasKey(existing(key))));
    assertThat(entities, is(aMapWithSize(1)));
  }

  @Test
  public void purgeNonExistingEntity() {
    var purgedEntity = underTest.purge(key);

    assertThat(purgedEntity.isEmpty(), is(true));
    assertThat(entities, is(aMapWithSize(0)));
  }

  @Test
  public void purgeDeletedEntity() {
    entities.put(deleted(key), oldEntity);

    var purgedEntity = underTest.purge(key);

    assertThat(purgedEntity.isPresent(), is(true));
    assertThat(purgedEntity.get(), is(oldEntity));
    assertThat(entities, is(aMapWithSize(0)));
  }

  @Test
  public void purgeNonDeletedEntity() {
    entities.put(existing(key), oldEntity);

    var purgedEntity = underTest.purge(key);

    assertThat(purgedEntity.isEmpty(), is(true));
    assertThat(entities, hasEntry(existing(key), oldEntity));
    assertThat(entities, is(aMapWithSize(1)));
  }
}
