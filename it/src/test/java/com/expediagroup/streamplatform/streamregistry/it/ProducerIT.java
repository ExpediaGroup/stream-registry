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

import com.apollographql.apollo.api.Mutation;

import com.expediagroup.streamplatform.streamregistry.graphql.client.InsertProducerMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpdateProducerMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpdateProducerStatusMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpsertProducerMutation;
import com.expediagroup.streamplatform.streamregistry.it.helpers.ObjectIT;

public class ProducerIT extends ObjectIT {

  @Override
  public void create() {

    Mutation createMutation = factory.insertProducerMutationBuilder().build();

    Object data = client.getData(createMutation);

    InsertProducerMutation.Insert update = ((InsertProducerMutation.Data) data).getProducer().getInsert();

    assertThat(update.getSpecification().getDescription().get(), is(factory.description));
    assertThat(update.getSpecification().getConfiguration().get(factory.key).asText(), is(factory.value));

    assertMutationFails(createMutation);
  }

  @Override
  public void update() {

    Mutation updateMutation = factory.updateProducerMutationBuilder().build();

    assertMutationFails(updateMutation);

    client.invoke(factory.insertProducerMutationBuilder().build());

    Object data = client.getData(updateMutation);
    UpdateProducerMutation.Update update = ((UpdateProducerMutation.Data) data).getProducer().getUpdate();

    assertThat(update.getSpecification().getDescription().get(), is(factory.description));
    assertThat(update.getSpecification().getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Override
  public void upsert() {

    Object data = client.getData(factory.upsertProducerMutationBuilder().build());

    UpsertProducerMutation.Upsert upsert = ((UpsertProducerMutation.Data) data).getProducer().getUpsert();

    assertThat(upsert.getSpecification().getDescription().get(), is(factory.description));
    assertThat(upsert.getSpecification().getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Override
  public void updateStatus() {
    client.getData(factory.upsertProducerMutationBuilder().build());
    Object data = client.getData(factory.updateProducerStatusBuilder().build());

    UpdateProducerStatusMutation.UpdateStatus update =
        ((UpdateProducerStatusMutation.Data) data).getProducer().getUpdateStatus();

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
    client.invoke(factory.upsertDomainMutationBuilder().build());
    client.invoke(factory.upsertSchemaMutationBuilder().build());
    client.invoke(factory.upsertStreamMutationBuilder().build());
  }
}
