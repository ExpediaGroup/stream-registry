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
package com.expediagroup.streamplatform.streamregistry.cli.command.delete;

import static com.google.common.collect.ObjectArrays.concat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import picocli.CommandLine;
import picocli.CommandLine.ParseResult;

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

@RunWith(MockitoJUnitRunner.Silent.class)
public class DeleteTest {
  private final CommandLine underTest = new CommandLine(new Delete());

  private final String[] standardOptions = {"--bootstrapServers=bootstrapServers", "--schemaRegistryUrl=schemaRegistryUrl", "--streamRegistryUrl=streamRegistryUrl", "--streamRegistryUsername=streamRegistryUsername", "--streamRegistryPassword=streamRegistryPassword"};

  private static final String DOMAIN = "domain";
  private static final String SCHEMA = "schema";
  private static final String STREAM = "stream";
  private static final int VERSION = 1;
  private static final String ZONE = "zone";
  private static final String INFRASTRUCTURE = "infrastructure";
  private static final String PRODUCER = "producer";
  private static final String CONSUMER = "consumer";
  private static final String STREAM_BINDING = "streamBinding";
  private static final String PRODUCER_BINDING = "producerBinding";
  private static final String CONSUMER_BINDING = "consumerBinding";

  private static final String DOMAIN_ARG = "--domain=" + DOMAIN;
  private static final String SCHEMA_ARG = "--schema=" + SCHEMA;
  private static final String STREAM_ARG = "--stream=" + STREAM;
  private static final String VERSION_ARG = "--version=" + VERSION;
  private static final String ZONE_ARG = "--zone=" + ZONE;
  private static final String INFRASTRUCTURE_ARG = "--infrastructure=" + INFRASTRUCTURE;
  private static final String PRODUCER_ARG = "--producer=" + PRODUCER;
  private static final String CONSUMER_ARG = "--consumer=" + CONSUMER;
  private static final String DRY_RUN_ARG = "--dryRun";
  private static final String CASCADE_ARG = "--cascade";

  private final DomainKey domainKey = new DomainKey(DOMAIN);
  private final SchemaKey schemaKey = new SchemaKey(domainKey, SCHEMA);
  private final StreamKey streamKey = new StreamKey(domainKey, STREAM, VERSION);
  private final StreamKey streamKey2 = new StreamKey(domainKey, STREAM, 2);
  private final ZoneKey zoneKey = new ZoneKey(ZONE);
  private final InfrastructureKey infrastructureKey = new InfrastructureKey(zoneKey, INFRASTRUCTURE);
  private final ProducerKey producerKey = new ProducerKey(streamKey, zoneKey, PRODUCER);
  private final ConsumerKey consumerKey = new ConsumerKey(streamKey, zoneKey, CONSUMER);
  private final StreamBindingKey streamBindingKey = new StreamBindingKey(streamKey, infrastructureKey);
  private final ProducerBindingKey producerBindingKey = new ProducerBindingKey(producerKey, streamBindingKey);
  private final ConsumerBindingKey consumerBindingKey = new ConsumerBindingKey(consumerKey, streamBindingKey);

  @Mock private EntityClient client;
  @Mock private EntityDeleter deleter;
  private final StringPrintStream out = StringPrintStream.create();
  private final StringPrintStream err = StringPrintStream.create();

  @Test
  public void noEntitiesFound() {
    when(client.getDomainKeys(DOMAIN))
        .thenReturn(Collections.emptyList());

    run(DOMAIN,
        DOMAIN_ARG);

    verifyNoInteractions(deleter);
  }

  @Test
  public void domain() {
    when(client.getDomainKeys(DOMAIN))
        .thenReturn(Collections.singletonList(domainKey));

    run(DOMAIN,
        DOMAIN_ARG);

    verify(deleter).delete(domainKey);
  }

  @Test
  public void domainDryRun() {
    when(client.getDomainKeys(DOMAIN))
        .thenReturn(Collections.singletonList(domainKey));

    run(DOMAIN, DRY_RUN_ARG,
        DOMAIN_ARG);

    verifyNoInteractions(deleter);
  }

  @Test
  public void schema() {
    when(client.getSchemaKeys(DOMAIN, SCHEMA))
        .thenReturn(Collections.singletonList(schemaKey));

    run(SCHEMA,
        DOMAIN_ARG, SCHEMA_ARG);

    verify(deleter).delete(schemaKey);
  }

  @Test
  public void schemaDryRun() {
    when(client.getSchemaKeys(DOMAIN, SCHEMA))
        .thenReturn(Collections.singletonList(schemaKey));

    run(SCHEMA, DRY_RUN_ARG,
        DOMAIN_ARG, SCHEMA_ARG);

    verifyNoInteractions(deleter);
  }

  @Test
  public void stream() {
    when(client.getStreamKeyWithSchemaKeys(DOMAIN, STREAM, VERSION, null, null))
        .thenReturn(Collections.singletonMap(streamKey, schemaKey));

    run(STREAM,
        DOMAIN_ARG, STREAM_ARG, VERSION_ARG);

    verify(deleter).delete(streamKey);
  }

  @Test
  public void streamDryRun() {
    when(client.getStreamKeyWithSchemaKeys(DOMAIN, STREAM, VERSION, null, null))
        .thenReturn(Collections.singletonMap(streamKey, schemaKey));

    run(STREAM, DRY_RUN_ARG,
        DOMAIN_ARG, STREAM_ARG, VERSION_ARG);

    verifyNoInteractions(deleter);
  }

  @Test
  public void streamCascade() {
    when(client.getConsumerBindingKeys(DOMAIN, STREAM, VERSION, null, null, null))
        .thenReturn(Collections.singletonList(consumerBindingKey));
    when(client.getConsumerKeys(DOMAIN, STREAM, VERSION, null, null))
        .thenReturn(Collections.singletonList(consumerKey));
    when(client.getProducerBindingKeys(DOMAIN, STREAM, VERSION, null, null, null))
        .thenReturn(Collections.singletonList(producerBindingKey));
    when(client.getProducerKeys(DOMAIN, STREAM, VERSION, null, null))
        .thenReturn(Collections.singletonList(producerKey));
    when(client.getStreamBindingKeys(DOMAIN, STREAM, VERSION, null, null))
        .thenReturn(Collections.singletonList(streamBindingKey));
    when(client.getStreamKeyWithSchemaKeys(DOMAIN, STREAM, VERSION, null, null))
        .thenReturn(Collections.singletonMap(streamKey, schemaKey));
    when(client.getStreamKeyWithSchemaKeys(null, null, null, DOMAIN, SCHEMA))
        .thenReturn(Collections.singletonMap(streamKey, schemaKey));

    run(STREAM, CASCADE_ARG,
        DOMAIN_ARG, STREAM_ARG, VERSION_ARG);

    InOrder inOrder = inOrder(deleter);
    inOrder.verify(deleter).delete(consumerBindingKey);
    inOrder.verify(deleter).delete(consumerKey);
    inOrder.verify(deleter).delete(producerBindingKey);
    inOrder.verify(deleter).delete(producerKey);
    inOrder.verify(deleter).delete(streamBindingKey);
    inOrder.verify(deleter).delete(streamKey);
    inOrder.verify(deleter).delete(schemaKey);
  }

  @Test
  public void streamCascadeReusedSchema() {
    when(client.getConsumerBindingKeys(DOMAIN, STREAM, VERSION, null, null, null))
        .thenReturn(Collections.singletonList(consumerBindingKey));
    when(client.getConsumerKeys(DOMAIN, STREAM, VERSION, null, null))
        .thenReturn(Collections.singletonList(consumerKey));
    when(client.getProducerBindingKeys(DOMAIN, STREAM, VERSION, null, null, null))
        .thenReturn(Collections.singletonList(producerBindingKey));
    when(client.getProducerKeys(DOMAIN, STREAM, VERSION, null, null))
        .thenReturn(Collections.singletonList(producerKey));
    when(client.getStreamBindingKeys(DOMAIN, STREAM, VERSION, null, null))
        .thenReturn(Collections.singletonList(streamBindingKey));
    when(client.getStreamKeyWithSchemaKeys(DOMAIN, STREAM, VERSION, null, null))
        .thenReturn(Collections.singletonMap(streamKey, schemaKey));
    when(client.getStreamKeyWithSchemaKeys(null, null, null, DOMAIN, SCHEMA))
        .thenReturn(new HashMap<StreamKey, SchemaKey>() {{
          put(streamKey, schemaKey);
          put(streamKey2, schemaKey);
        }});

    run(STREAM, CASCADE_ARG,
        DOMAIN_ARG, STREAM_ARG, VERSION_ARG);

    InOrder inOrder = inOrder(deleter);
    inOrder.verify(deleter).delete(consumerBindingKey);
    inOrder.verify(deleter).delete(consumerKey);
    inOrder.verify(deleter).delete(producerBindingKey);
    inOrder.verify(deleter).delete(producerKey);
    inOrder.verify(deleter).delete(streamBindingKey);
    inOrder.verify(deleter).delete(streamKey);
    inOrder.verify(deleter, never()).delete(schemaKey);
  }

  @Test
  public void zone() {
    when(client.getZoneKeys(ZONE))
        .thenReturn(Collections.singletonList(zoneKey));

    run(ZONE,
        ZONE_ARG);

    verify(deleter).delete(zoneKey);
  }

  @Test
  public void zoneDryRun() {
    when(client.getZoneKeys(ZONE))
        .thenReturn(Collections.singletonList(zoneKey));

    run(ZONE, DRY_RUN_ARG,
        ZONE_ARG);

    verifyNoInteractions(deleter);
  }

  @Test
  public void infrastructure() {
    when(client.getInfrastructureKeys(ZONE, INFRASTRUCTURE))
        .thenReturn(Collections.singletonList(infrastructureKey));

    run(INFRASTRUCTURE,
        ZONE_ARG, INFRASTRUCTURE_ARG);

    verify(deleter).delete(infrastructureKey);
  }

  @Test
  public void infrastructureDryRun() {
    when(client.getInfrastructureKeys(ZONE, INFRASTRUCTURE))
        .thenReturn(Collections.singletonList(infrastructureKey));

    run(INFRASTRUCTURE, DRY_RUN_ARG,
        ZONE_ARG, INFRASTRUCTURE_ARG);

    verifyNoInteractions(deleter);
  }

  @Test
  public void producer() {
    when(client.getProducerKeys(DOMAIN, STREAM, VERSION, ZONE, PRODUCER))
        .thenReturn(Collections.singletonList(producerKey));

    run(PRODUCER,
        DOMAIN_ARG, STREAM_ARG, VERSION_ARG, ZONE_ARG, PRODUCER_ARG);

    verify(deleter).delete(producerKey);
  }

  @Test
  public void producerDryRun() {
    when(client.getProducerKeys(DOMAIN, STREAM, VERSION, ZONE, PRODUCER))
        .thenReturn(Collections.singletonList(producerKey));

    run(PRODUCER, DRY_RUN_ARG,
        DOMAIN_ARG, STREAM_ARG, VERSION_ARG, ZONE_ARG, PRODUCER_ARG);

    verifyNoInteractions(deleter);
  }

  @Test
  public void consumer() {
    when(client.getConsumerKeys(DOMAIN, STREAM, VERSION, ZONE, CONSUMER))
        .thenReturn(Collections.singletonList(consumerKey));

    run(CONSUMER,
        DOMAIN_ARG, STREAM_ARG, VERSION_ARG, ZONE_ARG, CONSUMER_ARG);

    verify(deleter).delete(consumerKey);
  }

  @Test
  public void consumerDryRun() {
    when(client.getConsumerKeys(DOMAIN, STREAM, VERSION, ZONE, CONSUMER))
        .thenReturn(Collections.singletonList(consumerKey));

    run(CONSUMER, DRY_RUN_ARG,
        DOMAIN_ARG, STREAM_ARG, VERSION_ARG, ZONE_ARG, CONSUMER_ARG);

    verifyNoInteractions(deleter);
  }

  @Test
  public void streamBinding() {
    when(client.getStreamBindingKeys(DOMAIN, STREAM, VERSION, ZONE, INFRASTRUCTURE))
        .thenReturn(Collections.singletonList(streamBindingKey));

    run(STREAM_BINDING,
        DOMAIN_ARG, STREAM_ARG, VERSION_ARG, ZONE_ARG, INFRASTRUCTURE_ARG);

    verify(deleter).delete(streamBindingKey);
  }

  @Test
  public void streamBindingDryRun() {
    when(client.getStreamBindingKeys(DOMAIN, STREAM, VERSION, ZONE, INFRASTRUCTURE))
        .thenReturn(Collections.singletonList(streamBindingKey));

    run(STREAM_BINDING, DRY_RUN_ARG,
        DOMAIN_ARG, STREAM_ARG, VERSION_ARG, ZONE_ARG, INFRASTRUCTURE_ARG);

    verifyNoInteractions(deleter);
  }

  @Test
  public void producerBinding() {
    when(client.getProducerBindingKeys(DOMAIN, STREAM, VERSION, ZONE, INFRASTRUCTURE, PRODUCER))
        .thenReturn(Collections.singletonList(producerBindingKey));

    run(PRODUCER_BINDING,
        DOMAIN_ARG, STREAM_ARG, VERSION_ARG, ZONE_ARG, INFRASTRUCTURE_ARG, PRODUCER_ARG);

    verify(deleter).delete(producerBindingKey);
  }

  @Test
  public void producerBindingDryRun() {
    when(client.getProducerBindingKeys(DOMAIN, STREAM, VERSION, ZONE, INFRASTRUCTURE, PRODUCER))
        .thenReturn(Collections.singletonList(producerBindingKey));

    run(PRODUCER_BINDING, DRY_RUN_ARG,
        DOMAIN_ARG, STREAM_ARG, VERSION_ARG, ZONE_ARG, INFRASTRUCTURE_ARG, PRODUCER_ARG);

    verifyNoInteractions(deleter);
  }

  @Test
  public void consumerBinding() {
    when(client.getConsumerBindingKeys(DOMAIN, STREAM, VERSION, ZONE, INFRASTRUCTURE, CONSUMER))
        .thenReturn(Collections.singletonList(consumerBindingKey));

    run(CONSUMER_BINDING,
        DOMAIN_ARG, STREAM_ARG, VERSION_ARG, ZONE_ARG, INFRASTRUCTURE_ARG, CONSUMER_ARG);

    verify(deleter).delete(consumerBindingKey);
  }

  @Test
  public void consumerBindingDryRun() {
    when(client.getConsumerBindingKeys(DOMAIN, STREAM, VERSION, ZONE, INFRASTRUCTURE, CONSUMER))
        .thenReturn(Collections.singletonList(consumerBindingKey));

    run(CONSUMER_BINDING, DRY_RUN_ARG,
        DOMAIN_ARG, STREAM_ARG, VERSION_ARG, ZONE_ARG, INFRASTRUCTURE_ARG, CONSUMER_ARG);

    verifyNoInteractions(deleter);
  }

  private void run(String entity, String... args) {
    ParseResult result = underTest.parseArgs(concat(concat(entity, standardOptions), args, String.class));
    Delete.Base action = (Delete.Base) result.subcommand().commandSpec().userObject();
    action.run(out, err, client, deleter);
  }
}
