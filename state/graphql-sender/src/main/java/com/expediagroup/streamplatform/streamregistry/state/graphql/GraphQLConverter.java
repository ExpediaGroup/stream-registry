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
package com.expediagroup.streamplatform.streamregistry.state.graphql;

import static java.util.stream.Collectors.toList;

import java.util.Map;

import com.apollographql.apollo.api.InputType;
import com.apollographql.apollo.api.Mutation;

import com.expediagroup.streamplatform.streamregistry.state.graphql.type.ConsumerBindingKeyInput;
import com.expediagroup.streamplatform.streamregistry.state.graphql.type.ConsumerKeyInput;
import com.expediagroup.streamplatform.streamregistry.state.graphql.type.DomainKeyInput;
import com.expediagroup.streamplatform.streamregistry.state.graphql.type.InfrastructureKeyInput;
import com.expediagroup.streamplatform.streamregistry.state.graphql.type.ProducerBindingKeyInput;
import com.expediagroup.streamplatform.streamregistry.state.graphql.type.ProducerKeyInput;
import com.expediagroup.streamplatform.streamregistry.state.graphql.type.SchemaKeyInput;
import com.expediagroup.streamplatform.streamregistry.state.graphql.type.SpecificationInput;
import com.expediagroup.streamplatform.streamregistry.state.graphql.type.StatusInput;
import com.expediagroup.streamplatform.streamregistry.state.graphql.type.StreamBindingKeyInput;
import com.expediagroup.streamplatform.streamregistry.state.graphql.type.StreamKeyInput;
import com.expediagroup.streamplatform.streamregistry.state.graphql.type.TagInput;
import com.expediagroup.streamplatform.streamregistry.state.graphql.type.ZoneKeyInput;
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

class GraphQLConverter {

  private final Map<Class<? extends Entity.Key<?>>, Converter<?, ?, ?, ?, ?>> converters = Map.of(
      DomainKey.class, new DomainConverter(),
      SchemaKey.class, new SchemaConverter(),
      StreamKey.class, new StreamConverter(),
      ZoneKey.class, new ZoneConverter(),
      InfrastructureKey.class, new InfrastructureConverter(),
      ProducerKey.class, new ProducerConverter(),
      ConsumerKey.class, new ConsumerConverter(),
      StreamBindingKey.class, new StreamBindingConverter(),
      ProducerBindingKey.class, new ProducerBindingConverter(),
      ConsumerBindingKey.class, new ConsumerBindingConverter()
  );

  <K extends Entity.Key<S>, S extends Specification> Mutation<?, ?, ?> convert(Event<K, S> event) {
    var converter = (Converter<?, ?, ?, K, S>) converters.get(event.getKey().getClass());

    if (converter == null) {
      throw new IllegalStateException("Unknown key class " + event.getKey().getClass());
    }

    if (event instanceof SpecificationEvent) {
      return converter.convertSpecificationEvent((SpecificationEvent<K, S>) event);
    } else {
      return converter.convertStatusEvent((StatusEvent<K, S>) event);
    }
  }

  interface Converter<GK extends InputType, GSP extends Mutation<?, ?, ?>, GST extends Mutation<?, ?, ?>, K extends Entity.Key<S>, S extends Specification> {
    GK convertKey(K key);

    GSP convertSpecificationEvent(SpecificationEvent<K, S> event);

    default SpecificationInput convertSpecification(S specification) {
      return SpecificationInput.builder()
          .description(specification.getDescription())
          .tags(specification.getTags().stream()
              .map(tag -> TagInput.builder()
                  .name(tag.getName())
                  .value(tag.getValue())
                  .build())
              .collect(toList()))
          .type(specification.getType())
          .configuration(specification.getConfiguration())
          .build();
    }

    GST convertStatusEvent(StatusEvent<K, S> event);

    default StatusInput convertStatus(StatusEntry entry) {
      if (!"agentStatus".equals(entry.getName())) {
        throw new UnsupportedOperationException("Stream Registry currently only supports a single status called 'agentStatus'.");
      }
      return StatusInput.builder()
          .agentStatus(entry.getValue())
          .build();
    }
  }

  static class DomainConverter implements Converter<DomainKeyInput, DomainSpecificationMutation, DomainStatusMutation, DomainKey, DefaultSpecification> {
    @Override
    public DomainKeyInput convertKey(DomainKey key) {
      return DomainKeyInput.builder()
          .name(key.getName())
          .build();
    }

    @Override
    public DomainSpecificationMutation convertSpecificationEvent(SpecificationEvent<DomainKey, DefaultSpecification> event) {
      return DomainSpecificationMutation.builder()
          .key(convertKey(event.getKey()))
          .specification(convertSpecification(event.getSpecification()))
          .build();
    }

    @Override
    public DomainStatusMutation convertStatusEvent(StatusEvent<DomainKey, DefaultSpecification> event) {
      return DomainStatusMutation.builder()
          .key(convertKey(event.getKey()))
          .status(convertStatus(event.getStatusEntry()))
          .build();
    }
  }

  static class SchemaConverter implements Converter<SchemaKeyInput, SchemaSpecificationMutation, SchemaStatusMutation, SchemaKey, DefaultSpecification> {
    @Override
    public SchemaKeyInput convertKey(SchemaKey key) {
      return SchemaKeyInput.builder()
          .domain(key.getDomainKey().getName())
          .name(key.getName())
          .build();
    }

    @Override
    public SchemaSpecificationMutation convertSpecificationEvent(SpecificationEvent<SchemaKey, DefaultSpecification> event) {
      return SchemaSpecificationMutation.builder()
          .key(convertKey(event.getKey()))
          .specification(convertSpecification(event.getSpecification()))
          .build();
    }

    @Override
    public SchemaStatusMutation convertStatusEvent(StatusEvent<SchemaKey, DefaultSpecification> event) {
      return SchemaStatusMutation.builder()
          .key(convertKey(event.getKey()))
          .status(convertStatus(event.getStatusEntry()))
          .build();
    }
  }

  static class StreamConverter implements Converter<StreamKeyInput, StreamSpecificationMutation, StreamStatusMutation, StreamKey, StreamSpecification> {
    @Override
    public StreamKeyInput convertKey(StreamKey key) {
      return StreamKeyInput.builder()
          .domain(key.getDomainKey().getName())
          .name(key.getName())
          .version(key.getVersion())
          .build();
    }

    @Override
    public StreamSpecificationMutation convertSpecificationEvent(SpecificationEvent<StreamKey, StreamSpecification> event) {
      return StreamSpecificationMutation.builder()
          .key(convertKey(event.getKey()))
          .specification(convertSpecification(event.getSpecification()))
          .schemaKey(SchemaKeyInput.builder()
              .domain(event.getSpecification().getSchemaKey().getDomainKey().getName())
              .name(event.getSpecification().getSchemaKey().getName())
              .build())
          .build();
    }

    @Override
    public StreamStatusMutation convertStatusEvent(StatusEvent<StreamKey, StreamSpecification> event) {
      return StreamStatusMutation.builder()
          .key(convertKey(event.getKey()))
          .status(convertStatus(event.getStatusEntry()))
          .build();
    }
  }

  static class ZoneConverter implements Converter<ZoneKeyInput, ZoneSpecificationMutation, ZoneStatusMutation, ZoneKey, DefaultSpecification> {
    @Override
    public ZoneKeyInput convertKey(ZoneKey key) {
      return ZoneKeyInput.builder()
          .name(key.getName())
          .build();
    }

    @Override
    public ZoneSpecificationMutation convertSpecificationEvent(SpecificationEvent<ZoneKey, DefaultSpecification> event) {
      return ZoneSpecificationMutation.builder()
          .key(convertKey(event.getKey()))
          .specification(convertSpecification(event.getSpecification()))
          .build();
    }

    @Override
    public ZoneStatusMutation convertStatusEvent(StatusEvent<ZoneKey, DefaultSpecification> event) {
      return ZoneStatusMutation.builder()
          .key(convertKey(event.getKey()))
          .status(convertStatus(event.getStatusEntry()))
          .build();
    }
  }

  static class InfrastructureConverter implements Converter<InfrastructureKeyInput, InfrastructureSpecificationMutation, InfrastructureStatusMutation, InfrastructureKey, DefaultSpecification> {
    @Override
    public InfrastructureKeyInput convertKey(InfrastructureKey key) {
      return InfrastructureKeyInput.builder()
          .zone(key.getZoneKey().getName())
          .name(key.getName())
          .build();
    }

    @Override
    public InfrastructureSpecificationMutation convertSpecificationEvent(SpecificationEvent<InfrastructureKey, DefaultSpecification> event) {
      return InfrastructureSpecificationMutation.builder()
          .key(convertKey(event.getKey()))
          .specification(convertSpecification(event.getSpecification()))
          .build();
    }

    @Override
    public InfrastructureStatusMutation convertStatusEvent(StatusEvent<InfrastructureKey, DefaultSpecification> event) {
      return InfrastructureStatusMutation.builder()
          .key(convertKey(event.getKey()))
          .status(convertStatus(event.getStatusEntry()))
          .build();
    }
  }

  static class ProducerConverter implements Converter<ProducerKeyInput, ProducerSpecificationMutation, ProducerStatusMutation, ProducerKey, DefaultSpecification> {
    @Override
    public ProducerKeyInput convertKey(ProducerKey key) {
      return ProducerKeyInput.builder()
          .streamDomain(key.getStreamKey().getDomainKey().getName())
          .streamName(key.getStreamKey().getName())
          .streamVersion(key.getStreamKey().getVersion())
          .zone(key.getZoneKey().getName())
          .name(key.getName())
          .build();
    }

    @Override
    public ProducerSpecificationMutation convertSpecificationEvent(SpecificationEvent<ProducerKey, DefaultSpecification> event) {
      return ProducerSpecificationMutation.builder()
          .key(convertKey(event.getKey()))
          .specification(convertSpecification(event.getSpecification()))
          .build();
    }

    @Override
    public ProducerStatusMutation convertStatusEvent(StatusEvent<ProducerKey, DefaultSpecification> event) {
      return ProducerStatusMutation.builder()
          .key(convertKey(event.getKey()))
          .status(convertStatus(event.getStatusEntry()))
          .build();
    }
  }

  static class ConsumerConverter implements Converter<ConsumerKeyInput, ConsumerSpecificationMutation, ConsumerStatusMutation, ConsumerKey, DefaultSpecification> {
    @Override
    public ConsumerKeyInput convertKey(ConsumerKey key) {
      return ConsumerKeyInput.builder()
          .streamDomain(key.getStreamKey().getDomainKey().getName())
          .streamName(key.getStreamKey().getName())
          .streamVersion(key.getStreamKey().getVersion())
          .zone(key.getZoneKey().getName())
          .name(key.getName())
          .build();
    }

    @Override
    public ConsumerSpecificationMutation convertSpecificationEvent(SpecificationEvent<ConsumerKey, DefaultSpecification> event) {
      return ConsumerSpecificationMutation.builder()
          .key(convertKey(event.getKey()))
          .specification(convertSpecification(event.getSpecification()))
          .build();
    }

    @Override
    public ConsumerStatusMutation convertStatusEvent(StatusEvent<ConsumerKey, DefaultSpecification> event) {
      return ConsumerStatusMutation.builder()
          .key(convertKey(event.getKey()))
          .status(convertStatus(event.getStatusEntry()))
          .build();
    }
  }

  static class StreamBindingConverter implements Converter<StreamBindingKeyInput, StreamBindingSpecificationMutation, StreamBindingStatusMutation, StreamBindingKey, DefaultSpecification> {
    @Override
    public StreamBindingKeyInput convertKey(StreamBindingKey key) {
      return StreamBindingKeyInput.builder()
          .streamDomain(key.getStreamKey().getDomainKey().getName())
          .streamName(key.getStreamKey().getName())
          .streamVersion(key.getStreamKey().getVersion())
          .infrastructureZone(key.getInfrastructureKey().getZoneKey().getName())
          .infrastructureName(key.getInfrastructureKey().getName())
          .build();
    }

    @Override
    public StreamBindingSpecificationMutation convertSpecificationEvent(SpecificationEvent<StreamBindingKey, DefaultSpecification> event) {
      return StreamBindingSpecificationMutation.builder()
          .key(convertKey(event.getKey()))
          .specification(convertSpecification(event.getSpecification()))
          .build();
    }

    @Override
    public StreamBindingStatusMutation convertStatusEvent(StatusEvent<StreamBindingKey, DefaultSpecification> event) {
      return StreamBindingStatusMutation.builder()
          .key(convertKey(event.getKey()))
          .status(convertStatus(event.getStatusEntry()))
          .build();
    }
  }

  static class ProducerBindingConverter implements Converter<ProducerBindingKeyInput, ProducerBindingSpecificationMutation, ProducerBindingStatusMutation, ProducerBindingKey, DefaultSpecification> {
    @Override
    public ProducerBindingKeyInput convertKey(ProducerBindingKey key) {
      return ProducerBindingKeyInput.builder()
          .streamDomain(key.getStreamBindingKey().getStreamKey().getDomainKey().getName())
          .streamName(key.getStreamBindingKey().getStreamKey().getName())
          .streamVersion(key.getStreamBindingKey().getStreamKey().getVersion())
          .infrastructureZone(key.getStreamBindingKey().getInfrastructureKey().getZoneKey().getName())
          .infrastructureName(key.getStreamBindingKey().getInfrastructureKey().getName())
          .producerName(key.getProducerKey().getName())
          .build();
    }

    @Override
    public ProducerBindingSpecificationMutation convertSpecificationEvent(SpecificationEvent<ProducerBindingKey, DefaultSpecification> event) {
      return ProducerBindingSpecificationMutation.builder()
          .key(convertKey(event.getKey()))
          .specification(convertSpecification(event.getSpecification()))
          .build();
    }

    @Override
    public ProducerBindingStatusMutation convertStatusEvent(StatusEvent<ProducerBindingKey, DefaultSpecification> event) {
      return ProducerBindingStatusMutation.builder()
          .key(convertKey(event.getKey()))
          .status(convertStatus(event.getStatusEntry()))
          .build();
    }
  }

  static class ConsumerBindingConverter implements Converter<ConsumerBindingKeyInput, ConsumerBindingSpecificationMutation, ConsumerBindingStatusMutation, ConsumerBindingKey, DefaultSpecification> {
    @Override
    public ConsumerBindingKeyInput convertKey(ConsumerBindingKey key) {
      return ConsumerBindingKeyInput.builder()
          .streamDomain(key.getStreamBindingKey().getStreamKey().getDomainKey().getName())
          .streamName(key.getStreamBindingKey().getStreamKey().getName())
          .streamVersion(key.getStreamBindingKey().getStreamKey().getVersion())
          .infrastructureZone(key.getStreamBindingKey().getInfrastructureKey().getZoneKey().getName())
          .infrastructureName(key.getStreamBindingKey().getInfrastructureKey().getName())
          .consumerName(key.getConsumerKey().getName())
          .build();
    }

    @Override
    public ConsumerBindingSpecificationMutation convertSpecificationEvent(SpecificationEvent<ConsumerBindingKey, DefaultSpecification> event) {
      return ConsumerBindingSpecificationMutation.builder()
          .key(convertKey(event.getKey()))
          .specification(convertSpecification(event.getSpecification()))
          .build();
    }

    @Override
    public ConsumerBindingStatusMutation convertStatusEvent(StatusEvent<ConsumerBindingKey, DefaultSpecification> event) {
      return ConsumerBindingStatusMutation.builder()
          .key(convertKey(event.getKey()))
          .status(convertStatus(event.getStatusEntry()))
          .build();
    }
  }
}
