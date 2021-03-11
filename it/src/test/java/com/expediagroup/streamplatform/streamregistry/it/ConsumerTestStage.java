/**
 * Copyright (C) 2018-2021 Expedia, Inc.
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import com.apollographql.apollo.api.Mutation;

import org.junit.Test;

import com.expediagroup.streamplatform.streamregistry.graphql.client.test.ConsumerQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.ConsumersQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.InsertConsumerMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.UpdateConsumerMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.UpdateConsumerStatusMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.UpsertConsumerMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.fragment.ConsumerPart;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.fragment.SpecificationPart;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.type.ConsumerKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.type.ConsumerKeyQuery;
import com.expediagroup.streamplatform.streamregistry.it.helpers.AbstractTestStage;

public class ConsumerTestStage extends AbstractTestStage {

  @Override
  public void create() {

    setFactorySuffix("create");

    assertMutationFails(factory.updateConsumerMutationBuilder().build());

    Object data = client.getOptionalData(factory.insertConsumerMutationBuilder().build()).get();

    InsertConsumerMutation.Insert insert = ((InsertConsumerMutation.Data) data).getConsumer().getInsert();

    ConsumerPart part = insert.getFragments().getConsumerPart();
    assertThat(part.getKey().getName(), is(factory.consumerName));

    SpecificationPart specificationPart = part.getSpecification().getFragments().getSpecificationPart();
    assertThat(specificationPart.getDescription().get(), is(factory.description));
    assertThat(specificationPart.getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Test
  public void update() {

    setFactorySuffix("update");

    Mutation updateMutation = factory.updateConsumerMutationBuilder().build();

    assertMutationFails(updateMutation);

    client.invoke(factory.insertConsumerMutationBuilder().build());

    UpdateConsumerMutation.Update update = ((UpdateConsumerMutation.Data) client.getOptionalData(updateMutation).get())
        .getConsumer().getUpdate();

    ConsumerPart part = update.getFragments().getConsumerPart();
    assertThat(part.getKey().getName(), is(factory.consumerName));

    SpecificationPart specificationPart = part.getSpecification().getFragments().getSpecificationPart();
    assertThat(specificationPart.getDescription().get(), is(factory.description));
    assertThat(specificationPart.getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Test
  public void upsert() {

    setFactorySuffix("upsert");

    Object data = client.getOptionalData(factory.upsertConsumerMutationBuilder().build()).get();

    UpsertConsumerMutation.Upsert upsert = ((UpsertConsumerMutation.Data) data).getConsumer().getUpsert();

    ConsumerPart part = upsert.getFragments().getConsumerPart();
    assertThat(part.getKey().getName(), is(factory.consumerName));

    SpecificationPart specificationPart = part.getSpecification().getFragments().getSpecificationPart();
    assertThat(specificationPart.getDescription().get(), is(factory.description));
    assertThat(specificationPart.getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Override
  public void delete() {
    setFactorySuffix("delete");
  }

  @Test
  public void updateStatus() {

    client.getOptionalData(factory.upsertConsumerMutationBuilder().build()).get();

    Object data = client.getOptionalData(factory.updateConsumerStatusBuilder().build()).get();

    UpdateConsumerStatusMutation.UpdateStatus update =
        ((UpdateConsumerStatusMutation.Data) data).getConsumer().getUpdateStatus();

    ConsumerPart part = update.getFragments().getConsumerPart();
    assertThat(part.getKey().getName(), is(factory.consumerName));

    SpecificationPart specificationPart = part.getSpecification().getFragments().getSpecificationPart();
    assertThat(specificationPart.getDescription().get(), is(factory.description));
    assertThat(specificationPart.getConfiguration().get(factory.key).asText(), is(factory.value));

    assertThat(part.getStatus().get().getFragments().getStatusPart().getAgentStatus().get("skey").asText(), is("svalue"));
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

    assertEquals(after.getConsumer().getByKey().get().getFragments().getConsumerPart().getKey().getName(), input.name());
  }

  @Override
  public void queryByRegex() {

    setFactorySuffix("query_by_regex");

    ConsumerKeyQuery query = ConsumerKeyQuery.builder().nameRegex(".*").build();

    ConsumersQuery.Data before = (ConsumersQuery.Data) client.getOptionalData(ConsumersQuery.builder().key(query).build()).get();

    client.invoke(factory.upsertConsumerMutationBuilder().build());

    ConsumersQuery.Data after = (ConsumersQuery.Data) client.getOptionalData(ConsumersQuery.builder().key(query).build()).get();

    assertEquals(before.getConsumer().getByQuery().size() + 1, after.getConsumer().getByQuery().size());
    assertNotNull(after.getConsumer().getByQuery().get(0)
        .getFragments().getConsumerPart().getStatus().get()
        .getFragments().getStatusPart().getAgentStatus());
  }

  @Override
  public void createRequiredDatastoreState() {
    client.createDomain(factory);
    client.createSchema(factory);
    client.createStream(factory);
    client.createZone(factory);
  }

  @Override
  public void queryByInvalidKey() {
    ConsumerKeyInput input = factory.consumerKeyInputBuilder().name("disnae_exist").build();
    assertFalse(client.getConsumer(input).isPresent());
  }
}
