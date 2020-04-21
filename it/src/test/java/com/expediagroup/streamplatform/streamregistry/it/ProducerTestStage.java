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
package com.expediagroup.streamplatform.streamregistry.it;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import com.apollographql.apollo.api.Mutation;

import com.expediagroup.streamplatform.streamregistry.graphql.client.InsertProducerMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.ProducerQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.ProducersQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpdateProducerMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpdateProducerStatusMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpsertProducerMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.ProducerKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.ProducerKeyQuery;
import com.expediagroup.streamplatform.streamregistry.it.helpers.AbstractTestStage;

public class ProducerTestStage extends AbstractTestStage {

  @Override
  public void create() {

    Mutation createMutation = factory.insertProducerMutationBuilder().build();

    Object data = client.getOptionalData(createMutation).get();

    InsertProducerMutation.Insert update = ((InsertProducerMutation.Data) data).getProducer().getInsert();

    assertThat(update.getSpecification().getDescription().get(), is(factory.description));
    assertThat(update.getSpecification().getConfiguration().get(factory.key).asText(), is(factory.value));

    assertMutationFails(createMutation);
  }

  @Override
  public void update() {

    setFactorySuffix("update");

    Mutation updateMutation = factory.updateProducerMutationBuilder().build();

    assertMutationFails(updateMutation);

    client.invoke(factory.insertProducerMutationBuilder().build());

    Object data = client.getOptionalData(updateMutation).get();
    UpdateProducerMutation.Update update = ((UpdateProducerMutation.Data) data).getProducer().getUpdate();

    assertThat(update.getSpecification().getDescription().get(), is(factory.description));
    assertThat(update.getSpecification().getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Override
  public void upsert() {

    Object data = client.getOptionalData(factory.upsertProducerMutationBuilder().build()).get();

    UpsertProducerMutation.Upsert upsert = ((UpsertProducerMutation.Data) data).getProducer().getUpsert();

    assertThat(upsert.getSpecification().getDescription().get(), is(factory.description));
    assertThat(upsert.getSpecification().getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Override
  public void updateStatus() {
    client.getOptionalData(factory.upsertProducerMutationBuilder().build()).get();
    Object data = client.getOptionalData(factory.updateProducerStatusBuilder().build()).get();

    UpdateProducerStatusMutation.UpdateStatus update =
        ((UpdateProducerStatusMutation.Data) data).getProducer().getUpdateStatus();

    assertThat(update.getSpecification().getDescription().get(), is(factory.description));
    assertThat(update.getStatus().get().getAgentStatus().get("skey").asText(), is("svalue"));
  }

  @Override
  public void queryByKey() {

    ProducerKeyInput input = factory.producerKeyInputBuilder().build();

    try {
      client.getOptionalData(ProducerQuery.builder().key(input).build()).get();
    } catch (RuntimeException e) {
      assertEquals(e.getMessage(), "No value present");
    }

    client.getOptionalData(factory.upsertProducerMutationBuilder().build()).get();

    ProducerQuery.Data after = (ProducerQuery.Data) client.getOptionalData(ProducerQuery.builder().key(input).build()).get();

    assertEquals(after.getProducer().getByKey().get().getKey().getName(), input.name());
  }

  @Override
  public void queryByRegex() {

    setFactorySuffix("query_by_regex");

    ProducerKeyQuery query = ProducerKeyQuery.builder().nameRegex("producer_name.*").build();

    ProducersQuery.Data before = (ProducersQuery.Data) client.getOptionalData(ProducersQuery.builder().key(query).build()).get();

    client.invoke(factory.upsertProducerMutationBuilder().build());

    ProducersQuery.Data after = (ProducersQuery.Data) client.getOptionalData(ProducersQuery.builder().key(query).build()).get();

    assertEquals(after.getProducer().getByQuery().size(), before.getProducer().getByQuery().size() + 1);
    assertNotNull(after.getProducer().getByQuery().get(0).getStatus().get().getAgentStatus());
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
    ProducerKeyInput input = factory.producerKeyInputBuilder().name("disnae_exist").build();
    assertTrue(client.getProducer(input).isEmpty());
  }
}
