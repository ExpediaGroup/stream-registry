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
import static org.junit.Assert.assertTrue;

import com.expediagroup.streamplatform.streamregistry.graphql.client.test.DeleteSchemaMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.InsertSchemaMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.SchemaQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.SchemasQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.UpdateSchemaMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.UpdateSchemaStatusMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.UpsertSchemaMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.fragment.SchemaPart;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.fragment.SpecificationPart;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.type.SchemaKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.type.SchemaKeyQuery;
import com.expediagroup.streamplatform.streamregistry.it.helpers.AbstractTestStage;

public class SchemaTestStage extends AbstractTestStage {

  @Override
  public void create() {
    Object data = client.getOptionalData(factory.insertSchemaMutationBuilder().build()).get();

    InsertSchemaMutation.Insert insert = ((InsertSchemaMutation.Data) data).getSchema().getInsert();

    SchemaPart part = insert.getFragments().getSchemaPart();
    assertThat(part.getKey().getName(), is(factory.schemaName));

    SpecificationPart specificationPart = part.getSpecification().getFragments().getSpecificationPart();
    assertThat(specificationPart.getDescription().get(), is(factory.description));
    assertThat(specificationPart.getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Override
  public void update() {

    Object data = client.getOptionalData(factory.updateSchemaMutationBuilder().build()).get();

    UpdateSchemaMutation.Update update = ((UpdateSchemaMutation.Data) data).getSchema().getUpdate();

    SchemaPart part = update.getFragments().getSchemaPart();
    assertThat(part.getKey().getName(), is(factory.schemaName));

    SpecificationPart specificationPart = part.getSpecification().getFragments().getSpecificationPart();
    assertThat(specificationPart.getDescription().get(), is(factory.description));
    assertThat(specificationPart.getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Override
  public void upsert() {

    Object data = client.getOptionalData(factory.upsertSchemaMutationBuilder().build()).get();

    UpsertSchemaMutation.Upsert upsert = ((UpsertSchemaMutation.Data) data).getSchema().getUpsert();

    SchemaPart part = upsert.getFragments().getSchemaPart();
    assertThat(part.getKey().getName(), is(factory.schemaName));

    SpecificationPart specificationPart = part.getSpecification().getFragments().getSpecificationPart();
    assertThat(specificationPart.getDescription().get(), is(factory.description));
    assertThat(specificationPart.getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Override
  public void delete() {
    setFactorySuffix("delete");

    Object data = client.getOptionalData(factory.deleteSchemaMutationBuilder().build()).get();

    boolean delete = ((DeleteSchemaMutation.Data) data).getSchema().isDelete();

    assertTrue(delete);
  }

  @Override
  public void updateStatus() {
    client.getOptionalData(factory.upsertSchemaMutationBuilder().build()).get();
    Object data = client.getOptionalData(factory.updateSchemaStatusBuilder().build()).get();

    UpdateSchemaStatusMutation.UpdateStatus update =
        ((UpdateSchemaStatusMutation.Data) data).getSchema().getUpdateStatus();

    SchemaPart part = update.getFragments().getSchemaPart();

    assertThat(part.getSpecification().getFragments().getSpecificationPart().getDescription().get(), is(factory.description));
    assertThat(part.getStatus().get().getFragments().getStatusPart().getAgentStatus().get("skey").asText(), is("svalue"));
  }

  @Override
  public void queryByKey() {

    SchemaKeyInput input = factory.schemaKeyInputBuilder().build();

    try {
      client.getOptionalData(SchemaQuery.builder().key(input).build()).get();
    } catch (RuntimeException e) {
      assertEquals(e.getMessage(), "No value present");
    }

    client.getOptionalData(factory.upsertSchemaMutationBuilder().build()).get();

    SchemaQuery.Data after = (SchemaQuery.Data) client.getOptionalData(SchemaQuery.builder().key(input).build()).get();

    assertEquals(after.getSchema().getByKey().get().getFragments().getSchemaPart().getKey().getName(), input.name());
  }

  @Override
  public void queryByRegex() {

    setFactorySuffix("query_by_regex");

    SchemaKeyQuery query = SchemaKeyQuery.builder().domainRegex("domain_name.*").build();

    SchemasQuery.Data before = (SchemasQuery.Data) client.getOptionalData(SchemasQuery.builder().key(query).build()).get();

    client.invoke(factory.upsertSchemaMutationBuilder().build());

    SchemasQuery.Data after = (SchemasQuery.Data) client.getOptionalData(SchemasQuery.builder().key(query).build()).get();

    assertEquals(after.getSchema().getByQuery().size(), before.getSchema().getByQuery().size() + 1);

    assertNotNull(after.getSchema().getByQuery().get(1)
        .getFragments().getSchemaPart().getStatus().get()
        .getFragments().getStatusPart().getAgentStatus());
  }

  @Override
  public void createRequiredDatastoreState() {
    client.createDomain(factory);
  }

  @Override
  public void queryByInvalidKey() {
    SchemaKeyInput input = factory.schemaKeyInputBuilder().name("disnae_exist").build();
    assertFalse(client.getSchema(input).isPresent());
  }
}
