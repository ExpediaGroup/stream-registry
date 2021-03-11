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

import com.expediagroup.streamplatform.streamregistry.graphql.client.test.InsertProducerMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.ProducerQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.ProducersQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.UpdateProducerMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.UpdateProducerStatusMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.UpsertProducerMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.fragment.ProducerPart;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.fragment.SpecificationPart;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.type.ProducerKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.type.ProducerKeyQuery;
import com.expediagroup.streamplatform.streamregistry.it.helpers.AbstractTestStage;

public class ProducerTestStage extends AbstractTestStage {

  @Override
  public void create() {

    Mutation createMutation = factory.insertProducerMutationBuilder().build();

    Object data = client.getOptionalData(createMutation).get();

    InsertProducerMutation.Insert insert = ((InsertProducerMutation.Data) data).getProducer().getInsert();

    ProducerPart part = insert.getFragments().getProducerPart();
    assertThat(part.getKey().getName(), is(factory.producerName));

    SpecificationPart specificationPart = part.getSpecification().getFragments().getSpecificationPart();
    assertThat(specificationPart.getDescription().get(), is(factory.description));
    assertThat(specificationPart.getConfiguration().get(factory.key).asText(), is(factory.value));

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

    ProducerPart part = update.getFragments().getProducerPart();
    assertThat(part.getKey().getName(), is(factory.producerName));

    SpecificationPart specificationPart = part.getSpecification().getFragments().getSpecificationPart();
    assertThat(specificationPart.getDescription().get(), is(factory.description));
    assertThat(specificationPart.getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Override
  public void upsert() {

    Object data = client.getOptionalData(factory.upsertProducerMutationBuilder().build()).get();

    UpsertProducerMutation.Upsert upsert = ((UpsertProducerMutation.Data) data).getProducer().getUpsert();

    ProducerPart part = upsert.getFragments().getProducerPart();
    assertThat(part.getKey().getName(), is(factory.producerName));

    SpecificationPart specificationPart = part.getSpecification().getFragments().getSpecificationPart();
    assertThat(specificationPart.getDescription().get(), is(factory.description));
    assertThat(specificationPart.getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Override
  public void delete() {
    setFactorySuffix("delete");
  }

  @Override
  public void updateStatus() {
    client.getOptionalData(factory.upsertProducerMutationBuilder().build()).get();
    Object data = client.getOptionalData(factory.updateProducerStatusBuilder().build()).get();

    UpdateProducerStatusMutation.UpdateStatus update =
        ((UpdateProducerStatusMutation.Data) data).getProducer().getUpdateStatus();

    ProducerPart part = update.getFragments().getProducerPart();

    assertThat(part.getSpecification().getFragments().getSpecificationPart().getDescription().get(), is(factory.description));
    assertThat(part.getStatus().get().getFragments().getStatusPart().getAgentStatus().get("skey").asText(), is("svalue"));
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

    assertEquals(after.getProducer().getByKey().get().getFragments().getProducerPart().getKey().getName(), input.name());
  }

  @Override
  public void queryByRegex() {

    setFactorySuffix("query_by_regex");

    ProducerKeyQuery query = ProducerKeyQuery.builder().nameRegex("producer_name.*").build();

    ProducersQuery.Data before = (ProducersQuery.Data) client.getOptionalData(ProducersQuery.builder().key(query).build()).get();

    client.invoke(factory.upsertProducerMutationBuilder().build());

    ProducersQuery.Data after = (ProducersQuery.Data) client.getOptionalData(ProducersQuery.builder().key(query).build()).get();

    assertEquals(after.getProducer().getByQuery().size(), before.getProducer().getByQuery().size() + 1);
    assertNotNull(after.getProducer().getByQuery().get(0)
        .getFragments().getProducerPart().getStatus().get()
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
    ProducerKeyInput input = factory.producerKeyInputBuilder().name("disnae_exist").build();
    assertFalse(client.getProducer(input).isPresent());
  }
}
