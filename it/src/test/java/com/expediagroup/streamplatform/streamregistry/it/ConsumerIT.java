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
import static org.junit.Assert.fail;

import org.junit.Test;

import com.expediagroup.streamplatform.streamregistry.graphql.client.InsertConsumerMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpdateConsumerMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpdateConsumerStatusMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpsertConsumerMutation;
import com.expediagroup.streamplatform.streamregistry.it.helpers.ObjectIT;

public class ConsumerIT extends ObjectIT {

  @Test
  public void upsert() {

    Object data = client.data(factory.upsertConsumerMutationBuilder().build());

    UpsertConsumerMutation.Upsert upsert = ((UpsertConsumerMutation.Data)data).getConsumer().getUpsert();

    assertThat(upsert.getKey().getName(), is(factory.consumerName.getValue()));

    assertThat(upsert.getSpecification().getDescription().get(), is(factory.description.getValue()));
    assertThat(upsert.getSpecification().getConfiguration().get(factory.key.toString()).asText(), is(factory.value.toString()));
  }

  @Override
  public void create() {
      Object data = client.data(factory.insertConsumerMutationBuilder().build());

      InsertConsumerMutation.Insert insert = ((InsertConsumerMutation.Data)data).getConsumer().getInsert();

      assertThat(insert.getKey().getName(), is(factory.consumerName.getValue()));

      assertThat(insert.getSpecification().getDescription().get(), is(factory.description.getValue()));
      assertThat(insert.getSpecification().getConfiguration().get(factory.key.toString()).asText(), is(factory.value.toString()));

    }

  @Test
  public void update() {

    try {
      client.data(factory.updateConsumerMutationBuilder().build());
      fail("Expected a ValidationException");
    }catch (RuntimeException e ) {
      assertEquals("Can't update because it doesn't exist",e.getMessage());
    }

    client.mutate(factory.insertConsumerMutationBuilder().build());

    Object data = client.data(factory.updateConsumerMutationBuilder().build());

    UpdateConsumerMutation.Update update = ((UpdateConsumerMutation.Data)data).getConsumer().getUpdate();

    assertThat(update.getKey().getName(), is(factory.consumerName.getValue()));

    assertThat(update.getSpecification().getDescription().get(), is(factory.description.getValue()));
    assertThat(update.getSpecification().getConfiguration().get(factory.key.toString()).asText(), is(factory.value.toString()));

  }

  @Test
  public void updateStatus() {

    client.data(factory.upsertConsumerMutationBuilder().build());

    Object data =  client.data(factory.updateConsumerStatusBuilder().build());

    UpdateConsumerStatusMutation.UpdateStatus update =
        ((UpdateConsumerStatusMutation.Data)data).getConsumer().getUpdateStatus();

    assertThat(update.getKey().getName(), is(factory.consumerName.getValue()));

    assertThat(update.getSpecification().getDescription().get(), is(factory.description.getValue()));
    assertThat(update.getSpecification().getConfiguration().get(factory.key.toString()).asText(), is(factory.value.toString()));

    assertThat(update.getStatus().get().getAgentStatus().get("skey").asText(), is("svalue"));

  }

  @Override
  public void queryByKey() {

  }

  @Override
  public void queryByRegex() {

  }
}
