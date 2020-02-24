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

import org.junit.Test;

import com.expediagroup.streamplatform.streamregistry.graphql.client.InfrastructureQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.InfrastructuresQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.InsertInfrastructureMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpdateInfrastructureMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpdateInfrastructureStatusMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpsertInfrastructureMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.InfrastructureKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.InfrastructureKeyQuery;
import com.expediagroup.streamplatform.streamregistry.it.helpers.AbstractTestStage;

public class InfrastructureTestStage extends AbstractTestStage {

  @Override
  public void create() {

    setFactorySuffix("create");

    assertMutationFails(factory.updateInfrastructureMutationBuilder().build());

    Object data = client.getOptionalData(factory.insertInfrastructureMutationBuilder().build()).get();

    InsertInfrastructureMutation.Insert insert = ((InsertInfrastructureMutation.Data) data).getInfrastructure().getInsert();

    assertThat(insert.getSpecification().getDescription().get(), is(factory.description));
    assertThat(insert.getSpecification().getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Test
  public void update() {

    setFactorySuffix("update");

    Mutation updateMutation = factory.updateInfrastructureMutationBuilder().build();

    assertMutationFails(updateMutation);

    client.invoke(factory.upsertInfrastructureMutationBuilder().build());

    UpdateInfrastructureMutation.Update update =
        ((UpdateInfrastructureMutation.Data) client.getOptionalData(updateMutation).get()).getInfrastructure().getUpdate();

    assertThat(update.getSpecification().getDescription().get(), is(factory.description));
    assertThat(update.getSpecification().getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Override
  public void upsert() {
    Object data = client.getOptionalData(factory.upsertInfrastructureMutationBuilder().build()).get();

    UpsertInfrastructureMutation.Upsert upsert = ((UpsertInfrastructureMutation.Data) data).getInfrastructure().getUpsert();

    assertThat(upsert.getSpecification().getDescription().get(), is(factory.description));
    assertThat(upsert.getSpecification().getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Override
  public void updateStatus() {
    client.getOptionalData(factory.upsertInfrastructureMutationBuilder().build()).get();
    Object data = client.getOptionalData(factory.updateInfrastructureStatusBuilder().build()).get();

    UpdateInfrastructureStatusMutation.UpdateStatus update =
        ((UpdateInfrastructureStatusMutation.Data) data).getInfrastructure().getUpdateStatus();

    assertThat(update.getSpecification().getDescription().get(), is(factory.description));
    assertThat(update.getStatus().get().getAgentStatus().get("skey").asText(), is("svalue"));
  }

  @Override
  public void queryByKey() {

    InfrastructureKeyInput input = factory.infrastructureKeyInputBuilder().build();

    try {
      client.getOptionalData(InfrastructureQuery.builder().key(input).build()).get();
    } catch (RuntimeException e) {
      assertEquals(e.getMessage(), "No value present");
    }

    client.getOptionalData(factory.upsertInfrastructureMutationBuilder().build()).get();

    InfrastructureQuery.Data after = (InfrastructureQuery.Data) client.getOptionalData(InfrastructureQuery.builder().key(input).build()).get();

    assertEquals(after.getInfrastructure().getByKey().get().getKey().getName(), input.name());
  }

  @Override
  public void queryByRegex() {

    setFactorySuffix("query_by_regex");

    InfrastructureKeyQuery query = InfrastructureKeyQuery.builder().nameRegex(".*").build();

    InfrastructuresQuery.Data before = (InfrastructuresQuery.Data) client.getOptionalData(InfrastructuresQuery.builder().key(query).build()).get();

    client.invoke(factory.upsertInfrastructureMutationBuilder().build());

    InfrastructuresQuery.Data after = (InfrastructuresQuery.Data) client.getOptionalData(InfrastructuresQuery.builder().key(query).build()).get();

    assertEquals(before.getInfrastructure().getByQuery().size() + 1, after.getInfrastructure().getByQuery().size());
    assertNotNull(after.getInfrastructure().getByQuery().get(0).getStatus().get().getAgentStatus());
  }

  @Override
  public void createRequiredDatastoreState() {
    client.createZone(factory);
  }

  @Override
  public void queryByInvalidKey() {
    InfrastructureKeyInput input = factory.infrastructureKeyInputBuilder().name("disnae_exist").build();
    assertTrue(client.getInfrastructure(input).isEmpty());
  }
}
