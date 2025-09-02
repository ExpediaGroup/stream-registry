/**
 * Copyright (C) 2018-2025 Expedia, Inc.
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
import static com.expediagroup.streamplatform.streamregistry.state.StateValue.deleted;
import static com.expediagroup.streamplatform.streamregistry.state.StateValue.existing;
import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.collection.IsMapWithSize.aMapWithSize;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.After;
import org.junit.Test;

import com.expediagroup.streamplatform.streamregistry.state.model.Entity;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.DomainKey;
import com.expediagroup.streamplatform.streamregistry.state.model.event.Event;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.DefaultSpecification;
import com.expediagroup.streamplatform.streamregistry.state.model.status.DefaultStatus;

import lombok.val;

public abstract class EntityViewUpdaterTest {

  private final Map<Entity.Key<?>, StateValue> entities = new HashMap<>();

  private final DefaultSpecification oldSpecification = specification.withDescription("old-description");
  private final DefaultStatus oldStatus = new DefaultStatus();
  private final Entity<DomainKey, DefaultSpecification> oldEntity = entity
    .withSpecification(oldSpecification)
    .withStatus(oldStatus);


  final EntityViewUpdater underTest = entityViewUpdater(entities);


  public abstract EntityViewUpdater entityViewUpdater(Map<Entity.Key<?>, StateValue> entities);

  @After
  public void clean() {
    entities.clear();
  }

  @Test
  public void newSpecification() {
    val result = underTest.update(specificationEvent);

    assertThat(result, is(nullValue()));
    assertThat(entities.get(key), is(existing(entity.withStatus(oldStatus))));
  }

  @Test
  public void updateSpecification() {
    entities.put(key, existing(oldEntity));
    val result = underTest.update(specificationEvent);

    assertThat(result, is(oldEntity));
    assertThat(entities.get(key), is(existing(entity.withStatus(oldStatus))));
  }

  @Test
  public void updateDeletedSpecification() {
    underTest.update(specificationEvent);
    underTest.update(specificationDeletionEvent);
    assertThat(entities, is(aMapWithSize(1)));
    assertThat(entities, hasEntry(key, deleted(entity.withStatus(oldStatus))));

    DefaultSpecification updatedSpecification = specification.withDescription(randomUUID().toString());
    underTest.update(Event.specification(key, updatedSpecification));

    assertThat(entities, is(aMapWithSize(1)));
    assertThat(entities, hasEntry(key, existing(oldEntity.withSpecification(updatedSpecification))));
  }

  @Test
  public void newStatusExistingEntity() {
    entities.put(key, existing(oldEntity));
    val result = underTest.update(statusEvent);

    assertThat(result, is(oldEntity));
    assertThat(entities.get(key), is(existing(oldEntity.withStatus(status))));
  }

  @Test
  public void statusNoEntity() {
    val result = underTest.update(statusEvent);

    assertThat(result, is(nullValue()));
    assertThat(entities, is(aMapWithSize(0)));
  }

  @Test
  public void updateStatusOfDeletedEvent() {
    underTest.update(specificationDeletionEvent);
    assertThat(entities, hasEntry(key, deleted(null)));

    val result = underTest.update(statusDeletionEvent);
    assertThat(result, is(nullValue()));
    assertThat(entities, hasEntry(key, deleted(null)));
  }

  @Test
  public void deleteSpecificationEvent() {
    val previousEntity = underTest.update(specificationEvent);

    assertThat(previousEntity, is(nullValue()));
    assertThat(entities, is(aMapWithSize(1)));
    assertThat(entities, hasEntry(key, existing(entity.withStatus(oldStatus))));

    val deletedEntity = underTest.update(specificationDeletionEvent);
    assertThat(deletedEntity, is(entity.withStatus(oldStatus)));
    assertThat(entities, is(aMapWithSize(1)));
    assertThat(entities, hasEntry(key, deleted(entity.withStatus(oldStatus))));
  }

  @Test
  public void doubleDeleteSpecificationEvent() {
    val previousEntity = underTest.update(specificationEvent);

    assertThat(previousEntity, is(nullValue()));
    assertThat(entities, is(aMapWithSize(1)));
    assertThat(entities, hasEntry(key, existing(entity.withStatus(oldStatus))));

    val deletedEntity = underTest.update(specificationDeletionEvent);
    assertThat(deletedEntity, is(entity.withStatus(oldStatus)));
    assertThat(entities, is(aMapWithSize(1)));
    assertThat(entities, hasEntry(key, deleted(entity.withStatus(oldStatus))));

    val doubleDeletedEntity = underTest.update(specificationDeletionEvent);
    assertThat(doubleDeletedEntity, is(entity.withStatus(oldStatus)));
    assertThat(entities, is(aMapWithSize(1)));
    assertThat(entities, hasEntry(key, deleted(entity.withStatus(oldStatus))));
  }

  @Test
  public void deleteStatusEvent() {
    entities.put(key, existing(oldEntity));

    val updatedStatusEventResult = underTest.update(statusEvent);
    assertThat(updatedStatusEventResult, is(oldEntity));
    assertThat(entities, is(aMapWithSize(1)));
    assertThat(entities, hasEntry(key, existing(oldEntity.withStatus(status))));

    val deletedStatusEventResult = underTest.update(statusDeletionEvent);
    assertThat(deletedStatusEventResult, is(oldEntity.withStatus(status)));
    assertThat(entities, is(aMapWithSize(1)));
    assertThat(entities, hasEntry(key, existing(oldEntity)));
  }

  @Test
  public void deleteSpecificationEventWithNoPreviousEntity() {
    val deletedEntity = underTest.update(specificationDeletionEvent);
    assertThat(deletedEntity, is(nullValue()));
    assertThat(entities, is(aMapWithSize(1)));
    assertThat(entities, hasEntry(key, deleted(null)));
  }

  @Test
  public void purgeNonExistingEntity() {
    val purgedEntity = underTest.purge(key);

    assertThat(purgedEntity.isPresent(), is(false));
    assertThat(entities, is(aMapWithSize(0)));
  }

  @Test
  public void purgeDeletedEntity() {
    entities.put(key, deleted(oldEntity));

    Optional<Entity<DomainKey, DefaultSpecification>> purgedEntity = underTest.purge(key);

    assertThat(purgedEntity.isPresent(), is(true));
    assertThat(purgedEntity.get(), is(oldEntity));
    assertThat(entities, is(aMapWithSize(0)));
  }

  @Test
  public void purgeNonDeletedEntity() {
    entities.put(key, existing(oldEntity));

    Optional<Entity<DomainKey, DefaultSpecification>> purgedEntity = underTest.purge(key);

    assertThat(purgedEntity.isPresent(), is(false));
    assertThat(entities, is(aMapWithSize(1)));
    assertThat(entities, hasEntry(key, existing(oldEntity)));
  }

  @Test
  public void purgeWithNullPreviousEntry() {
    underTest.update(specificationDeletionEvent);
    assertThat(entities, is(aMapWithSize(1)));
    assertThat(entities, hasEntry(key, deleted(null)));

    underTest.purge(key);

    assertThat(entities, is(aMapWithSize(0)));
  }

  @Test(expected = IllegalArgumentException.class)
  public void nullEvents() {
    underTest.update(null);
  }
}
