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

import com.expediagroup.streamplatform.streamregistry.graphql.client.ConsumerBindingQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.ConsumerBindingsQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.InsertConsumerBindingMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpdateConsumerBindingMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpdateConsumerBindingStatusMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpsertConsumerBindingMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.ConsumerBindingKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.ConsumerBindingKeyQuery;
import com.expediagroup.streamplatform.streamregistry.it.helpers.AbstractTestStage;

public class ConsumerBindingTestStage extends AbstractTestStage {

  @Override
  public void create() {

    setFactorySuffix("create");

    assertMutationFails(factory.updateConsumerBindingMutationBuilder().build());

    Object data = client.getOptionalData(factory.insertConsumerBindingMutationBuilder().build()).get();

    InsertConsumerBindingMutation.Insert insert = ((InsertConsumerBindingMutation.Data) data).getConsumerBinding().getInsert();

    assertThat(insert.getKey().getStreamName(), is(factory.streamName));

    assertThat(insert.getSpecification().getDescription().get(), is(factory.description));
    assertThat(insert.getSpecification().getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Test
  public void update() {

    setFactorySuffix("update");

    Mutation updateMutation = factory.updateConsumerBindingMutationBuilder().build();

    assertMutationFails(updateMutation);

    client.invoke(factory.insertConsumerBindingMutationBuilder().build());

    UpdateConsumerBindingMutation.Update update = ((UpdateConsumerBindingMutation.Data) client.getOptionalData(updateMutation).get())
        .getConsumerBinding().getUpdate();

    assertThat(update.getKey().getStreamName(), is(factory.streamName));

    assertThat(update.getSpecification().getDescription().get(), is(factory.description));
    assertThat(update.getSpecification().getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Override
  public void upsert() {

    setFactorySuffix("upsert");

    Object data = client.getOptionalData(factory.upsertConsumerBindingMutationBuilder().build()).get();

    UpsertConsumerBindingMutation.Upsert upsert = ((UpsertConsumerBindingMutation.Data) data).getConsumerBinding().getUpsert();

    assertThat(upsert.getSpecification().getDescription().get(), is(factory.description));
    assertThat(upsert.getSpecification().getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Override
  public void updateStatus() {
    client.getOptionalData(factory.upsertConsumerBindingMutationBuilder().build()).get();
    Object data = client.getOptionalData(factory.updateConsumerBindingStatusBuilder().build()).get();

    UpdateConsumerBindingStatusMutation.UpdateStatus update =
        ((UpdateConsumerBindingStatusMutation.Data) data).getConsumerBinding().getUpdateStatus();

    assertThat(update.getSpecification().getDescription().get(), is(factory.description));
    assertThat(update.getStatus().get().getAgentStatus().get("skey").asText(), is("svalue"));
  }

  @Override
  public void queryByKey() {

    ConsumerBindingKeyInput input = factory.consumerBindingKeyInputBuilder().build();

    try {
      client.getOptionalData(ConsumerBindingQuery.builder().key(input).build()).get();
    } catch (RuntimeException e) {
      assertEquals(e.getMessage(), "No value present");
    }

    client.getOptionalData(factory.upsertConsumerBindingMutationBuilder().build()).get();

    ConsumerBindingQuery.Data after = (ConsumerBindingQuery.Data) client.getOptionalData(ConsumerBindingQuery.builder().key(input).build()).get();

    assertEquals(after.getConsumerBinding().getByKey().get().getKey().getStreamName(), input.streamName());
  }

  @Override
  public void queryByRegex() {

    setFactorySuffix("query_by_regex");

    ConsumerBindingKeyQuery query = ConsumerBindingKeyQuery.builder().consumerNameRegex(".*").build();

    ConsumerBindingsQuery.Data before = (ConsumerBindingsQuery.Data) client.getOptionalData(ConsumerBindingsQuery.builder().key(query).build()).get();

    client.invoke(factory.upsertConsumerBindingMutationBuilder().build());

    ConsumerBindingsQuery.Data after = (ConsumerBindingsQuery.Data) client.getOptionalData(ConsumerBindingsQuery.builder().key(query).build()).get();

    assertEquals(before.getConsumerBinding().getByQuery().size() + 1,
        after.getConsumerBinding().getByQuery().size());
  }

  @Override
  public void createRequiredDatastoreState() {
    client.createConsumer(factory);
  }

  @Override
  public void queryByInvalidKey() {
    ConsumerBindingKeyInput input = factory.consumerBindingKeyInputBuilder().consumerName("disnae_exist").build();
    assertTrue(client.getConsumerBinding(input).isEmpty());
  }
}
