package com.expediagroup.streamplatform.streamregistry.state.it;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.junit.Rule;
import org.junit.Test;
import org.testcontainers.containers.KafkaContainer;

import com.expediagroup.streamplatform.streamregistry.state.DefaultEntityView;
import com.expediagroup.streamplatform.streamregistry.state.DefaultEventCorrelator;
import com.expediagroup.streamplatform.streamregistry.state.EntityView;
import com.expediagroup.streamplatform.streamregistry.state.EntityViewListener;
import com.expediagroup.streamplatform.streamregistry.state.kafka.KafkaEventReceiver;
import com.expediagroup.streamplatform.streamregistry.state.kafka.KafkaEventSender;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.DomainKey;
import com.expediagroup.streamplatform.streamregistry.state.model.event.Event;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.DefaultSpecification;

public class StateIT {
  @Rule
  public KafkaContainer kafka = new KafkaContainer();

  private final ObjectMapper mapper = new ObjectMapper();
  private final ObjectNode configuration = mapper.createObjectNode();
  private final DomainKey key = new DomainKey("domain");
  private final DefaultSpecification specification = new DefaultSpecification("description", List.of(), "type", configuration);
  private final Event<DomainKey, DefaultSpecification> event = Event.of(key, specification);

  @Test
  public void test() throws Exception {
    var topic = "topic";
    var schemaRegistryUrl = "mock://schemas";

    var correlator = new DefaultEventCorrelator();

    var receiver = new KafkaEventReceiver(KafkaEventReceiver.Config.builder()
        .bootstrapServers(kafka.getBootstrapServers())
        .schemaRegistryUrl(schemaRegistryUrl)
        .topic(topic)
        .groupId("groupId")
        .build(), correlator);

    var kafkaSender = new KafkaEventSender(KafkaEventSender.Config.builder()
        .bootstrapServers(kafka.getBootstrapServers())
        .schemaRegistryUrl(schemaRegistryUrl)
        .topic(topic)
        .build(), correlator);

    EntityView view = new DefaultEntityView(receiver);
    var listener = mock(EntityViewListener.class);
    view.load(listener).join();

    kafkaSender.send(event).join();
    verify(listener).onEvent(null, event);

    var entity = view.get(key);
    assertThat(entity.isPresent(), is(true));
    assertThat(entity.get().getSpecification(), is(specification));
  }
}
