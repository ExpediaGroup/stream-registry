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

import org.junit.Test;

import com.apollographql.apollo.api.Mutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.ConsumerBindingQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.ConsumerBindingsQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.DomainQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.DomainsQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.InsertConsumerBindingMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpdateConsumerBindingMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpdateConsumerBindingStatusMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpsertConsumerBindingMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.ConsumerBindingKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.ConsumerBindingKeyQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.DomainKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.DomainKeyQuery;
import com.expediagroup.streamplatform.streamregistry.it.helpers.ObjectIT;

public class ConsumerBindingIT extends ObjectIT {

  @Override
  public void create() {

    setFactorySuffix("create");

    assertMutationFails(factory.updateConsumerBindingMutationBuilder().build());

    Object data = client.getData(factory.insertConsumerBindingMutationBuilder().build());

    InsertConsumerBindingMutation.Insert insert = ((InsertConsumerBindingMutation.Data) data).getConsumerBinding().getInsert();

    assertThat(insert.getKey().getStreamName(), is(factory.streamName));

    assertThat(insert.getSpecification().getDescription().get(), is(factory.description));
    assertThat(insert.getSpecification().getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Test
  public void update() {

    setFactorySuffix("update");

    Mutation updateMutation = factory.updateConsumerBindingMutationBuilder().build();

    assertMutationFails(updateMutation);

    client.invoke(factory.insertConsumerBindingMutationBuilder().build());

    UpdateConsumerBindingMutation.Update update = ((UpdateConsumerBindingMutation.Data) client.getData(updateMutation))
        .getConsumerBinding().getUpdate();

    assertThat(update.getKey().getStreamName(), is(factory.streamName));

    assertThat(update.getSpecification().getDescription().get(), is(factory.description));
    assertThat(update.getSpecification().getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Override
  public void upsert() {

    setFactorySuffix("upsert");

    Object data = client.getData(factory.upsertConsumerBindingMutationBuilder().build());

    UpsertConsumerBindingMutation.Upsert upsert = ((UpsertConsumerBindingMutation.Data) data).getConsumerBinding().getUpsert();

    assertThat(upsert.getSpecification().getDescription().get(), is(factory.description));
    assertThat(upsert.getSpecification().getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Override
  public void updateStatus() {
    client.getData(factory.upsertConsumerBindingMutationBuilder().build());
    Object data = client.getData(factory.updateConsumerBindingStatusBuilder().build());

    UpdateConsumerBindingStatusMutation.UpdateStatus update =
        ((UpdateConsumerBindingStatusMutation.Data) data).getConsumerBinding().getUpdateStatus();

    assertThat(update.getSpecification().getDescription().get(), is(factory.description));
    assertThat(update.getStatus().get().getAgentStatus().get("skey").asText(), is("svalue"));
  }


  @Override
  public void queryByKey() {

    ConsumerBindingKeyInput input = factory.consumerBindingKeyInputBuilder().build();

    try {
      client.getData(ConsumerBindingQuery.builder().key(input).build());
    } catch (RuntimeException e) {
      assertEquals(e.getMessage(), "No value present");
    }

    client.getData(factory.upsertConsumerBindingMutationBuilder().build());

    ConsumerBindingQuery.Data after = (ConsumerBindingQuery.Data) client.getData(ConsumerBindingQuery.builder().key(input).build());

    assertEquals(after.getConsumerBinding().getKey().getStreamName(), input.streamName());
  }

  @Override
  public void queryByRegex() {

    setFactorySuffix("queryByRegex");

    ConsumerBindingKeyQuery query = ConsumerBindingKeyQuery.builder().consumerNameRegex(".*").build();

    ConsumerBindingsQuery.Data before = (ConsumerBindingsQuery.Data) client.getData(ConsumerBindingsQuery.builder().key(query).build());

    client.invoke(factory.upsertConsumerBindingMutationBuilder().build());

    ConsumerBindingsQuery.Data after = (ConsumerBindingsQuery.Data) client.getData(ConsumerBindingsQuery.builder().key(query).build());

    assertEquals(before.getConsumerBindings().size() + 1, after.getConsumerBindings().size());
  }

  @Override
  public void createRequiredDatastoreState() {

  }
}
