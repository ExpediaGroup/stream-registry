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

import com.expediagroup.streamplatform.streamregistry.graphql.client.InsertStreamBindingMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.StreamBindingQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.StreamBindingsQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpdateStreamBindingMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpdateStreamBindingStatusMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpsertStreamBindingMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.StreamBindingKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.StreamBindingKeyQuery;
import com.expediagroup.streamplatform.streamregistry.it.helpers.ObjectIT;

public class StreamBindingIT extends ObjectIT {

  @Override
  public void create() {
    Object data = client.getData(factory.insertStreamBindingMutationBuilder().build());

    InsertStreamBindingMutation.Insert insert = ((InsertStreamBindingMutation.Data) data).getStreamBinding().getInsert();

    assertThat(insert.getSpecification().getDescription().get(), is(factory.description));
    assertThat(insert.getSpecification().getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Override
  public void update() {
    Object data = client.getData(factory.updateStreamBindingMutationBuilder().build());

    UpdateStreamBindingMutation.Update update = ((UpdateStreamBindingMutation.Data) data).getStreamBinding().getUpdate();

    assertThat(update.getSpecification().getDescription().get(), is(factory.description));
    assertThat(update.getSpecification().getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Override
  public void upsert() {

    Object data = client.getData(factory.upsertStreamBindingMutationBuilder().build());

    UpsertStreamBindingMutation.Upsert upsert = ((UpsertStreamBindingMutation.Data) data).getStreamBinding().getUpsert();

    assertThat(upsert.getSpecification().getDescription().get(), is(factory.description));
    assertThat(upsert.getSpecification().getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Override
  public void updateStatus() {
    client.getData(factory.upsertStreamBindingMutationBuilder().build());
    Object data = client.getData(factory.updateStreamBindingStatusBuilder().build());

    UpdateStreamBindingStatusMutation.UpdateStatus update =
        ((UpdateStreamBindingStatusMutation.Data) data).getStreamBinding().getUpdateStatus();

    assertThat(update.getSpecification().getDescription().get(), is(factory.description));
    assertThat(update.getStatus().get().getAgentStatus().get("skey").asText(), is("svalue"));
  }

  @Override
  public void queryByKey() {

    StreamBindingKeyInput input = factory.streamBindingKeyInputBuilder().build();

    try {
      client.getData(StreamBindingQuery.builder().key(input).build());
    } catch (RuntimeException e) {
      assertEquals(e.getMessage(), "No value present");
    }

    client.getData(factory.upsertStreamBindingMutationBuilder().build());

    StreamBindingQuery.Data after = (StreamBindingQuery.Data) client.getData(StreamBindingQuery.builder().key(input).build());

    assertEquals(after.getStreamBindingQuery().getStreamBinding().getKey().getStreamName(), input.streamName());
  }

  @Override
  public void queryByRegex() {

    setFactorySuffix("query_by_regex");

    StreamBindingKeyQuery query = StreamBindingKeyQuery.builder().streamNameRegex("stream_name.*").build();

    StreamBindingsQuery.Data before = (StreamBindingsQuery.Data) client.getData(StreamBindingsQuery.builder().key(query).build());

    client.invoke(factory.upsertStreamBindingMutationBuilder().build());

    StreamBindingsQuery.Data after = (StreamBindingsQuery.Data) client.getData(StreamBindingsQuery.builder().key(query).build());

    assertEquals(after.getStreamBindingQuery().getStreamBindings().size(), before.getStreamBindingQuery().getStreamBindings().size() + 1);
  }

  @Override
  public void createRequiredDatastoreState() {
    client.createStream(factory);
  }
}
