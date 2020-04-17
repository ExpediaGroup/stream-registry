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

import static com.expediagroup.streamplatform.streamregistry.core.handlers.IdentityHandler.DEFAULT;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import com.apollographql.apollo.api.Response;

import com.expediagroup.streamplatform.streamregistry.graphql.client.InsertStreamMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.StreamQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.StreamsQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpdateStreamMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpdateStreamStatusMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpsertStreamMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.StreamKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.StreamKeyQuery;
import com.expediagroup.streamplatform.streamregistry.it.helpers.AbstractTestStage;

public class StreamTestStage extends AbstractTestStage {

  @Override
  public void create() {
    Object data = client.getOptionalData(factory.insertStreamMutationBuilder().build()).get();

    InsertStreamMutation.Insert Insert = ((InsertStreamMutation.Data) data).getStream().getInsert();

    assertThat(Insert.getSpecification().getDescription().get(), is(factory.description));
  }

  @Override
  public void update() {
    Object data = client.getOptionalData(factory.updateStreamMutationBuilder().build()).get();

    UpdateStreamMutation.Update Update = ((UpdateStreamMutation.Data) data).getStream().getUpdate();

    assertThat(Update.getSpecification().getDescription().get(), is(factory.description));
  }

  @Override
  public void upsert() {
    //insert scenario without schema
    try {
      client.getOptionalData(factory.upsertStreamMutationInsertWithoutSchemaBuilder().build()).get();
    } catch (RuntimeException ex) {
      assertEquals("Can't create because Schema is required", ex.getMessage());
    }

    //upsert scenario with a different schema
    try {
      client.getOptionalData(factory.upsertStreamMutationUpsertWithDifferentSchemaKeySchemaBuilder().build()).get();
    } catch (RuntimeException ex) {
      assertEquals("Can't update because schema change is not allowed", ex.getMessage());
    }

    //update scenario
    Object data = client.getOptionalData(factory.upsertStreamMutationBuilder().build()).get();

    UpsertStreamMutation.Upsert upsert = ((UpsertStreamMutation.Data) data).getStream().getUpsert();

    assertThat(upsert.getSpecification().getDescription().get(), is(factory.description));
  }

  @Override
  public void updateStatus() {
    client.getOptionalData(factory.upsertStreamMutationBuilder().build()).get();
    Object data = client.getOptionalData(factory.updateStreamStatusBuilder().build()).get();

    UpdateStreamStatusMutation.UpdateStatus update =
        ((UpdateStreamStatusMutation.Data) data).getStream().getUpdateStatus();

    assertThat(update.getSpecification().getDescription().get(), is(factory.description));
    assertThat(update.getStatus().get().getAgentStatus().get("skey").asText(), is("svalue"));
  }

  @Override
  public void queryByKey() {

    StreamKeyInput input = factory.streamKeyInputBuilder().build();

    try {
      client.getOptionalData(StreamQuery.builder().key(input).build()).get();
    } catch (RuntimeException e) {
      assertEquals(e.getMessage(), "No value present");
    }

    client.getOptionalData(factory.upsertStreamMutationBuilder().build()).get();

    StreamQuery.Data after = (StreamQuery.Data) client.getOptionalData(StreamQuery.builder().key(input).build()).get();

    assertEquals(after.getStream().getByKey().get().getKey().getName(), input.name());

    assertEquals(after.getStream().getByKey().get().getSchema().getSpecification().getType(), DEFAULT);
  }

  @Override
  public void queryByRegex() {

    setFactorySuffix("query_by_regex");

    StreamKeyQuery query = StreamKeyQuery.builder().nameRegex(".*query_by_regex.*").build();

    StreamsQuery.Data before = (StreamsQuery.Data) client.getOptionalData(StreamsQuery.builder().key(query).build()).get();

    Response r = client.invoke(factory.upsertStreamMutationBuilder().build());

    StreamsQuery.Data after = (StreamsQuery.Data) client.getOptionalData(StreamsQuery.builder().key(query).build()).get();

    assertEquals(before.getStream().getByQuery().size() + 1, after.getStream().getByQuery().size());
    assertNotNull(after.getStream().getByQuery().get(0).getStatus().get().getAgentStatus());
  }

  @Override
  public void createRequiredDatastoreState() {
    client.createDomain(factory);
    client.createSchema(factory);
  }

  @Override
  public void queryByInvalidKey() {
    StreamKeyInput input = factory.streamKeyInputBuilder().name("disnae_exist").build();
    assertTrue(client.getStream(input).isEmpty());
  }
}
