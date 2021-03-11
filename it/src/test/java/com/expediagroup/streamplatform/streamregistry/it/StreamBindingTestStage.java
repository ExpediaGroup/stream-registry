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

import com.expediagroup.streamplatform.streamregistry.graphql.client.test.DeleteStreamBindingMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.InsertStreamBindingMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.StreamBindingQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.StreamBindingsQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.UpdateStreamBindingMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.UpdateStreamBindingStatusMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.UpsertStreamBindingMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.fragment.SpecificationPart;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.fragment.StreamBindingPart;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.type.StreamBindingKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.type.StreamBindingKeyQuery;
import com.expediagroup.streamplatform.streamregistry.it.helpers.AbstractTestStage;
import org.junit.Ignore;

public class StreamBindingTestStage extends AbstractTestStage {

  @Override
  public void create() {
    Object data = client.getOptionalData(factory.insertStreamBindingMutationBuilder().build()).get();

    InsertStreamBindingMutation.Insert insert = ((InsertStreamBindingMutation.Data) data).getStreamBinding().getInsert();

    StreamBindingPart part = insert.getFragments().getStreamBindingPart();
    assertThat(part.getKey().getStreamName(), is(factory.streamName));

    SpecificationPart specificationPart = part.getSpecification().getFragments().getSpecificationPart();
    assertThat(specificationPart.getDescription().get(), is(factory.description));
    assertThat(specificationPart.getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Override
  public void update() {
    Object data = client.getOptionalData(factory.updateStreamBindingMutationBuilder().build()).get();

    UpdateStreamBindingMutation.Update update = ((UpdateStreamBindingMutation.Data) data).getStreamBinding().getUpdate();

    StreamBindingPart part = update.getFragments().getStreamBindingPart();
    assertThat(part.getKey().getStreamName(), is(factory.streamName));

    SpecificationPart specificationPart = part.getSpecification().getFragments().getSpecificationPart();
    assertThat(specificationPart.getDescription().get(), is(factory.description));
    assertThat(specificationPart.getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Override
  public void upsert() {

    Object data = client.getOptionalData(factory.upsertStreamBindingMutationBuilder().build()).get();

    UpsertStreamBindingMutation.Upsert upsert = ((UpsertStreamBindingMutation.Data) data).getStreamBinding().getUpsert();

    StreamBindingPart part = upsert.getFragments().getStreamBindingPart();
    assertThat(part.getKey().getStreamName(), is(factory.streamName));

    SpecificationPart specificationPart = part.getSpecification().getFragments().getSpecificationPart();
    assertThat(specificationPart.getDescription().get(), is(factory.description));
    assertThat(specificationPart.getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Override
  @Ignore
  public void delete() {
    setFactorySuffix("delete");

    Object data = client.getOptionalData(factory.deleteStreamBindingMutationBuilder().build()).get();

    boolean delete = ((DeleteStreamBindingMutation.Data) data).getStreamBinding().isDelete();

    assertTrue(delete);
  }

  @Override
  public void updateStatus() {
    client.getOptionalData(factory.upsertStreamBindingMutationBuilder().build()).get();
    Object data = client.getOptionalData(factory.updateStreamBindingStatusBuilder().build()).get();

    UpdateStreamBindingStatusMutation.UpdateStatus update =
        ((UpdateStreamBindingStatusMutation.Data) data).getStreamBinding().getUpdateStatus();

    StreamBindingPart part = update.getFragments().getStreamBindingPart();

    assertThat(part.getSpecification().getFragments().getSpecificationPart().getDescription().get(), is(factory.description));
    assertThat(part.getStatus().get().getFragments().getStatusPart().getAgentStatus().get("skey").asText(), is("svalue"));
  }

  @Override
  public void queryByKey() {

    StreamBindingKeyInput input = factory.streamBindingKeyInputBuilder().build();

    try {
      client.getOptionalData(StreamBindingQuery.builder().key(input).build()).get();
    } catch (RuntimeException e) {
      assertEquals(e.getMessage(), "No value present");
    }

    client.getOptionalData(factory.upsertStreamBindingMutationBuilder().build()).get();

    StreamBindingQuery.Data after = (StreamBindingQuery.Data) client.getOptionalData(StreamBindingQuery.builder().key(input).build()).get();

    assertEquals(after.getStreamBinding().getByKey().get().getFragments().getStreamBindingPart().getKey().getStreamName(), input.streamName());
  }

  @Override
  public void queryByRegex() {

    setFactorySuffix("query_by_regex");

    StreamBindingKeyQuery query = StreamBindingKeyQuery.builder().streamNameRegex("stream_name.*").build();

    StreamBindingsQuery.Data before = (StreamBindingsQuery.Data) client.getOptionalData(StreamBindingsQuery.builder().key(query).build()).get();

    client.invoke(factory.upsertStreamBindingMutationBuilder().build());

    StreamBindingsQuery.Data after = (StreamBindingsQuery.Data) client.getOptionalData(StreamBindingsQuery.builder().key(query).build()).get();

    assertEquals(after.getStreamBinding().getByQuery().size(), before.getStreamBinding().getByQuery().size() + 1);
    assertNotNull(after.getStreamBinding().getByQuery().get(0)
        .getFragments().getStreamBindingPart().getStatus().get()
        .getFragments().getStatusPart().getAgentStatus());
  }

  @Override
  public void createRequiredDatastoreState() {
    client.createDomain(factory);
    client.createSchema(factory);
    client.createStream(factory);
    client.createZone(factory);
    client.createInfrastructure(factory);
  }

  @Override
  public void queryByInvalidKey() {
    StreamBindingKeyInput input = factory.streamBindingKeyInputBuilder().streamName("disnae_exist").build();
    assertFalse(client.getStreamBinding(input).isPresent());
  }
}
