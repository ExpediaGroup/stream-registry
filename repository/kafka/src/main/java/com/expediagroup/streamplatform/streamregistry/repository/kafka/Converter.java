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
package com.expediagroup.streamplatform.streamregistry.repository.kafka;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

import com.fasterxml.jackson.databind.node.ObjectNode;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.model.Consumer;
import com.expediagroup.streamplatform.streamregistry.model.ConsumerBinding;
import com.expediagroup.streamplatform.streamregistry.model.Domain;
import com.expediagroup.streamplatform.streamregistry.model.Infrastructure;
import com.expediagroup.streamplatform.streamregistry.model.Process;
import com.expediagroup.streamplatform.streamregistry.model.ProcessBinding;
import com.expediagroup.streamplatform.streamregistry.model.ProcessInputStream;
import com.expediagroup.streamplatform.streamregistry.model.ProcessOutputStream;
import com.expediagroup.streamplatform.streamregistry.model.Producer;
import com.expediagroup.streamplatform.streamregistry.model.ProducerBinding;
import com.expediagroup.streamplatform.streamregistry.model.Schema;
import com.expediagroup.streamplatform.streamregistry.model.Security;
import com.expediagroup.streamplatform.streamregistry.model.Specification;
import com.expediagroup.streamplatform.streamregistry.model.Status;
import com.expediagroup.streamplatform.streamregistry.model.Stream;
import com.expediagroup.streamplatform.streamregistry.model.StreamBinding;
import com.expediagroup.streamplatform.streamregistry.model.Tag;
import com.expediagroup.streamplatform.streamregistry.model.Zone;
import com.expediagroup.streamplatform.streamregistry.model.keys.ConsumerBindingKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.ConsumerKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.DomainKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.InfrastructureKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.ProcessBindingKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.ProcessKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.ProducerBindingKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.ProducerKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.SchemaKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.StreamBindingKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.StreamKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.ZoneKey;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.DefaultSpecification;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.ProcessBindingSpecification;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.ProcessInputStreamBinding;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.ProcessOutputStreamBinding;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.ProcessSpecification;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.StreamSpecification;
import com.expediagroup.streamplatform.streamregistry.state.model.status.DefaultStatus;
import com.expediagroup.streamplatform.streamregistry.state.model.status.StatusEntry;

interface Converter<ME extends com.expediagroup.streamplatform.streamregistry.model.Entity<MK>, MK, SK extends Entity.Key<SS>, SS extends com.expediagroup.streamplatform.streamregistry.state.model.specification.Specification> {
  MK convertKey(SK key);

  Specification convertSpecification(SS specification);

  ME convertEntity(Entity<SK, SS> entity);

  SK convertKey(MK key);

  SS convertSpecification(ME entity);

  Entity<SK, SS> convertEntity(ME entity);

  interface EntityFactory<ME extends com.expediagroup.streamplatform.streamregistry.model.Entity<MK>, MK> {
    ME create(MK key, Specification specification, Status status);
  }

  abstract class BaseConverter<
      ME extends com.expediagroup.streamplatform.streamregistry.model.Entity<MK>,
      MK,
      SK extends Entity.Key<SS>,
      SS extends com.expediagroup.streamplatform.streamregistry.state.model.specification.Specification>
      implements Converter<ME, MK, SK, SS> {

    @Override
    public Specification convertSpecification(SS specification) {
      return new Specification(
          specification.getDescription(),
          specification.getTags().stream()
              .map(tag -> new com.expediagroup.streamplatform.streamregistry.model.Tag(tag.getName(), tag.getValue()))
              .collect(toList()),
          specification.getType(),
          specification.getConfiguration(),
          specification.getSecurity().entrySet().stream().map(entry ->
              new Security(
                  entry.getKey(),
                  entry.getValue().stream().map(principal -> new com.expediagroup.streamplatform.streamregistry.model.Principal(principal.getName())).collect(toList())
              )
          ).collect(Collectors.toList()),
          convertFunction(specification.getFunction())
      );
    }

    @Override
    public Entity<SK, SS> convertEntity(ME entity) {
      return new Entity<>(
          convertKey(entity.getKey()),
          convertSpecification(entity),
          convert(entity.getStatus())
      );
    }

    protected Status convertStatus(com.expediagroup.streamplatform.streamregistry.state.model.status.Status status) {
      return new Status(
          status.getValue("agentStatus")
      );
    }

    protected com.expediagroup.streamplatform.streamregistry.state.model.status.Status convert(Status status) {
      DefaultStatus defaultStatus = new DefaultStatus();
      ObjectNode value = Optional.ofNullable(status).map(Status::getObjectNode).orElse(null);
      if (value != null) {
        defaultStatus = (DefaultStatus) defaultStatus.with(new StatusEntry("agentStatus", value));
      }
      return defaultStatus;
    }

    protected List<com.expediagroup.streamplatform.streamregistry.state.model.specification.Tag> convertTags(List<Tag> tags) {
      return tags.stream()
          .map(tag -> new com.expediagroup.streamplatform.streamregistry.state.model.specification.Tag(tag.getName(), tag.getValue()))
          .collect(toList());
    }

    protected Map<String, List<com.expediagroup.streamplatform.streamregistry.state.model.specification.Principal>> convertSecurity(List<Security> security) {
      return security.stream()
        .collect(Collectors.toMap(
          Security::getRole,
          sec -> sec.getPrincipals().stream().map(principal -> new com.expediagroup.streamplatform.streamregistry.state.model.specification.Principal(principal.getName())).collect(toList())
        ));
    }

    protected String convertFunction(String function) {
      if (function == null)
        return "";

      return function;
    }
  }

  @RequiredArgsConstructor
  abstract class DefaultConverter<
      ME extends com.expediagroup.streamplatform.streamregistry.model.Entity<MK>,
      MK,
      SK extends Entity.Key<DefaultSpecification>>
      extends BaseConverter<ME, MK, SK, DefaultSpecification> {
    private final EntityFactory<ME, MK> entityFactory;

    @Override
    public ME convertEntity(Entity<SK, DefaultSpecification> entity) {
      return entityFactory.create(
          convertKey(entity.getKey()),
          convertSpecification(entity.getSpecification()),
          convertStatus(entity.getStatus())
      );
    }

    @Override
    public DefaultSpecification convertSpecification(ME entity) {
      Specification specification = entity.getSpecification();
      return new DefaultSpecification(
          specification.getDescription(),
          convertTags(specification.getTags()),
          specification.getType(),
          specification.getConfiguration(),
          convertSecurity(specification.getSecurity()),
          convertFunction(specification.getFunction())
      );
    }
  }

  @Component
  class DomainConverter extends DefaultConverter<Domain, DomainKey, Entity.DomainKey> {
    DomainConverter() {
      super(Domain::new);
    }

    @Override
    public DomainKey convertKey(Entity.DomainKey key) {
      return new DomainKey(
          key.getName()
      );
    }

    @Override
    public Entity.DomainKey convertKey(DomainKey key) {
      return new Entity.DomainKey(
          key.getName()
      );
    }
  }

  @Component
  class SchemaConverter extends DefaultConverter<Schema, SchemaKey, Entity.SchemaKey> {
    private final DomainConverter domainConverter;

    SchemaConverter(DomainConverter domainConverter) {
      super(Schema::new);
      this.domainConverter = domainConverter;
    }

    @Override
    public SchemaKey convertKey(Entity.SchemaKey key) {
      return new SchemaKey(
          key.getDomainKey().getName(),
          key.getName()
      );
    }

    @Override
    public Entity.SchemaKey convertKey(SchemaKey key) {
      return new Entity.SchemaKey(
          domainConverter.convertKey(key.getDomainKey()),
          key.getName()
      );
    }
  }

  @Component
  @RequiredArgsConstructor
  class StreamConverter extends BaseConverter<Stream, StreamKey, Entity.StreamKey, StreamSpecification> {
    private final DomainConverter domainConverter;
    private final SchemaConverter schemaConverter;

    @Override
    public StreamKey convertKey(Entity.StreamKey key) {
      return new StreamKey(
          key.getDomainKey().getName(),
          key.getName(),
          key.getVersion()
      );
    }

    @Override
    public Entity.StreamKey convertKey(StreamKey key) {
      return new Entity.StreamKey(
          domainConverter.convertKey(key.getDomainKey()),
          key.getName(),
          key.getVersion()
      );
    }

    @Override
    public StreamSpecification convertSpecification(Stream entity) {
      Specification specification = entity.getSpecification();
      return new StreamSpecification(
          specification.getDescription(),
          convertTags(specification.getTags()),
          specification.getType(),
          specification.getConfiguration(),
          convertSecurity(specification.getSecurity()),
          convertFunction(specification.getFunction()),
          schemaConverter.convertKey(entity.getSchemaKey())
      );
    }

    @Override
    public Stream convertEntity(Entity<Entity.StreamKey, StreamSpecification> entity) {
      return new Stream(
          convertKey(entity.getKey()),
          schemaConverter.convertKey(entity.getSpecification().getSchemaKey()),
          convertSpecification(entity.getSpecification()),
          convertStatus(entity.getStatus())
      );
    }
  }

  @Component
  @RequiredArgsConstructor
  class ProcessConverter extends BaseConverter<Process, ProcessKey, Entity.ProcessKey, ProcessSpecification> {
    private final DomainConverter domainConverter;
    private final ZoneConverter zoneConverter;
    private final StreamConverter streamConverter;

    @Override
    public ProcessKey convertKey(Entity.ProcessKey key) {
      return new ProcessKey(
        key.getDomainKey().getName(),
        key.getName()
      );
    }

    @Override
    public Entity.ProcessKey convertKey(ProcessKey key) {
      return new Entity.ProcessKey(
        domainConverter.convertKey(key.getDomainKey()),
        key.getName()
      );
    }

    @Override
    public ProcessSpecification convertSpecification(Process entity) {
      Specification specification = entity.getSpecification();
      return new ProcessSpecification(
        entity.getZones().stream().map(zoneConverter::convertKey).collect(Collectors.toList()),
        specification.getDescription(),
        convertTags(specification.getTags()),
        specification.getType(),
        specification.getConfiguration(),
        convertSecurity(specification.getSecurity()),
        convertFunction(specification.getFunction()),
        entity.getInputs().stream().map(input ->
          new com.expediagroup.streamplatform.streamregistry.state.model.specification.ProcessInputStream(
            streamConverter.convertKey(input.getStreamKey()),
            input.getConfiguration()
          )
        ).collect(Collectors.toList()),
        entity.getOutputs().stream().map(output ->
          new com.expediagroup.streamplatform.streamregistry.state.model.specification.ProcessOutputStream(
            streamConverter.convertKey(output.getStreamKey()),
            output.getConfiguration()
          )
        ).collect(Collectors.toList())
      );
    }

    @Override
    public Process convertEntity(Entity<Entity.ProcessKey, ProcessSpecification> entity) {
      return new Process(
        convertKey(entity.getKey()),
        convertSpecification(entity.getSpecification()),
        entity.getSpecification().getZones().stream().map(zone ->
          new ZoneKey(zone.getName())).collect(Collectors.toList()),
        entity.getSpecification().getInputs().stream().map(input ->
          new ProcessInputStream(
            streamConverter.convertKey(input.getStream()),
            input.getConfiguration()
          )
        ).collect(Collectors.toList()),
        entity.getSpecification().getOutputs().stream().map(output ->
          new ProcessOutputStream(
            streamConverter.convertKey(output.getStream()),
            output.getConfiguration()
          )
        ).collect(Collectors.toList()),
        convertStatus(entity.getStatus())
      );
    }
  }

  @Component
  class ZoneConverter extends DefaultConverter<Zone, ZoneKey, Entity.ZoneKey> {
    ZoneConverter() {
      super(Zone::new);
    }

    @Override
    public ZoneKey convertKey(Entity.ZoneKey key) {
      return new ZoneKey(
          key.getName()
      );
    }

    @Override
    public Entity.ZoneKey convertKey(ZoneKey key) {
      return new Entity.ZoneKey(
          key.getName()
      );
    }
  }

  @Component
  class InfrastructureConverter extends DefaultConverter<Infrastructure, InfrastructureKey, Entity.InfrastructureKey> {
    private final ZoneConverter zoneConverter;

    InfrastructureConverter(ZoneConverter zoneConverter) {
      super(Infrastructure::new);
      this.zoneConverter = zoneConverter;
    }

    @Override
    public InfrastructureKey convertKey(Entity.InfrastructureKey key) {
      return new InfrastructureKey(
          key.getZoneKey().getName(),
          key.getName()
      );
    }

    @Override
    public Entity.InfrastructureKey convertKey(InfrastructureKey key) {
      return new Entity.InfrastructureKey(
          zoneConverter.convertKey(key.getZoneKey()),
          key.getName()
      );
    }
  }

  @Component
  class ProducerConverter extends DefaultConverter<Producer, ProducerKey, Entity.ProducerKey> {
    private final StreamConverter streamConverter;
    private final ZoneConverter zoneConverter;

    ProducerConverter(StreamConverter streamConverter, ZoneConverter zoneConverter) {
      super(Producer::new);
      this.streamConverter = streamConverter;
      this.zoneConverter = zoneConverter;
    }

    @Override
    public ProducerKey convertKey(Entity.ProducerKey key) {
      return new ProducerKey(
          key.getStreamKey().getDomainKey().getName(),
          key.getStreamKey().getName(),
          key.getStreamKey().getVersion(),
          key.getZoneKey().getName(),
          key.getName()
      );
    }

    @Override
    public Entity.ProducerKey convertKey(ProducerKey key) {
      return new Entity.ProducerKey(
          streamConverter.convertKey(key.getStreamKey()),
          zoneConverter.convertKey(key.getZoneKey()),
          key.getName()
      );
    }
  }

  @Component
  class ConsumerConverter extends DefaultConverter<Consumer, ConsumerKey, Entity.ConsumerKey> {
    private final StreamConverter streamConverter;
    private final ZoneConverter zoneConverter;

    ConsumerConverter(StreamConverter streamConverter, ZoneConverter zoneConverter) {
      super(Consumer::new);
      this.streamConverter = streamConverter;
      this.zoneConverter = zoneConverter;
    }

    @Override
    public ConsumerKey convertKey(Entity.ConsumerKey key) {
      return new ConsumerKey(
          key.getStreamKey().getDomainKey().getName(),
          key.getStreamKey().getName(),
          key.getStreamKey().getVersion(),
          key.getZoneKey().getName(),
          key.getName()
      );
    }

    @Override
    public Entity.ConsumerKey convertKey(ConsumerKey key) {
      return new Entity.ConsumerKey(
          streamConverter.convertKey(key.getStreamKey()),
          zoneConverter.convertKey(key.getZoneKey()),
          key.getName()
      );
    }
  }

  @Component
  class StreamBindingConverter extends DefaultConverter<StreamBinding, StreamBindingKey, Entity.StreamBindingKey> {
    private final StreamConverter streamConverter;
    private final InfrastructureConverter infrastructureConverter;

    StreamBindingConverter(StreamConverter streamConverter, InfrastructureConverter infrastructureConverter) {
      super(StreamBinding::new);
      this.streamConverter = streamConverter;
      this.infrastructureConverter = infrastructureConverter;
    }

    @Override
    public StreamBindingKey convertKey(Entity.StreamBindingKey key) {
      return new StreamBindingKey(
          key.getStreamKey().getDomainKey().getName(),
          key.getStreamKey().getName(),
          key.getStreamKey().getVersion(),
          key.getInfrastructureKey().getZoneKey().getName(),
          key.getInfrastructureKey().getName()
      );
    }

    @Override
    public Entity.StreamBindingKey convertKey(StreamBindingKey key) {
      return new Entity.StreamBindingKey(
          streamConverter.convertKey(key.getStreamKey()),
          infrastructureConverter.convertKey(key.getInfrastructureKey())

      );
    }
  }

  @Component
  @RequiredArgsConstructor
  class ProcessBindingConverter extends BaseConverter<ProcessBinding, ProcessBindingKey, Entity.ProcessBindingKey, ProcessBindingSpecification> {
    private final DomainConverter domainConverter;
    private final ZoneConverter zoneConverter;
    private final StreamBindingConverter streamBindingConverter;

    @Override
    public ProcessBindingKey convertKey(Entity.ProcessBindingKey key) {
      return new ProcessBindingKey(
        key.getProcessKey().getDomainKey().getName(),
        key.getZoneKey().getName(),
        key.getProcessKey().getName()
      );
    }

    @Override
    public Entity.ProcessBindingKey convertKey(ProcessBindingKey key) {
      return new Entity.ProcessBindingKey(
        new Entity.ProcessKey(domainConverter.convertKey(key.getDomainKey()), key.getProcessName()),
        zoneConverter.convertKey(key.getZoneKey())
      );
    }

    @Override
    public ProcessBindingSpecification convertSpecification(ProcessBinding entity) {
      Specification specification = entity.getSpecification();
      return new ProcessBindingSpecification(
        zoneConverter.convertKey(entity.getZone()),
        specification.getDescription(),
        convertTags(specification.getTags()),
        specification.getType(),
        specification.getConfiguration(),
        convertSecurity(specification.getSecurity()),
        convertFunction(specification.getFunction()),
        entity.getInputs().stream().map(input -> new ProcessInputStreamBinding(
          streamBindingConverter.convertKey(input.getStreamBindingKey()),
          input.getConfiguration())).collect(Collectors.toList()),
        entity.getOutputs().stream().map(output -> new ProcessOutputStreamBinding(
          streamBindingConverter.convertKey(output.getStreamBindingKey()),
          output.getConfiguration())).collect(Collectors.toList())
        );
    }

    @Override
    public ProcessBinding convertEntity(Entity<Entity.ProcessBindingKey, ProcessBindingSpecification> entity) {
      return new ProcessBinding(
        convertKey(entity.getKey()),
        convertSpecification(entity.getSpecification()),
        zoneConverter.convertKey(entity.getKey().getZoneKey()),
        entity.getSpecification().getInputs().stream().map(input -> new com.expediagroup.streamplatform.streamregistry.model.ProcessInputStreamBinding(
          streamBindingConverter.convertKey(input.getStreamBindingKey()), input.getConfiguration()
        )).collect(Collectors.toList()),
        entity.getSpecification().getOutputs().stream().map(output -> new com.expediagroup.streamplatform.streamregistry.model.ProcessOutputStreamBinding(
          streamBindingConverter.convertKey(output.getStreamBindingKey()), output.getConfiguration()
        )).collect(Collectors.toList()),
        convertStatus(entity.getStatus())
      );
    }
  }

  @Component
  class ProducerBindingConverter extends DefaultConverter<ProducerBinding, ProducerBindingKey, Entity.ProducerBindingKey> {
    private final ProducerConverter producerConverter;
    private final StreamBindingConverter streamBindingConverter;

    ProducerBindingConverter(ProducerConverter producerConverter, StreamBindingConverter streamBindingConverter) {
      super(ProducerBinding::new);
      this.producerConverter = producerConverter;
      this.streamBindingConverter = streamBindingConverter;
    }

    @Override
    public ProducerBindingKey convertKey(Entity.ProducerBindingKey key) {
      return new ProducerBindingKey(
          key.getProducerKey().getStreamKey().getDomainKey().getName(),
          key.getProducerKey().getStreamKey().getName(),
          key.getProducerKey().getStreamKey().getVersion(),
          key.getStreamBindingKey().getInfrastructureKey().getZoneKey().getName(),
          key.getStreamBindingKey().getInfrastructureKey().getName(),
          key.getProducerKey().getName()
      );
    }

    @Override
    public Entity.ProducerBindingKey convertKey(ProducerBindingKey key) {
      return new Entity.ProducerBindingKey(
          producerConverter.convertKey(key.getProducerKey()),
          streamBindingConverter.convertKey(key.getStreamBindingKey())
      );
    }
  }

  @Component
  class ConsumerBindingConverter extends DefaultConverter<ConsumerBinding, ConsumerBindingKey, Entity.ConsumerBindingKey> {
    private final ConsumerConverter consumerConverter;
    private final StreamBindingConverter streamBindingConverter;

    ConsumerBindingConverter(ConsumerConverter consumerConverter, StreamBindingConverter streamBindingConverter) {
      super(ConsumerBinding::new);
      this.consumerConverter = consumerConverter;
      this.streamBindingConverter = streamBindingConverter;
    }

    @Override
    public ConsumerBindingKey convertKey(Entity.ConsumerBindingKey key) {
      return new ConsumerBindingKey(
          key.getConsumerKey().getStreamKey().getDomainKey().getName(),
          key.getConsumerKey().getStreamKey().getName(),
          key.getConsumerKey().getStreamKey().getVersion(),
          key.getStreamBindingKey().getInfrastructureKey().getZoneKey().getName(),
          key.getStreamBindingKey().getInfrastructureKey().getName(),
          key.getConsumerKey().getName()
      );
    }

    @Override
    public Entity.ConsumerBindingKey convertKey(ConsumerBindingKey key) {
      return new Entity.ConsumerBindingKey(
          consumerConverter.convertKey(key.getConsumerKey()),
          streamBindingConverter.convertKey(key.getStreamBindingKey())
      );
    }
  }
}
