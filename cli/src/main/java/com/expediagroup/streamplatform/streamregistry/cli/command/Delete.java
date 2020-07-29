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
package com.expediagroup.streamplatform.streamregistry.cli.command;

import static com.expediagroup.streamplatform.streamregistry.state.model.event.Event.specificationDeletion;
import static com.expediagroup.streamplatform.streamregistry.state.model.event.Event.statusDeletion;

import java.util.List;

import lombok.Getter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import com.expediagroup.streamplatform.streamregistry.cli.action.KafkaEventSenderAction;
import com.expediagroup.streamplatform.streamregistry.cli.option.EntityOptions;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.ConsumerBindingKey;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.ConsumerKey;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.DomainKey;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.InfrastructureKey;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.Key;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.ProducerBindingKey;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.ProducerKey;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.SchemaKey;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.StreamBindingKey;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.StreamKey;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.ZoneKey;
import com.expediagroup.streamplatform.streamregistry.state.model.event.Event;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.DefaultSpecification;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.Specification;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.StreamSpecification;

@Command(name = "delete", subcommands = {
    Delete.Domain.class,
    Delete.Schema.class,
    Delete.Stream.class,
    Delete.Zone.class,
    Delete.Infrastructure.class,
    Delete.Producer.class,
    Delete.Consumer.class,
    Delete.StreamBinding.class,
    Delete.ProducerBinding.class,
    Delete.ConsumerBinding.class
})
public class Delete {
  static abstract class Base<K extends Key<S>, S extends Specification> extends KafkaEventSenderAction {
    @Option(names = "--statusName") @Getter String statusName;

    @Override
    public List<Event<?, ?>> events() {
      if (statusName == null) {
        return List.of(specificationDeletion(getEntityOptions().key()));
      } else {
        return List.of(statusDeletion(getEntityOptions().key(), statusName));
      }
    }

    protected abstract EntityOptions<K, S> getEntityOptions();
  }

  static abstract class Default<K extends Key<DefaultSpecification>> extends Base<K, DefaultSpecification> {}

  @Command(name = "domain")
  public static class Domain extends Default<DomainKey> {
    @Mixin @Getter EntityOptions.Domain entityOptions;
  }

  @Command(name = "schema")
  static class Schema extends Default<SchemaKey> {
    @Mixin @Getter EntityOptions.Schema entityOptions;
  }

  @Command(name = "stream")
  static class Stream extends Base<StreamKey, StreamSpecification> {
    @Mixin @Getter EntityOptions.Stream entityOptions;
  }

  @Command(name = "zone")
  static class Zone extends Default<ZoneKey> {
    @Mixin @Getter EntityOptions.Zone entityOptions;
  }

  @Command(name = "infrastructure")
  static class Infrastructure extends Default<InfrastructureKey> {
    @Mixin @Getter EntityOptions.Infrastructure entityOptions;
  }

  @Command(name = "producer")
  static class Producer extends Default<ProducerKey> {
    @Mixin @Getter EntityOptions.Producer entityOptions;
  }

  @Command(name = "consumer")
  static class Consumer extends Default<ConsumerKey> {
    @Mixin @Getter EntityOptions.Consumer entityOptions;
  }

  @Command(name = "streamBinding")
  static class StreamBinding extends Default<StreamBindingKey> {
    @Mixin @Getter EntityOptions.StreamBinding entityOptions;
  }

  @Command(name = "producerBinding")
  static class ProducerBinding extends Default<ProducerBindingKey> {
    @Mixin @Getter EntityOptions.ProducerBinding entityOptions;
  }

  @Command(name = "consumerBinding")
  static class ConsumerBinding extends Default<ConsumerBindingKey> {
    @Mixin @Getter EntityOptions.ConsumerBinding entityOptions;
  }
}
