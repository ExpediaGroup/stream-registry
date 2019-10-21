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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.apollographql.apollo.api.Mutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.ConsumerQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.ConsumersQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.InsertConsumerMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpdateConsumerMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpdateConsumerStatusMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpsertConsumerMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.ConsumerKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.ConsumerKeyQuery;
import com.expediagroup.streamplatform.streamregistry.it.helpers.ObjectIT;

public class ConsumerIT extends ObjectIT {

  @Test
  public void upsert() {

    setFactorySuffix("upsert");

    Object data = client.getData(factory.upsertConsumerMutationBuilder().build());

    UpsertConsumerMutation.Upsert upsert = ((UpsertConsumerMutation.Data) data).getConsumer().getUpsert();

    assertThat(upsert.getKey().getName(), is(factory.consumerName));

    assertThat(upsert.getSpecification().getDescription().get(), is(factory.description));
    assertThat(upsert.getSpecification().getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Override
  public void create() {

    setFactorySuffix("create");

    assertMutationFails(factory.updateConsumerMutationBuilder().build());

    Object data = client.getData(factory.insertConsumerMutationBuilder().build());

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

    UpdateConsumerMutation.Update update = ((UpdateConsumerMutation.Data) client.getData(updateMutation))
        .getConsumer().getUpdate();

    assertThat(update.getKey().getName(), is(factory.consumerName));

    assertThat(update.getSpecification().getDescription().get(), is(factory.description));
    assertThat(update.getSpecification().getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Test
  public void updateStatus() {

    client.getData(factory.upsertConsumerMutationBuilder().build());

    Object data = client.getData(factory.updateConsumerStatusBuilder().build());

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
      client.getData(ConsumerQuery.builder().key(input).build());
    } catch (RuntimeException e) {
      assertEquals(e.getMessage(), "No value present");
    }

    client.getData(factory.upsertConsumerMutationBuilder().build());

    ConsumerQuery.Data after = (ConsumerQuery.Data) client.getData(ConsumerQuery.builder().key(input).build());

    assertEquals(after.getConsumer().getKey().getName(), input.name());
  }

  @Override
  public void queryByRegex() {

    setFactorySuffix("queryByRegex");

    ConsumerKeyQuery query = ConsumerKeyQuery.builder().nameRegex(".*").build();

    ConsumersQuery.Data before = (ConsumersQuery.Data) client.getData(ConsumersQuery.builder().key(query).build());

    client.invoke(factory.upsertConsumerMutationBuilder().build());

    ConsumersQuery.Data after = (ConsumersQuery.Data) client.getData(ConsumersQuery.builder().key(query).build());

    assertEquals(before.getConsumers().size() + 1, after.getConsumers().size());
  }

  @Override
  public void createRequiredDatastoreState() {
    client.createStream(factory);
  }
}
