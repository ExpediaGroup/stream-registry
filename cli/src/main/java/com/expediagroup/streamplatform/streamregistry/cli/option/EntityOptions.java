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
package com.expediagroup.streamplatform.streamregistry.cli.option;

import lombok.Data;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

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
import com.expediagroup.streamplatform.streamregistry.state.model.specification.DefaultSpecification;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.Specification;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.StreamSpecification;

public interface EntityOptions<K extends Key<S>, S extends Specification> {
  K key();

  @Data
  class Domain implements EntityOptions<DomainKey, DefaultSpecification> {
    @Option(names = "--domain", required = true) private String domain;

    @Override
    public DomainKey key() {
      return new DomainKey(domain);
    }
  }

  @Data
  class Schema implements EntityOptions<SchemaKey, DefaultSpecification> {
    @Mixin private Domain domain;
    @Option(names = "--schema", required = true) private String schema;

    @Override
    public SchemaKey key() {
      return new SchemaKey(domain.key(), schema);
    }
  }

  @Data
  class Stream implements EntityOptions<StreamKey, StreamSpecification> {
    @Mixin Domain domain;
    @Option(names = "--stream", required = true) private String stream;
    @Option(names = "--version", required = true) private int version;

    @Override
    public StreamKey key() {
      return new StreamKey(domain.key(), stream, version);
    }
  }

  @Data
  class ApplyStream extends Stream {
    @Option(names = "--schemaDomain", required = true) private String schemaDomain;
    @Option(names = "--schema", required = true) private String schema;

    public SchemaKey schemaKey() {
      return new SchemaKey(new DomainKey(schemaDomain), schema);
    }
  }

  @Data
  class Zone implements EntityOptions<ZoneKey, DefaultSpecification> {
    @Option(names = "--zone", required = true) private String zone;

    @Override
    public ZoneKey key() {
      return new ZoneKey(zone);
    }
  }

  @Data
  class Infrastructure implements EntityOptions<InfrastructureKey, DefaultSpecification> {
    @Mixin private Zone zone;
    @Option(names = "--infrastructure", required = true) private String infrastructure;

    @Override
    public InfrastructureKey key() {
      return new InfrastructureKey(zone.key(), infrastructure);
    }
  }

  @Data
  class Producer implements EntityOptions<ProducerKey, DefaultSpecification> {
    @Mixin private Stream stream;
    @Mixin private Zone zone;
    @Option(names = "--producer", required = true) private String producer;

    @Override
    public ProducerKey key() {
      return new ProducerKey(stream.key(), zone.key(), producer);
    }
  }

  @Data
  class Consumer implements EntityOptions<ConsumerKey, DefaultSpecification> {
    @Mixin private Stream stream;
    @Mixin private Zone zone;
    @Option(names = "--consumer", required = true) private String consumer;

    public ConsumerKey key() {
      return new ConsumerKey(stream.key(), zone.key(), consumer);
    }
  }

  @Data
  class StreamBinding implements EntityOptions<StreamBindingKey, DefaultSpecification> {
    @Mixin private Stream stream;
    @Mixin private Infrastructure infrastructure;

    @Override
    public StreamBindingKey key() {
      return new StreamBindingKey(stream.key(), infrastructure.key());
    }
  }

  @Data
  class ProducerBinding implements EntityOptions<ProducerBindingKey, DefaultSpecification> {
    @Mixin private StreamBinding streamBinding;
    @Option(names = "--producer", required = true) private String producer;

    @Override
    public ProducerBindingKey key() {
      StreamBindingKey key = streamBinding.key();
      return new ProducerBindingKey(new ProducerKey(key.getStreamKey(), key.getInfrastructureKey().getZoneKey(), producer), key);
    }
  }

  @Data
  class ConsumerBinding implements EntityOptions<ConsumerBindingKey, DefaultSpecification> {
    @Mixin private StreamBinding streamBinding;
    @Option(names = "--consumer", required = true) private String consumer;

    @Override
    public ConsumerBindingKey key() {
      StreamBindingKey key = streamBinding.key();
      return new ConsumerBindingKey(new ConsumerKey(key.getStreamKey(), key.getInfrastructureKey().getZoneKey(), consumer), key);
    }
  }
}
