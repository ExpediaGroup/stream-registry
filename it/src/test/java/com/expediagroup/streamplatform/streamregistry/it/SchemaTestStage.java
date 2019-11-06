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

import com.expediagroup.streamplatform.streamregistry.graphql.client.InsertSchemaMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.SchemaQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.SchemasQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpdateSchemaMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpdateSchemaStatusMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpsertSchemaMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.SchemaKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.SchemaKeyQuery;
import com.expediagroup.streamplatform.streamregistry.it.helpers.AbstractTestStage;

public class SchemaTestStage extends AbstractTestStage {

  @Override
  public void create() {
    Object data = client.getData(factory.insertSchemaMutationBuilder().build());

    InsertSchemaMutation.Insert upsert = ((InsertSchemaMutation.Data) data).getSchema().getInsert();

    assertThat(upsert.getSpecification().getDescription().get(), is(factory.description));
    assertThat(upsert.getSpecification().getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Override
  public void update() {

    Object data = client.getData(factory.updateSchemaMutationBuilder().build());

    UpdateSchemaMutation.Update upsert = ((UpdateSchemaMutation.Data) data).getSchema().getUpdate();

    assertThat(upsert.getSpecification().getDescription().get(), is(factory.description));
    assertThat(upsert.getSpecification().getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Override
  public void upsert() {

    Object data = client.getData(factory.upsertSchemaMutationBuilder().build());

    UpsertSchemaMutation.Upsert upsert = ((UpsertSchemaMutation.Data) data).getSchema().getUpsert();

    assertThat(upsert.getSpecification().getDescription().get(), is(factory.description));
    assertThat(upsert.getSpecification().getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Override
  public void updateStatus() {
    client.getData(factory.upsertSchemaMutationBuilder().build());
    Object data = client.getData(factory.updateSchemaStatusBuilder().build());

    UpdateSchemaStatusMutation.UpdateStatus update =
        ((UpdateSchemaStatusMutation.Data) data).getSchema().getUpdateStatus();

    assertThat(update.getSpecification().getDescription().get(), is(factory.description));
    assertThat(update.getStatus().get().getAgentStatus().get("skey").asText(), is("svalue"));
  }

  @Override
  public void queryByKey() {

    SchemaKeyInput input = factory.schemaKeyInputBuilder().build();

    try {
      client.getData(SchemaQuery.builder().key(input).build());
    } catch (RuntimeException e) {
      assertEquals(e.getMessage(), "No value present");
    }

    client.getData(factory.upsertSchemaMutationBuilder().build());

    SchemaQuery.Data after = (SchemaQuery.Data) client.getData(SchemaQuery.builder().key(input).build());

    assertEquals(after.getSchema().getByKey().getKey().getName(), input.name());
  }

  @Override
  public void queryByRegex() {

    setFactorySuffix("query_by_regex");

    SchemaKeyQuery query = SchemaKeyQuery.builder().domainRegex("domain_name.*").build();

    SchemasQuery.Data before = (SchemasQuery.Data) client.getData(SchemasQuery.builder().key(query).build());

    client.invoke(factory.upsertSchemaMutationBuilder().build());

    SchemasQuery.Data after = (SchemasQuery.Data) client.getData(SchemasQuery.builder().key(query).build());

    assertEquals(after.getSchema().getByQuery().size(), before.getSchema().getByQuery().size() + 1);
  }

  @Override
  public void createRequiredDatastoreState() {
    client.createDomain(factory);
  }
}
