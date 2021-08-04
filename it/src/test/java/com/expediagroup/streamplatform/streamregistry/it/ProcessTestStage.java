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
import static org.junit.Assert.*;

import com.apollographql.apollo.api.Response;

import com.expediagroup.streamplatform.streamregistry.graphql.client.test.*;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.fragment.ProcessPart;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.fragment.SpecificationPart;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.type.ProcessKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.type.ProcessKeyQuery;
import com.expediagroup.streamplatform.streamregistry.it.helpers.AbstractTestStage;

public class ProcessTestStage extends AbstractTestStage {

  @Override
  public void create() {
    Object data = client.getOptionalData(factory.insertProcessMutationBuilder().build()).get();

    InsertProcessMutation.Insert insert = ((InsertProcessMutation.Data) data).getProcess().getInsert();

    ProcessPart part = insert.getFragments().getProcessPart();
    assertThat(part.getKey().getName(), is(factory.processName));

    SpecificationPart specificationPart = part.getSpecification().getFragments().getSpecificationPart();
    assertThat(specificationPart.getDescription().get(), is(factory.description));
    assertThat(specificationPart.getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Override
  public void update() {
    Object data = client.getOptionalData(factory.updateProcessMutationBuilder().build()).get();

    UpdateProcessMutation.Update update = ((UpdateProcessMutation.Data) data).getProcess().getUpdate();

    ProcessPart part = update.getFragments().getProcessPart();
    assertThat(part.getKey().getName(), is(factory.processName));

    SpecificationPart specificationPart = part.getSpecification().getFragments().getSpecificationPart();
    assertThat(specificationPart.getDescription().get(), is(factory.description));
    assertThat(specificationPart.getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Override
  public void upsert() {
    Object data = client.getOptionalData(factory.upsertProcessMutationBuilder().build()).get();

    UpsertProcessMutation.Upsert upsert = ((UpsertProcessMutation.Data) data).getProcess().getUpsert();

    ProcessPart part = upsert.getFragments().getProcessPart();
    assertThat(part.getKey().getName(), is(factory.processName));

    SpecificationPart specificationPart = part.getSpecification().getFragments().getSpecificationPart();
    assertThat(specificationPart.getDescription().get(), is(factory.description));
    assertThat(specificationPart.getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Override
  public void delete() {
    setFactorySuffix("delete");

    Object data = client.getOptionalData(factory.deleteProcessMutationBuilder().build()).get();
    boolean result = ((DeleteProcessMutation.Data) data).getProcess().isDelete();

    assertTrue(result);
  }

  @Override
  public void updateStatus() {
    client.getOptionalData(factory.upsertProcessMutationBuilder().build()).get();
    Object data = client.getOptionalData(factory.updateProcessStatusBuilder().build()).get();

    UpdateProcessStatusMutation.UpdateStatus update =
        ((UpdateProcessStatusMutation.Data) data).getProcess().getUpdateStatus();

    ProcessPart part = update.getFragments().getProcessPart();

    assertThat(part.getSpecification().getFragments().getSpecificationPart().getDescription().get(), is(factory.description));
    assertThat(part.getStatus().get().getFragments().getStatusPart().getAgentStatus().get("skey").asText(), is("svalue"));
  }

  @Override
  public void queryByKey() {

    ProcessKeyInput input = factory.processKeyInputBuilder().build();

    try {
      client.getOptionalData(ProcessQuery.builder().key(input).build()).get();
    } catch (RuntimeException e) {
      assertEquals(e.getMessage(), "No value present");
    }

    client.getOptionalData(factory.upsertProcessMutationBuilder().build()).get();

    ProcessQuery.Data after = (ProcessQuery.Data) client.getOptionalData(ProcessQuery.builder().key(input).build()).get();

    assertEquals(after.getProcess().getByKey().get().getFragments().getProcessPart().getKey().getName(), input.name());
  }

  @Override
  public void queryByRegex() {

    setFactorySuffix("query_by_regex");

    ProcessKeyQuery query = ProcessKeyQuery.builder().nameRegex(".*query_by_regex.*").build();

    ProcesssQuery.Data before = (ProcesssQuery.Data) client.getOptionalData(ProcesssQuery.builder().key(query).build()).get();

    Response r = client.invoke(factory.upsertProcessMutationBuilder().build());

    ProcesssQuery.Data after = (ProcesssQuery.Data) client.getOptionalData(ProcesssQuery.builder().key(query).build()).get();

    assertNotNull(after.getProcess().getByQuery().get(0)
        .getFragments().getProcessPart().getStatus().get()
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
    client.createConsumer(factory);
    client.createStreamBinding(factory);
  }

  @Override
  public void queryByInvalidKey() {
    ProcessKeyInput input = factory.processKeyInputBuilder().name("disnae_exist").build();
    assertFalse(client.getProcess(input).isPresent());
  }
}
