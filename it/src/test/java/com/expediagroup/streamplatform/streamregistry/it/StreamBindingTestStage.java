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

import static junit.framework.TestCase.assertTrue;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.expediagroup.streamplatform.streamregistry.graphql.client.InsertStreamBindingMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.StreamBindingQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.StreamBindingsQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpdateStreamBindingMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpdateStreamBindingStatusMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpsertStreamBindingMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.StreamBindingKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.StreamBindingKeyQuery;
import com.expediagroup.streamplatform.streamregistry.it.helpers.AbstractTestStage;

public class StreamBindingTestStage extends AbstractTestStage {

  @Override
  public void create() {
    Object data = client.getOptionalData(factory.insertStreamBindingMutationBuilder().build()).get();

    InsertStreamBindingMutation.Insert insert = ((InsertStreamBindingMutation.Data) data).getStreamBinding().getInsert();

    assertThat(insert.getSpecification().getDescription().get(), is(factory.description));
    assertThat(insert.getSpecification().getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Override
  public void update() {
    Object data = client.getOptionalData(factory.updateStreamBindingMutationBuilder().build()).get();

    UpdateStreamBindingMutation.Update update = ((UpdateStreamBindingMutation.Data) data).getStreamBinding().getUpdate();

    assertThat(update.getSpecification().getDescription().get(), is(factory.description));
    assertThat(update.getSpecification().getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Override
  public void upsert() {

    Object data = client.getOptionalData(factory.upsertStreamBindingMutationBuilder().build()).get();

    UpsertStreamBindingMutation.Upsert upsert = ((UpsertStreamBindingMutation.Data) data).getStreamBinding().getUpsert();

    assertThat(upsert.getSpecification().getDescription().get(), is(factory.description));
    assertThat(upsert.getSpecification().getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Override
  public void updateStatus() {
    client.getOptionalData(factory.upsertStreamBindingMutationBuilder().build()).get();
    Object data = client.getOptionalData(factory.updateStreamBindingStatusBuilder().build()).get();

    UpdateStreamBindingStatusMutation.UpdateStatus update =
        ((UpdateStreamBindingStatusMutation.Data) data).getStreamBinding().getUpdateStatus();

    assertThat(update.getSpecification().getDescription().get(), is(factory.description));
    assertThat(update.getStatus().get().getAgentStatus().get("skey").asText(), is("svalue"));
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

    assertEquals(after.getStreamBinding().getByKey().get().getKey().getStreamName(), input.streamName());
  }

  @Override
  public void queryByRegex() {

    setFactorySuffix("query_by_regex");

    StreamBindingKeyQuery query = StreamBindingKeyQuery.builder().streamNameRegex("stream_name.*").build();

    StreamBindingsQuery.Data before = (StreamBindingsQuery.Data) client.getOptionalData(StreamBindingsQuery.builder().key(query).build()).get();

    client.invoke(factory.upsertStreamBindingMutationBuilder().build());

    StreamBindingsQuery.Data after = (StreamBindingsQuery.Data) client.getOptionalData(StreamBindingsQuery.builder().key(query).build()).get();

    assertEquals(after.getStreamBinding().getByQuery().size(), before.getStreamBinding().getByQuery().size() + 1);
  }

  @Override
  public void createRequiredDatastoreState() {
    client.createStream(factory);
  }

  @Override
  public void queryByInvalidKey() {
    StreamBindingKeyInput input = factory.streamBindingKeyInputBuilder().streamName("disnae_exist").build();
    assertTrue(client.getStreamBinding(input).isEmpty());
  }
}
