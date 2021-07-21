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
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.fragment.ProcessBindingPart;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.fragment.SpecificationPart;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.type.ProcessBindingKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.type.ProcessBindingKeyQuery;
import com.expediagroup.streamplatform.streamregistry.it.helpers.AbstractTestStage;

public class ProcessBindingTestStage extends AbstractTestStage {

  @Override
  public void create() {
    Object data = client.getOptionalData(factory.insertProcessBindingMutationBuilder().build()).get();

    InsertProcessBindingMutation.Insert insert = ((InsertProcessBindingMutation.Data) data).getProcessBinding().getInsert();

    ProcessBindingPart part = insert.getFragments().getProcessBindingPart();
    assertThat(part.getKey().getProcessName(), is(factory.processName));

    SpecificationPart specificationPart = part.getSpecification().getFragments().getSpecificationPart();
    assertThat(specificationPart.getDescription().get(), is(factory.description));
    assertThat(specificationPart.getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Override
  public void update() {
    Object data = client.getOptionalData(factory.updateProcessBindingMutationBuilder().build()).get();

    UpdateProcessBindingMutation.Update update = ((UpdateProcessBindingMutation.Data) data).getProcessBinding().getUpdate();

    ProcessBindingPart part = update.getFragments().getProcessBindingPart();
    assertThat(part.getKey().getProcessName(), is(factory.processName));

    SpecificationPart specificationPart = part.getSpecification().getFragments().getSpecificationPart();
    assertThat(specificationPart.getDescription().get(), is(factory.description));
    assertThat(specificationPart.getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Override
  public void upsert() {
    Object data = client.getOptionalData(factory.upsertProcessBindingMutationBuilder().build()).get();

    UpsertProcessBindingMutation.Upsert upsert = ((UpsertProcessBindingMutation.Data) data).getProcessBinding().getUpsert();

    ProcessBindingPart part = upsert.getFragments().getProcessBindingPart();
    assertThat(part.getKey().getProcessName(), is(factory.processName));

    SpecificationPart specificationPart = part.getSpecification().getFragments().getSpecificationPart();
    assertThat(specificationPart.getDescription().get(), is(factory.description));
    assertThat(specificationPart.getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Override
  public void delete() {
    setFactorySuffix("delete");

    Object data = client.getOptionalData(factory.deleteProcessBindingMutationBuilder().build()).get();
    boolean result = ((DeleteProcessBindingMutation.Data) data).getProcessBinding().isDelete();

    assertTrue(result);
  }

  @Override
  public void updateStatus() {
    client.getOptionalData(factory.upsertProcessBindingMutationBuilder().build()).get();
    Object data = client.getOptionalData(factory.updateProcessBindingStatusBuilder().build()).get();

    UpdateProcessBindingStatusMutation.UpdateStatus update =
        ((UpdateProcessBindingStatusMutation.Data) data).getProcessBinding().getUpdateStatus();

    ProcessBindingPart part = update.getFragments().getProcessBindingPart();

    assertThat(part.getSpecification().getFragments().getSpecificationPart().getDescription().get(), is(factory.description));
    assertThat(part.getStatus().get().getFragments().getStatusPart().getAgentStatus().get("skey").asText(), is("svalue"));
  }

  @Override
  public void queryByKey() {

    ProcessBindingKeyInput input = factory.processBindingKeyInputBuilder().build();

    try {
      client.getOptionalData(ProcessBindingQuery.builder().key(input).build()).get();
    } catch (RuntimeException e) {
      assertEquals(e.getMessage(), "No value present");
    }

    client.getOptionalData(factory.upsertProcessBindingMutationBuilder().build()).get();

    ProcessBindingQuery.Data after = (ProcessBindingQuery.Data) client.getOptionalData(ProcessBindingQuery.builder().key(input).build()).get();

    assertEquals(after.getProcessBinding().getByKey().get().getFragments().getProcessBindingPart().getKey().getProcessName(), input.processName());
  }

  @Override
  public void queryByRegex() {

    setFactorySuffix("query_by_regex");

    ProcessBindingKeyQuery query = ProcessBindingKeyQuery.builder().processNameRegex(".*query_by_regex.*").build();

    ProcessBindingsQuery.Data before = (ProcessBindingsQuery.Data) client.getOptionalData(ProcessBindingsQuery.builder().key(query).build()).get();

    Response r = client.invoke(factory.upsertProcessBindingMutationBuilder().build());

    ProcessBindingsQuery.Data after = (ProcessBindingsQuery.Data) client.getOptionalData(ProcessBindingsQuery.builder().key(query).build()).get();

    assertNotNull(after.getProcessBinding().getByQuery().get(0)
        .getFragments().getProcessBindingPart().getStatus().get()
        .getFragments().getStatusPart().getAgentStatus());
  }

  @Override
  public void createRequiredDatastoreState() {
    client.createDomain(factory);
  }

  @Override
  public void queryByInvalidKey() {
    ProcessBindingKeyInput input = factory.processBindingKeyInputBuilder().processName("disnae_exist").build();
    assertFalse(client.getProcessBinding(input).isPresent());
  }
}
