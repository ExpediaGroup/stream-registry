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
package com.expediagroup.streamplatform.streamregistry.cli.command.delete;

import static com.fasterxml.jackson.core.Version.unknownVersion;
import static com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.WRITE_DOC_START_MARKER;

import java.io.IOException;
import java.util.List;

import lombok.SneakyThrows;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

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
import com.expediagroup.streamplatform.streamregistry.state.model.specification.Specification;

public class Formatter {
  private static final ObjectMapper mapper = new ObjectMapper(new YAMLFactory().disable(WRITE_DOC_START_MARKER))
      .registerModule(new SimpleModule("keys", unknownVersion(), List.of(
          new DomainKeySerializer(),
          new SchemaKeySerializer(),
          new StreamKeySerializer(),
          new ZoneKeySerializer(),
          new InfrastructureKeySerializer(),
          new ProducerKeySerializer(),
          new ConsumerKeySerializer(),
          new StreamBindingKeySerializer(),
          new ProducerBindingKeySerializer(),
          new ConsumerBindingKeySerializer()
      )));

  @SneakyThrows
  public <S extends Specification> String format(Key<S> key) {
    return mapper.writeValueAsString(key);
  }

  private static abstract class NestedKeySerializer<K> extends StdSerializer<K> {
    private final String name;

    protected NestedKeySerializer(Class<K> kClass, String name) {
      super(kClass);
      this.name = name;
    }

    @Override
    public void serialize(K key, JsonGenerator g, SerializerProvider p) throws IOException {
      var localStartObject = !g.getOutputContext().inObject();
      if (localStartObject) {
        g.writeStartObject();
        g.writeFieldName(name);
        g.writeStartObject();
      }
      serialize(key, g);
      if (localStartObject) {
        g.writeEndObject();
        g.writeEndObject();
      }
    }

    protected abstract void serialize(K key, JsonGenerator g) throws IOException;
  }

  private static class DomainKeySerializer extends NestedKeySerializer<DomainKey> {
    protected DomainKeySerializer() {
      super(DomainKey.class, "domain");
    }

    @Override
    public void serialize(DomainKey key, JsonGenerator g) throws IOException {
      g.writeStringField("domain", key.getName());
    }
  }

  private static class SchemaKeySerializer extends NestedKeySerializer<SchemaKey> {
    protected SchemaKeySerializer() {
      super(SchemaKey.class, "schema");
    }

    @Override
    public void serialize(SchemaKey key, JsonGenerator g) throws IOException {
      g.writeObject(key.getDomainKey());
      g.writeStringField("schema", key.getName());
    }
  }

  private static class StreamKeySerializer extends NestedKeySerializer<StreamKey> {
    protected StreamKeySerializer() {
      super(StreamKey.class, "stream");
    }

    @Override
    public void serialize(StreamKey key, JsonGenerator g) throws IOException {
      g.writeObject(key.getDomainKey());
      g.writeStringField("stream", key.getName());
      g.writeNumberField("version", key.getVersion());
    }
  }

  private static class ZoneKeySerializer extends NestedKeySerializer<ZoneKey> {
    protected ZoneKeySerializer() {
      super(ZoneKey.class, "zone");
    }

    @Override
    public void serialize(ZoneKey key, JsonGenerator g) throws IOException {
      g.writeStringField("zone", key.getName());
    }
  }

  private static class InfrastructureKeySerializer extends NestedKeySerializer<InfrastructureKey> {
    protected InfrastructureKeySerializer() {
      super(InfrastructureKey.class, "infrastructure");
    }

    @Override
    public void serialize(InfrastructureKey key, JsonGenerator g) throws IOException {
      g.writeObject(key.getZoneKey());
      g.writeStringField("infrastructure", key.getName());
    }
  }

  private static class ProducerKeySerializer extends NestedKeySerializer<ProducerKey> {
    protected ProducerKeySerializer() {
      super(ProducerKey.class, "producer");
    }

    @Override
    public void serialize(ProducerKey key, JsonGenerator g) throws IOException {
      g.writeObject(key.getStreamKey());
      g.writeObject(key.getZoneKey());
      g.writeStringField("producer", key.getName());
    }
  }

  private static class ConsumerKeySerializer extends NestedKeySerializer<ConsumerKey> {
    protected ConsumerKeySerializer() {
      super(ConsumerKey.class, "consumer");
    }

    @Override
    public void serialize(ConsumerKey key, JsonGenerator g) throws IOException {
      g.writeObject(key.getStreamKey());
      g.writeObject(key.getZoneKey());
      g.writeStringField("consumer", key.getName());
    }
  }

  private static class StreamBindingKeySerializer extends NestedKeySerializer<StreamBindingKey> {
    protected StreamBindingKeySerializer() {
      super(StreamBindingKey.class, "streamBinding");
    }

    @Override
    public void serialize(StreamBindingKey key, JsonGenerator g) throws IOException {
      g.writeObject(key.getStreamKey());
      g.writeObject(key.getInfrastructureKey());
    }
  }

  private static class ProducerBindingKeySerializer extends NestedKeySerializer<ProducerBindingKey> {
    protected ProducerBindingKeySerializer() {
      super(ProducerBindingKey.class, "producerBinding");
    }

    @Override
    public void serialize(ProducerBindingKey key, JsonGenerator g) throws IOException {
      g.writeObject(key.getStreamBindingKey().getStreamKey());
      g.writeObject(key.getStreamBindingKey().getInfrastructureKey());
      g.writeStringField("producer", key.getProducerKey().getName());
    }
  }

  private static class ConsumerBindingKeySerializer extends NestedKeySerializer<ConsumerBindingKey> {
    protected ConsumerBindingKeySerializer() {
      super(ConsumerBindingKey.class, "consumerBinding");
    }

    @Override
    public void serialize(ConsumerBindingKey key, JsonGenerator g) throws IOException {
      g.writeObject(key.getStreamBindingKey().getStreamKey());
      g.writeObject(key.getStreamBindingKey().getInfrastructureKey());
      g.writeStringField("consumer", key.getConsumerKey().getName());
    }
  }
}
