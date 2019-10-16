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
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.apollographql.apollo.api.Mutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.InsertConsumerBindingMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.InsertConsumerMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpdateConsumerBindingMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpdateConsumerBindingStatusMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpdateConsumerMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpsertConsumerBindingMutation;
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

  }

  @Override
  public void queryByRegex() {

  }

  @Override
  public void createRequiredDatastoreState() {

  }
}
