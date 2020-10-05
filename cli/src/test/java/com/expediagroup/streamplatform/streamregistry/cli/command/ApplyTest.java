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

import static com.google.common.collect.ObjectArrays.concat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.junit.Test;

import picocli.CommandLine;
import picocli.CommandLine.ParseResult;

import com.expediagroup.streamplatform.streamregistry.cli.action.GraphQLEventSenderAction;
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
import com.expediagroup.streamplatform.streamregistry.state.model.specification.Specification;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.Tag;

public class ApplyTest {
  private final CommandLine underTest = new CommandLine(new Apply());

  private final String[] graphqlOptions = {"--streamRegistryUrl=streamRegistryUrl", "--streamRegistryUsername=streamRegistryUsername", "--streamRegistryPassword=streamRegistryPassword"};
  private final String[] configurationOptions = {"--description=description", "--tag=a:b", "--tag=c:d", "--type=type", "--configuration={\"e\":\"f\"}"};

  private final ObjectMapper mapper = new ObjectMapper();
  private final ObjectNode configuration = mapper.createObjectNode()
      .put("e", "f");

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

  @Test
  public void domain() {
    ParseResult result = parseArgs("domain",
        "--domain=domain");
    assertEvent(result, domainKey);
  }

  @Test
  public void schema() {
    ParseResult result = parseArgs("schema",
        "--domain=domain", "--schema=schema");
    assertEvent(result, schemaKey);
  }

  @Test
  public void stream() {
    ParseResult result = parseArgs("stream",
        "--domain=domain", "--stream=stream", "--version=1",
        "--schemaDomain=schemaDomain", "--schema=schema");
    assertEvent(result, streamKey);
  }

  @Test
  public void zone() {
    ParseResult result = underTest.parseArgs("zone",
        "--streamRegistryUrl=streamRegistryUrl",
        "--streamRegistryUsername=streamRegistryUsername",
        "--streamRegistryPassword=streamRegistryPassword",
        "--description=description", "--tag=a:b", "--tag=c:d", "--type=type", "--configuration={\"e\":\"f\"}",
        "--zone=zone");
    assertEvent(result, zoneKey);
  }

  @Test
  public void zone_withoutCredentials() {
    ParseResult result = underTest.parseArgs("zone",
        "--streamRegistryUrl=streamRegistryUrl",
        "--description=description", "--tag=a:b", "--tag=c:d", "--type=type", "--configuration={\"e\":\"f\"}",
        "--zone=zone");
    GraphQLEventSenderAction action = (GraphQLEventSenderAction) result.subcommand().commandSpec().userObject();
    List<Event<?, ?>> events = action.events();
    assertThat(events.size(), is(1));
  }

  @Test
  public void infrastructure() {
    ParseResult result = parseArgs("infrastructure",
        "--zone=zone", "--infrastructure=infrastructure");
    assertEvent(result, infrastructureKey);
  }

  @Test
  public void producer() {
    ParseResult result = parseArgs("producer",
        "--domain=domain", "--stream=stream", "--version=1", "--zone=zone", "--producer=producer");
    assertEvent(result, producerKey);
  }

  @Test
  public void consumer() {
    ParseResult result = parseArgs("consumer",
        "--domain=domain", "--stream=stream", "--version=1", "--zone=zone", "--consumer=consumer");
    assertEvent(result, consumerKey);
  }

  @Test
  public void streamBinding() {
    ParseResult result = parseArgs("streamBinding",
        "--domain=domain", "--stream=stream", "--version=1", "--zone=zone", "--infrastructure=infrastructure");
    assertEvent(result, streamBindingKey);
  }

  @Test
  public void producerBinding() {
    ParseResult result = parseArgs("producerBinding",
        "--domain=domain", "--stream=stream", "--version=1", "--zone=zone", "--infrastructure=infrastructure", "--producer=producer");
    assertEvent(result, producerBindingKey);
  }

  @Test
  public void consumerBinding() {
    ParseResult result = parseArgs("consumerBinding",
        "--domain=domain", "--stream=stream", "--version=1", "--zone=zone", "--infrastructure=infrastructure", "--consumer=consumer");
    assertEvent(result, consumerBindingKey);
  }

  private ParseResult parseArgs(String entity, String... args) {
    return underTest.parseArgs(concat(concat(concat(entity, graphqlOptions), configurationOptions, String.class), args, String.class));
  }

  private <S extends Specification> void assertEvent(ParseResult result, Entity.Key<S> key) {
    GraphQLEventSenderAction action = (GraphQLEventSenderAction) result.subcommand().commandSpec().userObject();
    assertGraphQLOptions(action);
    List<Event<?, ?>> events = action.events();
    assertThat(events.size(), is(1));
    SpecificationEvent<?, ?> event = (SpecificationEvent<?, ?>) events.get(0);
    assertThat(event.getKey(), is(key));
    Specification specification = event.getSpecification();
    assertThat(specification.getDescription(), is("description"));
    assertThat(specification.getTags(), is(List.of(new Tag("a", "b"), new Tag("c", "d"))));
    assertThat(specification.getType(), is("type"));
    assertThat(specification.getConfiguration(), is(configuration));

  }

  private void assertGraphQLOptions(GraphQLEventSenderAction action) {
    assertThat(action.getStreamRegistryUrl(), is("streamRegistryUrl"));
    assertThat(action.getStreamRegistryUsername(), is("streamRegistryUsername"));
    assertThat(action.getStreamRegistryPassword(), is("streamRegistryPassword"));
  }
}
