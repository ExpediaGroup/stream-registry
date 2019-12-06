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

import static junit.framework.TestCase.assertTrue;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.apollographql.apollo.api.Mutation;

import org.junit.Test;

import com.expediagroup.streamplatform.streamregistry.graphql.client.ConsumerQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.ConsumersQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.InsertConsumerMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpdateConsumerMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpdateConsumerStatusMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpsertConsumerMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.ConsumerKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.ConsumerKeyQuery;
import com.expediagroup.streamplatform.streamregistry.it.helpers.AbstractTestStage;

public class ConsumerTestStage extends AbstractTestStage {

  @Test
  public void upsert() {

    setFactorySuffix("upsert");

    Object data = client.getOptionalData(factory.upsertConsumerMutationBuilder().build()).get();

    UpsertConsumerMutation.Upsert upsert = ((UpsertConsumerMutation.Data) data).getConsumer().getUpsert();

    assertThat(upsert.getKey().getName(), is(factory.consumerName));

    assertThat(upsert.getSpecification().getDescription().get(), is(factory.description));
    assertThat(upsert.getSpecification().getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Override
  public void create() {

    setFactorySuffix("create");

    assertMutationFails(factory.updateConsumerMutationBuilder().build());

    Object data = client.getOptionalData(factory.insertConsumerMutationBuilder().build()).get();

    InsertConsumerMutation.Insert insert = ((InsertConsumerMutation.Data) data).getConsumer().getInsert();

    assertThat(insert.getKey().getName(), is(factory.consumerName));

    assertThat(insert.getSpecification().getDescription().get(), is(factory.description));
    assertThat(insert.getSpecification().getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Test
  public void update() {

    setFactorySuffix("update");

    Mutation updateMutation = factory.updateConsumerMutationBuilder().build();

    assertMutationFails(updateMutation);

    client.invoke(factory.insertConsumerMutationBuilder().build());

    UpdateConsumerMutation.Update update = ((UpdateConsumerMutation.Data) client.getOptionalData(updateMutation).get())
        .getConsumer().getUpdate();

    assertThat(update.getKey().getName(), is(factory.consumerName));

    assertThat(update.getSpecification().getDescription().get(), is(factory.description));
    assertThat(update.getSpecification().getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Test
  public void updateStatus() {

    client.getOptionalData(factory.upsertConsumerMutationBuilder().build()).get();

    Object data = client.getOptionalData(factory.updateConsumerStatusBuilder().build()).get();

    UpdateConsumerStatusMutation.UpdateStatus update =
        ((UpdateConsumerStatusMutation.Data) data).getConsumer().getUpdateStatus();

    assertThat(update.getKey().getName(), is(factory.consumerName));
    assertThat(update.getSpecification().getDescription().get(), is(factory.description));
    assertThat(update.getSpecification().getConfiguration().get(factory.key).asText(), is(factory.value));

    assertThat(update.getStatus().get().getAgentStatus().get("skey").asText(), is("svalue"));
  }

  @Override
  public void queryByKey() {

    ConsumerKeyInput input = factory.consumerKeyInputBuilder().build();

    try {
      client.getOptionalData(ConsumerQuery.builder().key(input).build()).get();
    } catch (RuntimeException e) {
      assertEquals(e.getMessage(), "No value present");
    }

    client.getOptionalData(factory.upsertConsumerMutationBuilder().build()).get();

    ConsumerQuery.Data after = (ConsumerQuery.Data) client.getOptionalData(ConsumerQuery.builder().key(input).build()).get();

    assertEquals(after.getConsumer().getByKey().get().getKey().getName(), input.name());
  }

  @Override
  public void queryByRegex() {

    setFactorySuffix("query_by_regex");

    ConsumerKeyQuery query = ConsumerKeyQuery.builder().nameRegex(".*").build();

    ConsumersQuery.Data before = (ConsumersQuery.Data) client.getOptionalData(ConsumersQuery.builder().key(query).build()).get();

    client.invoke(factory.upsertConsumerMutationBuilder().build());

    ConsumersQuery.Data after = (ConsumersQuery.Data) client.getOptionalData(ConsumersQuery.builder().key(query).build()).get();

    assertEquals(before.getConsumer().getByQuery().size() + 1, after.getConsumer().getByQuery().size());
  }

  @Override
  public void createRequiredDatastoreState() {
    client.createStream(factory);
  }

  @Override
  public void queryByInvalidKey() {
    ConsumerKeyInput input = factory.consumerKeyInputBuilder().name("disnae_exist").build();
    assertTrue(client.getConsumer(input).isEmpty());
  }
}
