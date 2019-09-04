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

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import okhttp3.OkHttpClient;

import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.kafka.streams.integration.utils.EmbeddedKafkaCluster;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import reactor.core.publisher.Mono;

import com.expediagroup.streamplatform.streamregistry.app.StreamRegistryApp;
import com.expediagroup.streamplatform.streamregistry.graphql.client.ConfigTypeAdapter;
import com.expediagroup.streamplatform.streamregistry.graphql.client.CreateDomainMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.DomainsQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.TagsTypeAdapter;
import com.expediagroup.streamplatform.streamregistry.graphql.client.reactor.ReactorApollo;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.CustomType;

public class StreamRegistryIT {
  private static final ObjectMapper mapper = new ObjectMapper();

  @ClassRule
  public static EmbeddedKafkaCluster kafka = new EmbeddedKafkaCluster(1);
  @ClassRule
  public static SchemaRegistryJUnitRule schemaRegistry = new SchemaRegistryJUnitRule();

  private static ConfigurableApplicationContext context;
  private static String url;

  @BeforeClass
  public static void beforeClass() {
    String[] args = new String[]{
        "--server.port=0",
        "--repository.kafka.bootstrap-servers=" + kafka.bootstrapServers(),
        "--repository.kafka.replicationFactor=1",
        "--schema.registry.url=" + schemaRegistry.url()
    };
    context = SpringApplication.run(StreamRegistryApp.class, args);
    url = "http://localhost:" + context.getEnvironment().getProperty("local.server.port") + "/graphql";
  }

  @AfterClass
  public static void afterClass() {
    if (context != null) {
      context.close();
      context = null;
    }
  }

  @Test
  public void test() {
    ApolloClient client = ApolloClient
        .builder()
        .serverUrl(url)
        .okHttpClient(new OkHttpClient.Builder().build())
        .addCustomTypeAdapter(CustomType.TAGS, new TagsTypeAdapter())
        .addCustomTypeAdapter(CustomType.CONFIG, new ConfigTypeAdapter())
        .build();

    Response<Optional<CreateDomainMutation.Data>> mutation = ReactorApollo.from(
        client.mutate(CreateDomainMutation
            .builder()
            .name("domain")
            .description("description")
            .tags(Map.of("key", "value"))
            .type("default")
            .configuration(mapper.createObjectNode().put("key", "value"))
            .build()))
        .block();

    assertThat(mutation.data().get().isCreateDomain(), is(true));

    Mono.delay(Duration.ofSeconds(5)).block();

    Response<Optional<DomainsQuery.Data>> query = ReactorApollo.from(
        client.query(DomainsQuery
            .builder()
            .name("^domain$")
            .build()))
        .block();

    List<DomainsQuery.Domain> domains = query.data().get().getDomains();
    assertThat(domains.size(), is(1));
  }
}
