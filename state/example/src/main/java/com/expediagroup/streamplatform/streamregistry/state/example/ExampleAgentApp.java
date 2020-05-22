package com.expediagroup.streamplatform.streamregistry.state.example;

import com.apollographql.apollo.ApolloClient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.expediagroup.streamplatform.streamregistry.state.DefaultEntityView;
import com.expediagroup.streamplatform.streamregistry.state.EntityView;
import com.expediagroup.streamplatform.streamregistry.state.EventReceiver;
import com.expediagroup.streamplatform.streamregistry.state.EventSender;
import com.expediagroup.streamplatform.streamregistry.state.graphql.DefaultApolloClientFactory;
import com.expediagroup.streamplatform.streamregistry.state.graphql.GraphQLEventSender;
import com.expediagroup.streamplatform.streamregistry.state.kafka.KafkaEventReceiver;

@SpringBootApplication
public class ExampleAgentApp {
  public static void main(String[] args) {
    SpringApplication.run(ExampleAgentApp.class, args);
  }

  @Bean
  ApolloClient apolloClient(
      @Value("${streamRegistryUrl}") String streamRegistryUrl
  ) {
    return new DefaultApolloClientFactory(streamRegistryUrl).create();
  }

  @Bean
  EventSender eventSender(ApolloClient apolloClient) {
    return new GraphQLEventSender(apolloClient);
  }

  @Bean
  EventReceiver receiver(
      @Value("${bootstrapServers}") String bootstrapServers,
      @Value("${topic}") String topic,
      @Value("${groupId}") String groupId,
      @Value("${schemaRegistryUrl}") String schemaRegistryUrl
  ) {
    KafkaEventReceiver.Config receiverConfig = KafkaEventReceiver.Config.builder()
        .bootstrapServers(bootstrapServers)
        .topic(topic)
        .groupId(groupId)
        .schemaRegistryUrl(schemaRegistryUrl)
        .build();
    return new KafkaEventReceiver(receiverConfig);
  }

  @Bean
  EntityView entityView(EventReceiver receiver) {
    return new DefaultEntityView(receiver);
  }
}
