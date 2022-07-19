/**
 * Copyright (C) 2018-2022 Expedia, Inc.
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
import static org.junit.Assert.assertTrue;

import com.apollographql.apollo.api.Mutation;

import com.expediagroup.streamplatform.streamregistry.graphql.client.test.DeleteDomainMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.DomainQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.DomainsQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.InsertDomainMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.UpdateDomainMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.UpdateDomainStatusMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.UpsertDomainMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.fragment.DomainPart;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.fragment.SpecificationPart;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.type.DomainKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.type.DomainKeyQuery;
import com.expediagroup.streamplatform.streamregistry.it.helpers.AbstractTestStage;

public class DomainTestStage extends AbstractTestStage {

  @Override
  public void create() {

    setFactorySuffix("create");

    assertMutationFails(factory.updateDomainMutationBuilder().build());

    Object data = client.getOptionalData(factory.insertDomainMutationBuilder().build()).get();

    InsertDomainMutation.Insert insert = ((InsertDomainMutation.Data) data).getDomain().getInsert();

    DomainPart part = insert.getFragments().getDomainPart();
    assertThat(part.getKey().getName(), is(factory.domainName));

    SpecificationPart specificationPart = part.getSpecification().getFragments().getSpecificationPart();
    assertThat(specificationPart.getDescription().get(), is(factory.description));
    assertThat(specificationPart.getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Override
  public void update() {

    setFactorySuffix("update");

    Mutation updateMutation = factory.updateDomainMutationBuilder().build();

    assertMutationFails(updateMutation);

    client.invoke(factory.upsertDomainMutationBuilder().build());

    UpdateDomainMutation.Update update =
        ((UpdateDomainMutation.Data) client.getOptionalData(updateMutation).get()).getDomain().getUpdate();

    DomainPart domainPart = update.getFragments().getDomainPart();
    assertThat(domainPart.getKey().getName(), is(factory.domainName));

    SpecificationPart specificationPart = domainPart.getSpecification().getFragments().getSpecificationPart();
    assertThat(specificationPart.getDescription().get(), is(factory.description));
    assertThat(specificationPart.getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Override
  public void upsert() {

    setFactorySuffix("upsert");

    Object data = client.getOptionalData(factory.upsertDomainMutationBuilder().build()).get();

    UpsertDomainMutation.Upsert upsert = ((UpsertDomainMutation.Data) data).getDomain().getUpsert();

    DomainPart domainPart = upsert.getFragments().getDomainPart();
    assertThat(domainPart.getKey().getName(), is(factory.domainName));

    SpecificationPart specificationPart = domainPart.getSpecification().getFragments().getSpecificationPart();
    assertThat(specificationPart.getDescription().get(), is(factory.description));
    assertThat(specificationPart.getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Override
  public void delete() {
    setFactorySuffix("delete");

    Object data = client.getOptionalData(factory.deleteDomainMutationBuilder().build()).get();
    boolean result = ((DeleteDomainMutation.Data) data).getDomain().isDelete();

    assertTrue(result);
  }

  @Override
  public void updateStatus() {
    client.getOptionalData(factory.upsertDomainMutationBuilder().build()).get();
    Object data = client.getOptionalData(factory.updateDomainStatusMutation().build()).get();

    UpdateDomainStatusMutation.UpdateStatus update =
        ((UpdateDomainStatusMutation.Data) data).getDomain().getUpdateStatus();

    DomainPart part = update.getFragments().getDomainPart();
    SpecificationPart specificationPart = part.getSpecification().getFragments().getSpecificationPart();
    assertThat(specificationPart.getDescription().get(), is(factory.description));
  }

  @Override
  public void queryByKey() {

    DomainKeyInput input = factory.domainKeyInputBuilder().build();

    try {
      client.getOptionalData(DomainQuery.builder().key(input).build()).get();
    } catch (RuntimeException e) {
      assertEquals(e.getMessage(), "No value present");
    }

    client.getOptionalData(factory.upsertDomainMutationBuilder().build()).get();

    DomainQuery.Data after = (DomainQuery.Data) client.getOptionalData(DomainQuery.builder().key(input).build()).get();

    assertEquals(after.getDomain().getByKey().get().getFragments().getDomainPart().getKey().getName(), input.name());
  }

  @Override
  public void queryByRegex() {

    setFactorySuffix("query_by_regex");

    DomainKeyQuery query = DomainKeyQuery.builder().nameRegex(".*").build();

    DomainsQuery.Data before = (DomainsQuery.Data) client.getOptionalData(DomainsQuery.builder().key(query).build()).get();

    client.invoke(factory.upsertDomainMutationBuilder().build());

    DomainsQuery.Data after = (DomainsQuery.Data) client.getOptionalData(DomainsQuery.builder().key(query).build()).get();

    assertEquals(before.getDomain().getByQuery().size() + 1, after.getDomain().getByQuery().size());
    assertNotNull(after.getDomain().getByQuery().get(0)
        .getFragments().getDomainPart().getStatus().get()
        .getFragments().getStatusPart().getAgentStatus());
  }

  @Override
  public void createRequiredDatastoreState() {

  }

  @Override
  public void queryByInvalidKey() {
    DomainKeyInput input = factory.domainKeyInputBuilder().name("disnae_exist").build();
    assertFalse(client.getDomain(input).isPresent());
  }
}
