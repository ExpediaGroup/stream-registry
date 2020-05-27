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

import com.expediagroup.streamplatform.streamregistry.graphql.client.test.InsertProducerBindingMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.ProducerBindingQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.ProducerBindingsQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.UpdateProducerBindingMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.UpdateProducerBindingStatusMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.UpsertProducerBindingMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.fragment.ProducerBindingPart;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.fragment.SpecificationPart;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.type.ProducerBindingKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.type.ProducerBindingKeyQuery;
import com.expediagroup.streamplatform.streamregistry.it.helpers.AbstractTestStage;

public class ProducerBindingTestStage extends AbstractTestStage {

  @Override
  public void create() {

    setFactorySuffix("create");

    Mutation insertMutation = factory.insertProducerBindingMutationBuilder().build();

    InsertProducerBindingMutation.Insert insert =
        ((InsertProducerBindingMutation.Data) client.getOptionalData(insertMutation).get())
            .getProducerBinding().getInsert();

    ProducerBindingPart part = insert.getFragments().getProducerBindingPart();
    assertThat(part.getKey().getProducerName(), is(factory.producerName));

    SpecificationPart specificationPart = part.getSpecification().getFragments().getSpecificationPart();
    assertThat(specificationPart.getDescription().get(), is(factory.description));
    assertThat(specificationPart.getConfiguration().get(factory.key).asText(), is(factory.value));

    assertMutationFails(insertMutation);
  }

  @Override
  public void update() {

    setFactorySuffix("update");

    Mutation updateMutation = factory.updateProducerBindingMutationBuilder().build();

    assertMutationFails(updateMutation);

    client.invoke(factory.insertProducerBindingMutationBuilder().build());

    Object data = client.getOptionalData(updateMutation).get();
    UpdateProducerBindingMutation.Update update = ((UpdateProducerBindingMutation.Data) data).getProducerBinding().getUpdate();

    ProducerBindingPart part = update.getFragments().getProducerBindingPart();
    assertThat(part.getKey().getProducerName(), is(factory.producerName));

    SpecificationPart specificationPart = part.getSpecification().getFragments().getSpecificationPart();
    assertThat(specificationPart.getDescription().get(), is(factory.description));
    assertThat(specificationPart.getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Override
  public void upsert() {

    Object data = client.getOptionalData(factory.upsertProducerBindingMutationBuilder().build()).get();

    UpsertProducerBindingMutation.Upsert upsert = ((UpsertProducerBindingMutation.Data) data).getProducerBinding().getUpsert();

    ProducerBindingPart part = upsert.getFragments().getProducerBindingPart();
    assertThat(part.getKey().getProducerName(), is(factory.producerName));

    SpecificationPart specificationPart = part.getSpecification().getFragments().getSpecificationPart();
    assertThat(specificationPart.getDescription().get(), is(factory.description));
    assertThat(specificationPart.getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Override
  public void updateStatus() {
    client.getOptionalData(factory.upsertProducerBindingMutationBuilder().build()).get();
    Object data = client.getOptionalData(factory.updateProducerBindingStatusBuilder().build()).get();

    UpdateProducerBindingStatusMutation.UpdateStatus update =
        ((UpdateProducerBindingStatusMutation.Data) data).getProducerBinding().getUpdateStatus();

    ProducerBindingPart part = update.getFragments().getProducerBindingPart();

    assertThat(part.getSpecification().getFragments().getSpecificationPart().getDescription().get(), is(factory.description));
    assertThat(part.getStatus().get().getFragments().getStatusPart().getAgentStatus().get("skey").asText(), is("svalue"));
  }

  @Override
  public void queryByKey() {

    ProducerBindingKeyInput input = factory.producerBindingKeyInputBuilder().build();

    try {
      client.getOptionalData(ProducerBindingQuery.builder().key(input).build()).get();
    } catch (RuntimeException e) {
      assertEquals(e.getMessage(), "No value present");
    }

    client.getOptionalData(factory.upsertProducerBindingMutationBuilder().build()).get();

    ProducerBindingQuery.Data after = (ProducerBindingQuery.Data) client.getOptionalData(ProducerBindingQuery.builder().key(input).build()).get();

    assertEquals(after.getProducerBinding().getByKey().get().getFragments().getProducerBindingPart().getKey().getStreamDomain(), input.streamDomain());
  }

  @Override
  public void queryByRegex() {

    setFactorySuffix("query_by_regex");

    ProducerBindingKeyQuery query = ProducerBindingKeyQuery.builder().producerNameRegex("producerName.*").build();

    ProducerBindingsQuery.Data before = (ProducerBindingsQuery.Data) client.getOptionalData(ProducerBindingsQuery.builder().key(query).build()).get();

    client.invoke(factory.upsertProducerBindingMutationBuilder().build());

    ProducerBindingsQuery.Data after = (ProducerBindingsQuery.Data) client.getOptionalData(ProducerBindingsQuery.builder().key(query).build()).get();

    assertEquals(after.getProducerBinding().getByQuery().size(), before.getProducerBinding().getByQuery().size() + 1);
    assertNotNull(after.getProducerBinding().getByQuery().get(0)
        .getFragments().getProducerBindingPart().getStatus().get()
        .getFragments().getStatusPart().getAgentStatus());
  }

  @Override
  public void createRequiredDatastoreState() {
    client.createDomain(factory);
    client.createSchema(factory);
    client.createStream(factory);
    client.createZone(factory);
    client.createInfrastructure(factory);
    client.createProducer(factory);
    client.createStreamBinding(factory);
  }

  @Override
  public void queryByInvalidKey() {
    ProducerBindingKeyInput input = factory.producerBindingKeyInputBuilder().producerName("disnae_exist").build();
    assertTrue(client.getProducerBinding(input).isEmpty());
  }
}
