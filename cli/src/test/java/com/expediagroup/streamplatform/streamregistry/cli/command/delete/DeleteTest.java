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

import static com.google.common.collect.ObjectArrays.concat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import picocli.CommandLine;
import picocli.CommandLine.ParseResult;

import com.expediagroup.streamplatform.streamregistry.cli.command.delete.EntityClient.StreamKeyWithSchemaKey;
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

  private final String[] standardOptions = {"--bootstrapServers=bootstrapServers", "--schemaRegistryUrl=schemaRegistryUrl", "--streamRegistryUrl=streamRegistryUrl"};

  private final DomainKey domainKey = new DomainKey("domain");
  private final SchemaKey schemaKey = new SchemaKey(domainKey, "schema");
  private final StreamKey streamKey = new StreamKey(domainKey, "stream", 1);
  private final ZoneKey zoneKey = new ZoneKey("zone");
  private final InfrastructureKey infrastructureKey = new InfrastructureKey(zoneKey, "infrastructure");
  private final ProducerKey producerKey = new ProducerKey(streamKey, zoneKey, "producer");
  private final ConsumerKey consumerKey = new ConsumerKey(streamKey, zoneKey, "consumer");
  private final StreamBindingKey streamBindingKey = new StreamBindingKey(streamKey, infrastructureKey);
  private final ProducerBindingKey producerBindingKey = new ProducerBindingKey(producerKey, streamBindingKey);
  private final ConsumerBindingKey consumerBindingKey = new ConsumerBindingKey(consumerKey, streamBindingKey);

  @Mock private EntityClient client;
  @Mock private EntityDeleter deleter;
  private final StringPrintStream out = new StringPrintStream();
  private final StringPrintStream err = new StringPrintStream();

  @Test
  public void noEntitiesFound() {
    when(client.getDomainKeys("domain"))
        .thenReturn(List.of());

    run("domain",
        "--domain=domain");

    verifyNoInteractions(deleter);
  }

  @Test
  public void domain() {
    when(client.getDomainKeys("domain"))
        .thenReturn(List.of(domainKey));

    run("domain",
        "--domain=domain");

    verify(deleter).delete(domainKey);
  }

  @Test
  public void domainDryRun() {
    when(client.getDomainKeys("domain"))
        .thenReturn(List.of(domainKey));

    run("domain", "--dryRun",
        "--domain=domain");

    verifyNoInteractions(deleter);
  }

  @Test
  public void schema() {
    when(client.getSchemaKeys("domain", "schema"))
        .thenReturn(List.of(schemaKey));

    run("schema",
        "--domain=domain", "--schema=schema");

    verify(deleter).delete(schemaKey);
  }

  @Test
  public void schemaDryRun() {
    when(client.getSchemaKeys("domain", "schema"))
        .thenReturn(List.of(schemaKey));

    run("schema", "--dryRun",
        "--domain=domain", "--schema=schema");

    verifyNoInteractions(deleter);
  }

  @Test
  public void stream() {
    when(client.getStreamKeyWithSchemaKeys("domain", "stream", 1, null, null))
        .thenReturn(List.of(new StreamKeyWithSchemaKey(streamKey, schemaKey)));

    run("stream",
        "--domain=domain", "--stream=stream", "--version=1");

    verify(deleter).delete(streamKey);
  }

  @Test
  public void streamDryRun() {
    when(client.getStreamKeyWithSchemaKeys("domain", "stream", 1, null, null))
        .thenReturn(List.of(new StreamKeyWithSchemaKey(streamKey, schemaKey)));

    run("stream", "--dryRun",
        "--domain=domain", "--stream=stream", "--version=1");

    verifyNoInteractions(deleter);
  }

  @Test
  public void streamCascade() {
    when(client.getConsumerBindingKeys("domain", "stream", 1, null, null, null))
        .thenReturn(List.of(consumerBindingKey));
    when(client.getConsumerKeys("domain", "stream", 1, null, null))
        .thenReturn(List.of(consumerKey));
    when(client.getProducerBindingKeys("domain", "stream", 1, null, null, null))
        .thenReturn(List.of(producerBindingKey));
    when(client.getProducerKeys("domain", "stream", 1, null, null))
        .thenReturn(List.of(producerKey));
    when(client.getStreamBindingKeys("domain", "stream", 1, null, null))
        .thenReturn(List.of(streamBindingKey));
    when(client.getStreamKeyWithSchemaKeys("domain", "stream", 1, null, null))
        .thenReturn(List.of(new StreamKeyWithSchemaKey(streamKey, schemaKey)));

    run("stream", "--cascade",
        "--domain=domain", "--stream=stream", "--version=1");

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
  public void zone() {
    when(client.getZoneKeys("zone"))
        .thenReturn(List.of(zoneKey));

    run("zone",
        "--zone=zone");

    verify(deleter).delete(zoneKey);
  }

  @Test
  public void zoneDryRun() {
    when(client.getZoneKeys("zone"))
        .thenReturn(List.of(zoneKey));

    run("zone", "--dryRun",
        "--zone=zone");

    verifyNoInteractions(deleter);
  }

  @Test
  public void infrastructure() {
    when(client.getInfrastructureKeys("zone", "infrastructure"))
        .thenReturn(List.of(infrastructureKey));

    run("infrastructure",
        "--zone=zone", "--infrastructure=infrastructure");

    verify(deleter).delete(infrastructureKey);
  }

  @Test
  public void infrastructureDryRun() {
    when(client.getInfrastructureKeys("zone", "infrastructure"))
        .thenReturn(List.of(infrastructureKey));

    run("infrastructure", "--dryRun",
        "--zone=zone", "--infrastructure=infrastructure");

    verifyNoInteractions(deleter);
  }

  @Test
  public void producer() {
    when(client.getProducerKeys("domain", "stream", 1, "zone", "producer"))
        .thenReturn(List.of(producerKey));

    run("producer",
        "--domain=domain", "--stream=stream", "--version=1", "--zone=zone", "--producer=producer");

    verify(deleter).delete(producerKey);
  }

  @Test
  public void producerDryRun() {
    when(client.getProducerKeys("domain", "stream", 1, "zone", "producer"))
        .thenReturn(List.of(producerKey));

    run("producer", "--dryRun",
        "--domain=domain", "--stream=stream", "--version=1", "--zone=zone", "--producer=producer");

    verifyNoInteractions(deleter);
  }

  @Test
  public void consumer() {
    when(client.getConsumerKeys("domain", "stream", 1, "zone", "consumer"))
        .thenReturn(List.of(consumerKey));

    run("consumer",
        "--domain=domain", "--stream=stream", "--version=1", "--zone=zone", "--consumer=consumer");

    verify(deleter).delete(consumerKey);
  }

  @Test
  public void consumerDryRun() {
    when(client.getConsumerKeys("domain", "stream", 1, "zone", "consumer"))
        .thenReturn(List.of(consumerKey));

    run("consumer", "--dryRun",
        "--domain=domain", "--stream=stream", "--version=1", "--zone=zone", "--consumer=consumer");

    verifyNoInteractions(deleter);
  }

  @Test
  public void streamBinding() {
    when(client.getStreamBindingKeys("domain", "stream", 1, "zone", "infrastructure"))
        .thenReturn(List.of(streamBindingKey));

    run("streamBinding",
        "--domain=domain", "--stream=stream", "--version=1", "--zone=zone", "--infrastructure=infrastructure");

    verify(deleter).delete(streamBindingKey);
  }

  @Test
  public void streamBindingDryRun() {
    when(client.getStreamBindingKeys("domain", "stream", 1, "zone", "infrastructure"))
        .thenReturn(List.of(streamBindingKey));

    run("streamBinding", "--dryRun",
        "--domain=domain", "--stream=stream", "--version=1", "--zone=zone", "--infrastructure=infrastructure");

    verifyNoInteractions(deleter);
  }

  @Test
  public void producerBinding() {
    when(client.getProducerBindingKeys("domain", "stream", 1, "zone", "infrastructure", "producer"))
        .thenReturn(List.of(producerBindingKey));

    run("producerBinding",
        "--domain=domain", "--stream=stream", "--version=1", "--zone=zone", "--infrastructure=infrastructure", "--producer=producer");

    verify(deleter).delete(producerBindingKey);
  }

  @Test
  public void producerBindingDryRun() {
    when(client.getProducerBindingKeys("domain", "stream", 1, "zone", "infrastructure", "producer"))
        .thenReturn(List.of(producerBindingKey));

    run("producerBinding", "--dryRun",
        "--domain=domain", "--stream=stream", "--version=1", "--zone=zone", "--infrastructure=infrastructure", "--producer=producer");

    verifyNoInteractions(deleter);
  }

  @Test
  public void consumerBinding() {
    when(client.getConsumerBindingKeys("domain", "stream", 1, "zone", "infrastructure", "consumer"))
        .thenReturn(List.of(consumerBindingKey));

    run("consumerBinding",
        "--domain=domain", "--stream=stream", "--version=1", "--zone=zone", "--infrastructure=infrastructure", "--consumer=consumer");

    verify(deleter).delete(consumerBindingKey);
  }

  @Test
  public void consumerBindingDryRun() {
    when(client.getConsumerBindingKeys("domain", "stream", 1, "zone", "infrastructure", "consumer"))
        .thenReturn(List.of(consumerBindingKey));

    run("consumerBinding", "--dryRun",
        "--domain=domain", "--stream=stream", "--version=1", "--zone=zone", "--infrastructure=infrastructure", "--consumer=consumer");

    verifyNoInteractions(deleter);
  }

  private void run(String entity, String... args) {
    ParseResult result = underTest.parseArgs(concat(concat(entity, standardOptions), args, String.class));
    Delete.Base action = (Delete.Base) result.subcommand().commandSpec().userObject();
    action.run(out, err, client, deleter);
  }
}
