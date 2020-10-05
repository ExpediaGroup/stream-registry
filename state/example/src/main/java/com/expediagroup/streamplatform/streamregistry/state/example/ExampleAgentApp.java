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
import com.expediagroup.streamplatform.streamregistry.state.graphql.Credentials;
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
      @Value("${streamRegistryUrl}") String streamRegistryUrl,
      @Value("${username}") String username,
      @Value("${password}") String password
  ) {
    return new DefaultApolloClientFactory(streamRegistryUrl, new Credentials(username, password)).create();
  }

  @Bean
  EventSender eventSender(ApolloClient apolloClient) {
    return new GraphQLEventSender(apolloClient);
  }

  @Bean
  EventReceiver eventReceiver(
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
  EntityView entityView(EventReceiver eventReceiver) {
    return new DefaultEntityView(eventReceiver);
  }
}
