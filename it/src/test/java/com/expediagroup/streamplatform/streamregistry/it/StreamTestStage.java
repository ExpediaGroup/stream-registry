/**
 * Copyright (C) 2018-2025 Expedia, Inc.
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

import com.apollographql.apollo.api.Response;

import com.expediagroup.streamplatform.streamregistry.graphql.client.test.DeleteStreamMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.InsertStreamMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.StreamQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.StreamsQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.UpdateStreamMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.UpdateStreamStatusMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.UpsertStreamMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.fragment.SpecificationPart;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.fragment.StreamPart;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.type.SchemaKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.type.StreamKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.type.StreamKeyQuery;
import com.expediagroup.streamplatform.streamregistry.it.helpers.AbstractTestStage;

public class StreamTestStage extends AbstractTestStage {

  @Override
  public void create() {
    Object data = client.getOptionalData(factory.insertStreamMutationBuilder().build()).get();

    InsertStreamMutation.Insert insert = ((InsertStreamMutation.Data) data).getStream().getInsert();

    StreamPart part = insert.getFragments().getStreamPart();
    assertThat(part.getKey().getName(), is(factory.streamName));

    SpecificationPart specificationPart = part.getSpecification().getFragments().getSpecificationPart();
    assertThat(specificationPart.getDescription().get(), is(factory.description));
    assertThat(specificationPart.getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Override
  public void update() {
    Object data = client.getOptionalData(factory.updateStreamMutationBuilder().build()).get();

    UpdateStreamMutation.Update update = ((UpdateStreamMutation.Data) data).getStream().getUpdate();

    StreamPart part = update.getFragments().getStreamPart();
    assertThat(part.getKey().getName(), is(factory.streamName));

    SpecificationPart specificationPart = part.getSpecification().getFragments().getSpecificationPart();
    assertThat(specificationPart.getDescription().get(), is(factory.description));
    assertThat(specificationPart.getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Override
  public void upsert() {

    try {
      client.getOptionalData(factory.upsertStreamMutationBuilder()
          .schema(null)
          .build()).get();
    } catch (RuntimeException ex) {
      assertEquals("Schema does not exist", ex.getMessage());
    }

    try {
      SchemaKeyInput nonExisting = SchemaKeyInput.builder()
          .domain(factory.domainName)
          .name("nonExisting")
          .build();
      client.getOptionalData(factory.upsertStreamMutationBuilder()
          .schema(nonExisting)
          .build()).get();
    } catch (RuntimeException ex) {
      assertEquals("Schema does not exist", ex.getMessage());
    }

    Object data = client.getOptionalData(factory.upsertStreamMutationBuilder().build()).get();

    UpsertStreamMutation.Upsert upsert = ((UpsertStreamMutation.Data) data).getStream().getUpsert();

    StreamPart part = upsert.getFragments().getStreamPart();
    assertThat(part.getKey().getName(), is(factory.streamName));

    SpecificationPart specificationPart = part.getSpecification().getFragments().getSpecificationPart();
    assertThat(specificationPart.getDescription().get(), is(factory.description));
    assertThat(specificationPart.getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Override
  public void delete() {
    setFactorySuffix("delete");

    Object data = client.getOptionalData(factory.deleteStreamMutationBuilder().build()).get();
    boolean result = ((DeleteStreamMutation.Data) data).getStream().isDelete();

    assertTrue(result);
  }

  @Override
  public void updateStatus() {
    client.getOptionalData(factory.upsertStreamMutationBuilder().build()).get();
    Object data = client.getOptionalData(factory.updateStreamStatusBuilder().build()).get();

    UpdateStreamStatusMutation.UpdateStatus update =
        ((UpdateStreamStatusMutation.Data) data).getStream().getUpdateStatus();

    StreamPart part = update.getFragments().getStreamPart();

    assertThat(part.getSpecification().getFragments().getSpecificationPart().getDescription().get(), is(factory.description));
    assertThat(part.getStatus().get().getFragments().getStatusPart().getAgentStatus().get("skey").asText(), is("svalue"));
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

    assertEquals(after.getStream().getByKey().get().getFragments().getStreamPart().getKey().getName(), input.name());
  }

  @Override
  public void queryByRegex() {

    setFactorySuffix("query_by_regex");

    StreamKeyQuery query = StreamKeyQuery.builder().nameRegex(".*query_by_regex.*").build();

    StreamsQuery.Data before = (StreamsQuery.Data) client.getOptionalData(StreamsQuery.builder().key(query).build()).get();

    Response r = client.invoke(factory.upsertStreamMutationBuilder().build());

    StreamsQuery.Data after = (StreamsQuery.Data) client.getOptionalData(StreamsQuery.builder().key(query).build()).get();

    assertNotNull(after.getStream().getByQuery().get(0)
        .getFragments().getStreamPart().getStatus().get()
        .getFragments().getStatusPart().getAgentStatus());
  }

  @Override
  public void createRequiredDatastoreState() {
    client.createDomain(factory);
    client.createSchema(factory);
  }

  @Override
  public void queryByInvalidKey() {
    StreamKeyInput input = factory.streamKeyInputBuilder().name("disnae_exist").build();
    assertFalse(client.getStream(input).isPresent());
  }
}
