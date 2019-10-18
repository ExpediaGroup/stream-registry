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
import com.expediagroup.streamplatform.streamregistry.graphql.client.InfrastructureQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.InfrastructuresQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.InsertInfrastructureMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpdateInfrastructureMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpdateInfrastructureStatusMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpsertInfrastructureMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.InfrastructureKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.InfrastructureKeyQuery;
import com.expediagroup.streamplatform.streamregistry.it.helpers.ObjectIT;

public class InfrastructureIT extends ObjectIT {

  @Override
  public void create() {

    setFactorySuffix("Create");

    assertMutationFails(factory.updateInfrastructureMutationBuilder().build());

    Object data = client.getData(factory.insertInfrastructureMutationBuilder().build());

    InsertInfrastructureMutation.Insert insert = ((InsertInfrastructureMutation.Data) data).getInfrastructure().getInsert();

    assertThat(insert.getSpecification().getDescription().get(), is(factory.description));
    assertThat(insert.getSpecification().getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Test
  public void update() {

    setFactorySuffix("Update");

    Mutation updateMutation = factory.updateInfrastructureMutationBuilder().build();

    assertMutationFails(updateMutation);

    client.invoke(factory.upsertInfrastructureMutationBuilder().build());

    UpdateInfrastructureMutation.Update update =
        ((UpdateInfrastructureMutation.Data) client.getData(updateMutation)).getInfrastructure().getUpdate();

    assertThat(update.getSpecification().getDescription().get(), is(factory.description));
    assertThat(update.getSpecification().getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Override
  public void upsert() {
    Object data = client.getData(factory.upsertInfrastructureMutationBuilder().build());

    UpsertInfrastructureMutation.Upsert upsert = ((UpsertInfrastructureMutation.Data) data).getInfrastructure().getUpsert();

    assertThat(upsert.getSpecification().getDescription().get(), is(factory.description));
    assertThat(upsert.getSpecification().getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Override
  public void updateStatus() {
    client.getData(factory.upsertInfrastructureMutationBuilder().build());
    Object data = client.getData(factory.updateInfrastructureStatusBuilder().build());

    UpdateInfrastructureStatusMutation.UpdateStatus update =
        ((UpdateInfrastructureStatusMutation.Data) data).getInfrastructure().getUpdateStatus();

    assertThat(update.getSpecification().getDescription().get(), is(factory.description));
    assertThat(update.getStatus().get().getAgentStatus().get("skey").asText(), is("svalue"));
  }

  @Override
  public void queryByKey() {

    InfrastructureKeyInput input = factory.infrastructureKeyInputBuilder().build();

    try {
      client.getData(InfrastructureQuery.builder().key(input).build());
    } catch (RuntimeException e) {
      assertEquals(e.getMessage(), "No value present");
    }

    client.getData(factory.upsertInfrastructureMutationBuilder().build());

    InfrastructureQuery.Data after = (InfrastructureQuery.Data) client.getData(InfrastructureQuery.builder().key(input).build());

    assertEquals(after.getInfrastructure().getKey().getName(), input.name());
  }

  @Override
  public void queryByRegex() {

    setFactorySuffix("queryByRegex");

    InfrastructureKeyQuery query = InfrastructureKeyQuery.builder().nameRegex(".*").build();

    InfrastructuresQuery.Data before = (InfrastructuresQuery.Data) client.getData(InfrastructuresQuery.builder().key(query).build());

    client.invoke(factory.upsertInfrastructureMutationBuilder().build());

    InfrastructuresQuery.Data after = (InfrastructuresQuery.Data) client.getData(InfrastructuresQuery.builder().key(query).build());

    assertEquals(before.getInfrastructures().size() + 1, after.getInfrastructures().size());
  }

  @Override
  public void createRequiredDatastoreState() {

  }
}
