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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import okhttp3.OkHttpClient;

import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.kafka.streams.integration.utils.EmbeddedKafkaCluster;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import reactor.core.publisher.Mono;

import com.expediagroup.streamplatform.streamregistry.StreamRegistryApp;
import com.expediagroup.streamplatform.streamregistry.graphql.client.GetDomainQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.InsertDomainMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.ObjectNodeTypeAdapter;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpsertDomainMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.reactor.ReactorApollo;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.CustomType;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.DomainKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.SpecificationInput;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.TagInput;


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
    String[] args = new String[] {
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
  public void upsertDomain() {

    ApolloClient client = ApolloClient
        .builder()
        .serverUrl(url)
        .okHttpClient(new OkHttpClient.Builder().build())
        .addCustomTypeAdapter(CustomType.OBJECTNODE, new ObjectNodeTypeAdapter())
        .build();

    DomainKeyInput key = DomainKeyInput.builder().name("domainName").build();

    SpecificationInput spec = SpecificationInput.builder()
        .configuration(mapper.createObjectNode().put("a","b"))
        .description("description")
        .tags(Collections.emptyList())
        .type("default")
        .build();

    UpsertDomainMutation upsertDomainMutation = UpsertDomainMutation.builder()
        .key(key)
        .specification(spec).build();

    Response<Optional<UpsertDomainMutation.Data>> mutation =
        ReactorApollo.from(client.mutate(upsertDomainMutation)).block();

    assertThat(mutation.data().get().getDomain().getUpsert().getKey().getName(), is("domainName"));

    assertThat(mutation.data().get().getDomain().getUpsert().getSpecification().getDescription().get(), is("description"));

    ObjectNode n=mutation.data().get().getDomain().getUpsert().getSpecification().getConfiguration();

    assertThat(n.get("a").asText(), is("b"));

  }

  @Test
  public void testInsert() {
    ApolloClient client = ApolloClient
        .builder()
        .serverUrl(url)
        .okHttpClient(new OkHttpClient.Builder().build())
        .addCustomTypeAdapter(CustomType.OBJECTNODE, new ObjectNodeTypeAdapter())
        .build();

    Response<Optional<InsertDomainMutation.Data>> mutation = ReactorApollo.from(
        client.mutate(InsertDomainMutation
            .builder()
            .key(DomainKeyInput
                .builder()
                .name("domain")
                .build())
            .specification(SpecificationInput
                .builder()
                .description("description")
                .tags(List.of(TagInput
                    .builder()
                    .name("name")
                    .value("value")
                    .build()))
                .type("default")
                .configuration(mapper.createObjectNode().put("key", "value"))
                .build())
            .build()))
        .block();

    assertThat(mutation.data().get().getDomain().getInsert().getKey().getName(), is("domain"));

    Mono.delay(Duration.ofSeconds(5)).block();

    Response<Optional<GetDomainQuery.Data>> query = ReactorApollo.from(
        client.query(GetDomainQuery
            .builder()
            .key(DomainKeyInput
                .builder()
                .name("domain")
                .build())
            .build()))
        .block();

    assertThat(query.data().get().getDomain().getKey().getName(), is("domain"));
  }

}
