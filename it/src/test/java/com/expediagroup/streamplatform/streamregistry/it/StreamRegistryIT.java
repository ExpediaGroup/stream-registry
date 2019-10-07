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

import static com.expediagroup.streamplatform.streamregistry.it.ITHelper.consumerKeyInputBuilder;
import static com.expediagroup.streamplatform.streamregistry.it.ITHelper.domainKeyInputBuilder;
import static com.expediagroup.streamplatform.streamregistry.it.ITHelper.specificationInputBuilder;
import static com.expediagroup.streamplatform.streamregistry.it.ITHelper.upsertConsumerMutation;
import static com.expediagroup.streamplatform.streamregistry.it.ITHelper.upsertDomainMutation;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.kafka.streams.integration.utils.EmbeddedKafkaCluster;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import com.expediagroup.streamplatform.streamregistry.StreamRegistryApp;
import com.expediagroup.streamplatform.streamregistry.graphql.client.GetDomainQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpsertConsumerMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpsertDomainMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.ConsumerKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.DomainKeyInput;

public class StreamRegistryIT {

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
  public void upsertConsumer() {
    ConsumerKeyInput key = consumerKeyInputBuilder().build();
    UpsertConsumerMutation upsert = upsertConsumerMutation(key, specificationInputBuilder().build());

    UpsertConsumerMutation.Upsert consumer =  client.consumerUpsert(upsert);

    assertThat(consumer.getKey().getName(), is("consumerName"));
    assertThat(consumer.getSpecification().getDescription().get(), is("description"));
    assertThat(consumer.getSpecification().getConfiguration().get("a").asText(), is("b"));

  }


  @Test
  public void upsertDomain() {
    DomainKeyInput key = domainKeyInputBuilder().build();
    UpsertDomainMutation upsert = upsertDomainMutation(key, specificationInputBuilder().build());

    UpsertDomainMutation.Upsert domain =  client.domainUpsert(upsert);

    assertThat(domain.getKey().getName(), is("domainName"));
    assertThat(domain.getSpecification().getDescription().get(), is("description"));
    assertThat(domain.getSpecification().getConfiguration().get("a").asText(), is("b"));
  }


  @Test
  public void queryDomain() {

    DomainKeyInput key = domainKeyInputBuilder().build();
    UpsertDomainMutation upsert = upsertDomainMutation(key, specificationInputBuilder().build());

    UpsertDomainMutation.Upsert domain =  client.domainUpsert(upsert);

    assertThat(domain.getKey().getName(), is("domainName"));
    assertThat(domain.getSpecification().getDescription().get(), is("description"));
    assertThat(domain.getSpecification().getConfiguration().get("a").asText(), is("b"));

    GetDomainQuery.Domain out =client.domainQuery(GetDomainQuery.builder().key(key).build());

    assertThat(out.getKey().getName(), is("domainName"));
    assertThat(out.getSpecification().getDescription().get(), is("description"));
    ObjectNode on = out.getSpecification().getConfiguration();
    assertThat(on.get("a").asText(), is("b"));
  }
}
