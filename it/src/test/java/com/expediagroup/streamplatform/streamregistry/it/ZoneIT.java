/**
 * Copyright (C) 2018-2019 Expedia, Inc.
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
package com.expediagroup.streamplatform.streamregistry.it;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.apache.kafka.streams.integration.utils.EmbeddedKafkaCluster;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import com.expediagroup.streamplatform.streamregistry.StreamRegistryApp;
import com.expediagroup.streamplatform.streamregistry.graphql.client.InsertConsumerMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpsertZoneMutation;

public class ZoneIT {

  @ClassRule
  public static EmbeddedKafkaCluster kafka = new EmbeddedKafkaCluster(1);
  @ClassRule
  public static SchemaRegistryJUnitRule schemaRegistry = new SchemaRegistryJUnitRule();

  private static ConfigurableApplicationContext context;
  private static String url;

  static Client client;

  @BeforeClass
  public static void beforeClass() {
    String[] args = new String[] {
        "--server.port=0",
        "--repository.kafka.bootstrap-servers=" + kafka.bootstrapServers(),
        "--repository.kafka.replicationFactor=1",
        "--schema.registry.url=" + schemaRegistry.url()
    };
    context = SpringApplication.run(StreamRegistryApp.class, args);
    url = "http://localhost:" + context.getEnvironment().getProperty("local.server.port") + "/graphql";

    client = new Client(url);
  }

  @AfterClass
  public static void afterClass() {
    if (context != null) {
      context.close();
      context = null;
    }
  }

  @Test
  public void upsertZone() {
    ITestDataFactory factory = new ITestDataFactory();

    Object data = client.data(factory.upsertZoneMutationBuilder().build());

    UpsertZoneMutation.Upsert upsert = ((UpsertZoneMutation.Data)data).getZone().getUpsert();

    assertThat(upsert.getKey().getName(), is("zoneName"));
    assertThat(upsert.getSpecification().getDescription().get(), is("description"));
    assertThat(upsert.getSpecification().getConfiguration().get("a").asText(), is("b"));
  }

  @Test
  public void insertConsumer() {
    ITestDataFactory factory = new ITestDataFactory();

    factory.consumerKeyInputBuilder().streamName("dsdsd");

    Object data = client.data(factory.insertConsumerMutationBuilder().build());

    InsertConsumerMutation.Insert insert = ((InsertConsumerMutation.Data)data).getConsumer().getInsert();

    assertThat(insert.getKey().getName(), is("consumerName"));
    assertThat(insert.getSpecification().getDescription().get(), is("description"));
    assertThat(insert.getSpecification().getConfiguration().get("a").asText(), is("b"));
  }

}
