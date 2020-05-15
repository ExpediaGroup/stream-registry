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
package com.expediagroup.streamplatform.streamregistry.state.avro;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker.Std;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.avro.specific.SpecificRecord;

import com.expediagroup.streamplatform.streamregistry.state.model.Entity;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.ConsumerBindingKey;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.ConsumerKey;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.DomainKey;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.InfrastructureKey;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.ProducerBindingKey;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.ProducerKey;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.SchemaKey;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.StreamBindingKey;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.StreamKey;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.ZoneKey;
import com.expediagroup.streamplatform.streamregistry.state.model.event.Event;
import com.expediagroup.streamplatform.streamregistry.state.model.event.SpecificationEvent;
import com.expediagroup.streamplatform.streamregistry.state.model.event.StatusEvent;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.DefaultSpecification;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.Specification;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.StreamSpecification;
import com.expediagroup.streamplatform.streamregistry.state.model.status.StatusEntry;

@RequiredArgsConstructor
public class AvroConverter {
  private final ObjectMapper mapper = new ObjectMapper()
      .setVisibility(new Std(NONE).withFieldVisibility(ANY))
      .registerModule(new AvroObjectModule());

  private final List<EntityConverter<?, ?>> entityConverters = List.of(
      new EntityConverter<>(DomainKey.class, DefaultSpecification.class,
          AvroDomainKey.class, AvroSpecification.class),
      new EntityConverter<>(SchemaKey.class, DefaultSpecification.class,
          AvroSchemaKey.class, AvroSpecification.class),
      new EntityConverter<>(StreamKey.class, StreamSpecification.class,
          AvroStreamKey.class, AvroStreamSpecification.class),
      new EntityConverter<>(ZoneKey.class, DefaultSpecification.class,
          AvroZoneKey.class, AvroSpecification.class),
      new EntityConverter<>(InfrastructureKey.class, DefaultSpecification.class,
          AvroInfrastructureKey.class, AvroSpecification.class),
      new EntityConverter<>(ProducerKey.class, DefaultSpecification.class,
          AvroProducerKey.class, AvroSpecification.class),
      new EntityConverter<>(ConsumerKey.class, DefaultSpecification.class,
          AvroConsumerKey.class, AvroSpecification.class),
      new EntityConverter<>(StreamBindingKey.class, DefaultSpecification.class,
          AvroStreamBindingKey.class, AvroSpecification.class),
      new EntityConverter<>(ProducerBindingKey.class, DefaultSpecification.class,
          AvroProducerBindingKey.class, AvroSpecification.class),
      new EntityConverter<>(ConsumerBindingKey.class, DefaultSpecification.class,
          AvroConsumerBindingKey.class, AvroSpecification.class)
  );

  private final Map<Class<? extends SpecificRecord>, ? extends EntityConverter<?, ?>> modelConverters = entityConverters
      .stream().collect(toMap(EntityConverter::getAvroKeyClass, c -> c));

  private final Map<Class<? extends Entity.Key<?>>, ? extends EntityConverter<?, ?>> avroConverters = entityConverters
      .stream().collect(toMap(EntityConverter::getModelKeyClass, c -> c));

  @RequiredArgsConstructor
  class EntityConverter<K extends Entity.Key<S>, S extends Specification> {
    @Getter
    @NonNull
    private final Class<K> modelKeyClass;
    @NonNull private final Class<S> modelSpecificationClass;
    @Getter
    @NonNull
    private final Class<? extends SpecificRecord> avroKeyClass;
    @NonNull private final Class<? extends SpecificRecord> avroSpecificationClass;

    private Event<?, ?> toModel(AvroSpecificationKey avroSpecificationKey, AvroSpecification avroSpecification) {
      var key = convertObject(avroSpecificationKey.getKey(), modelKeyClass);
      var specification = convertObject(avroSpecification, modelSpecificationClass);
      return Event.of(key, specification);
    }

    private Event<?, ?> toModel(AvroStatusKey avroStatusKey, AvroStatus avroStatus) {
      var key = convertObject(avroStatusKey.getKey(), modelKeyClass);
      var statusName = avroStatusKey.getStatusName();
      var statusValue = convertObject(avroStatus.getValue(), ObjectNode.class);
      return Event.of(key, new StatusEntry(statusName, statusValue));
    }

    private AvroEvent toAvro(SpecificationEvent<?, ?> event) {
      var key = convertObject(event.getKey(), avroKeyClass);
      var specification = convertObject(event.getSpecification(), avroSpecificationClass);
      return new AvroEvent(
          new AvroKey(new AvroSpecificationKey(key)),
          new AvroValue(specification)
      );
    }

    private AvroEvent toAvro(StatusEvent<?, ?> event) {
      var key = convertObject(event.getKey(), avroKeyClass);
      var statusName = event.getStatusEntry().getName();
      var statusValue = convertObject(event.getStatusEntry().getValue(), AvroObject.class);
      return new AvroEvent(
          new AvroKey(new AvroStatusKey(key, statusName)),
          new AvroValue(new AvroStatus(statusValue))
      );
    }
  }

  public Event<?, ?> toModel(AvroKey avroKey, AvroValue avroValue) {
    if (avroKey.getKey() instanceof AvroSpecificationKey) {
      return toModel((AvroSpecificationKey) avroKey.getKey(), (AvroSpecification) avroValue.getValue());
    } else {
      return toModel((AvroStatusKey) avroKey.getKey(), (AvroStatus) avroValue.getValue());
    }
  }

  private Event<?, ?> toModel(AvroSpecificationKey avroSpecificationKey, AvroSpecification avroSpecification) {
    return modelConverters(avroSpecificationKey.getKey()).toModel(avroSpecificationKey, avroSpecification);
  }

  private Event<?, ?> toModel(AvroStatusKey avroStatusKey, AvroStatus avroStatus) {
    return modelConverters(avroStatusKey.getKey()).toModel(avroStatusKey, avroStatus);
  }

  private EntityConverter<?, ?> modelConverters(Object key) {
    return requireNonNull(modelConverters.get(key.getClass()), () -> "Unknown key class " + key.getClass());
  }

  public AvroEvent toAvro(Event<?, ?> event) {
    if (event instanceof SpecificationEvent) {
      return toAvro((SpecificationEvent<?, ?>) event);
    } else {
      return toAvro((StatusEvent<?, ?>) event);
    }
  }

  private AvroEvent toAvro(SpecificationEvent<?, ?> event) {
    return avroConverter(event.getKey()).toAvro(event);
  }

  private AvroEvent toAvro(StatusEvent<?, ?> event) {
    return avroConverter(event.getKey()).toAvro(event);
  }

  private EntityConverter<?, ?> avroConverter(Object key) {
    return requireNonNull(avroConverters.get(key.getClass()), () -> "Unknown key class " + key.getClass());
  }

  protected <T> T convertObject(Object object, Class<T> tClass) {
    return mapper.convertValue(object, tClass);
  }
}
