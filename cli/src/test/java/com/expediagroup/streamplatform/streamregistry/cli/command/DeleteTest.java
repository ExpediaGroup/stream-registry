package com.expediagroup.streamplatform.streamregistry.cli.command;

import static com.google.common.collect.ObjectArrays.concat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;

import com.expediagroup.streamplatform.streamregistry.cli.action.KafkaEventSenderAction;
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
import com.expediagroup.streamplatform.streamregistry.state.model.event.SpecificationDeletionEvent;
import com.expediagroup.streamplatform.streamregistry.state.model.event.StatusDeletionEvent;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.Specification;

import picocli.CommandLine;
import picocli.CommandLine.ParseResult;

public class DeleteTest {
  private final CommandLine underTest = new CommandLine(new Delete());

  private final String[] kafkaOptions = {"--bootstrapServers=bootstrapServers", "--topic=topic", "--schemaRegistryUrl=schemaRegistryUrl"};

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
    assertSpecificationDeletion(result, domainKey);
  }

  @Test
  public void domainStatus() {
    ParseResult result = parseArgs("domain",
        "--domain=domain",
        "--statusName=agentStatus");
    assertStatusDeletion(result, domainKey);
  }

  @Test
  public void schema() {
    ParseResult result = parseArgs("schema",
        "--domain=domain", "--schema=schema");
    assertSpecificationDeletion(result, schemaKey);
  }

  @Test
  public void schemaStatus() {
    ParseResult result = parseArgs("schema",
        "--domain=domain", "--schema=schema",
        "--statusName=agentStatus");
    assertStatusDeletion(result, schemaKey);
  }

  @Test
  public void stream() {
    ParseResult result = parseArgs("stream",
        "--domain=domain", "--stream=stream", "--version=1");
    assertSpecificationDeletion(result, streamKey);
  }

  @Test
  public void streamStatus() {
    ParseResult result = parseArgs("stream",
        "--domain=domain", "--stream=stream", "--version=1",
        "--statusName=agentStatus");
    assertStatusDeletion(result, streamKey);
  }

  @Test
  public void zone() {
    ParseResult result = parseArgs("zone",
        "--zone=zone");
    assertSpecificationDeletion(result, zoneKey);
  }

  @Test
  public void zoneStatus() {
    ParseResult result = parseArgs("zone",
        "--zone=zone",
        "--statusName=agentStatus");
    assertStatusDeletion(result, zoneKey);
  }

  @Test
  public void infrastructure() {
    ParseResult result = parseArgs("infrastructure",
        "--zone=zone", "--infrastructure=infrastructure");
    assertSpecificationDeletion(result, infrastructureKey);
  }

  @Test
  public void infrastructureStatus() {
    ParseResult result = parseArgs("infrastructure",
        "--zone=zone", "--infrastructure=infrastructure",
        "--statusName=agentStatus");
    assertStatusDeletion(result, infrastructureKey);
  }

  @Test
  public void producer() {
    ParseResult result = parseArgs("producer",
        "--domain=domain", "--stream=stream", "--version=1", "--zone=zone", "--producer=producer");
    assertSpecificationDeletion(result, producerKey);
  }

  @Test
  public void producerStatus() {
    ParseResult result = parseArgs("producer",
        "--domain=domain", "--stream=stream", "--version=1", "--zone=zone", "--producer=producer",
        "--statusName=agentStatus");
    assertStatusDeletion(result, producerKey);
  }

  @Test
  public void consumer() {
    ParseResult result = parseArgs("consumer",
        "--domain=domain", "--stream=stream", "--version=1", "--zone=zone", "--consumer=consumer");
    assertSpecificationDeletion(result, consumerKey);
  }

  @Test
  public void consumerStatus() {
    ParseResult result = parseArgs("consumer",
        "--domain=domain", "--stream=stream", "--version=1", "--zone=zone", "--consumer=consumer",
        "--statusName=agentStatus");
    assertStatusDeletion(result, consumerKey);
  }

  @Test
  public void streamBinding() {
    ParseResult result = parseArgs("streamBinding",
        "--domain=domain", "--stream=stream", "--version=1", "--zone=zone", "--infrastructure=infrastructure");
    assertSpecificationDeletion(result, streamBindingKey);
  }

  @Test
  public void streamBindingStatus() {
    ParseResult result = parseArgs("streamBinding",
        "--domain=domain", "--stream=stream", "--version=1", "--zone=zone", "--infrastructure=infrastructure",
        "--statusName=agentStatus");
    assertStatusDeletion(result, streamBindingKey);
  }

  @Test
  public void producerBinding() {
    ParseResult result = parseArgs("producerBinding",
        "--domain=domain", "--stream=stream", "--version=1", "--zone=zone", "--infrastructure=infrastructure", "--producer=producer");
    assertSpecificationDeletion(result, producerBindingKey);
  }

  @Test
  public void producerBindingStatus() {
    ParseResult result = parseArgs("producerBinding",
        "--domain=domain", "--stream=stream", "--version=1", "--zone=zone", "--infrastructure=infrastructure", "--producer=producer",
        "--statusName=agentStatus");
    assertStatusDeletion(result, producerBindingKey);
  }

  @Test
  public void consumerBinding() {
    ParseResult result = parseArgs("consumerBinding",
        "--domain=domain", "--stream=stream", "--version=1", "--zone=zone", "--infrastructure=infrastructure", "--consumer=consumer");
    assertSpecificationDeletion(result, consumerBindingKey);
  }

  @Test
  public void consumerBindingStatus() {
    ParseResult result = parseArgs("consumerBinding",
        "--domain=domain", "--stream=stream", "--version=1", "--zone=zone", "--infrastructure=infrastructure", "--consumer=consumer",
        "--statusName=agentStatus");
    assertStatusDeletion(result, consumerBindingKey);
  }

  private ParseResult parseArgs(String entity, String... args) {
    return underTest.parseArgs(concat(concat(entity, kafkaOptions), args, String.class));
  }

  private <S extends Specification> void assertSpecificationDeletion(ParseResult result, Key<S> key) {
    assertEvent(result, key, SpecificationDeletionEvent.class);
  }

  private <S extends Specification> void assertStatusDeletion(ParseResult result, Key<S> key) {
    assertEvent(result, key, StatusDeletionEvent.class);
  }

  private <S extends Specification> void assertEvent(ParseResult result, Key<S> key, Class<?> eventClass) {
    KafkaEventSenderAction action = (KafkaEventSenderAction) result.subcommand().commandSpec().userObject();
    assertKafkaOptions(action);
    List<Event<?, ?>> events = action.events();
    assertThat(events.size(), is(1));
    Event<?, ?> event = events.get(0);
    assertThat(event, is(instanceOf(eventClass)));
    assertThat(event.getKey(), is(key));
  }

  private void assertKafkaOptions(KafkaEventSenderAction action) {
    assertThat(action.getBootstrapServers(), is("bootstrapServers"));
    assertThat(action.getTopic(), is("topic"));
    assertThat(action.getSchemaRegistryUrl(), is("schemaRegistryUrl"));
  }
}
