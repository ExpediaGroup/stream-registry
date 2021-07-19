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
package com.expediagroup.streamplatform.streamregistry.state.model;

import lombok.NonNull;
import lombok.Value;
import lombok.With;

import com.expediagroup.streamplatform.streamregistry.state.model.specification.DefaultSpecification;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.ProcessBindingSpecification;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.ProcessSpecification;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.Specification;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.StreamSpecification;
import com.expediagroup.streamplatform.streamregistry.state.model.status.Status;

@Value
public class Entity<K extends Entity.Key<S>, S extends Specification> {
  @NonNull K key;
  @With
  @NonNull S specification;
  @With
  @NonNull Status status;

  public interface Key<S extends Specification> {}

  @Value
  public static class DomainKey implements Entity.Key<DefaultSpecification> {
    @NonNull String name;
  }

  @Value
  public static class SchemaKey implements Entity.Key<DefaultSpecification> {
    @NonNull DomainKey domainKey;
    @NonNull String name;
  }

  @Value
  public static class StreamKey implements Entity.Key<StreamSpecification> {
    @NonNull DomainKey domainKey;
    @NonNull String name;
    int version;
  }

  @Value
  public static class ZoneKey implements Entity.Key<DefaultSpecification> {
    @NonNull String name;
  }

  @Value
  public static class InfrastructureKey implements Entity.Key<DefaultSpecification> {
    @NonNull ZoneKey zoneKey;
    @NonNull String name;
  }

  @Value
  public static class ProducerKey implements Entity.Key<DefaultSpecification> {
    @NonNull StreamKey streamKey;
    @NonNull ZoneKey zoneKey;
    @NonNull String name;
  }

  @Value
  public static class ConsumerKey implements Entity.Key<DefaultSpecification> {
    @NonNull StreamKey streamKey;
    @NonNull ZoneKey zoneKey;
    @NonNull String name;
  }

  @Value
  public static class ProcessKey implements Entity.Key<ProcessSpecification> {
    @NonNull DomainKey domainKey;
    @NonNull String name;
  }

  @Value
  public static class StreamBindingKey implements Entity.Key<DefaultSpecification> {
    @NonNull StreamKey streamKey;
    @NonNull InfrastructureKey infrastructureKey;
  }

  @Value
  public static class ProducerBindingKey implements Entity.Key<DefaultSpecification> {
    @NonNull ProducerKey producerKey;
    @NonNull StreamBindingKey streamBindingKey;
  }

  @Value
  public static class ConsumerBindingKey implements Entity.Key<DefaultSpecification> {
    @NonNull ConsumerKey consumerKey;
    @NonNull StreamBindingKey streamBindingKey;
  }

  @Value
  public static class ProcessBindingKey implements Entity.Key<ProcessBindingSpecification> {
    @NonNull ProcessKey processKey;
    @NonNull ZoneKey zoneKey;
  }
}
