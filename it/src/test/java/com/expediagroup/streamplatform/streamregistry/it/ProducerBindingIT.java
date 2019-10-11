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
import static org.junit.Assert.assertTrue;

import com.expediagroup.streamplatform.streamregistry.graphql.client.ProducerBindingQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.ProducerBindingsQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpdateProducerBindingStatusMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpsertProducerBindingMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.ZoneQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.ZonesQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.ProducerBindingKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.ProducerBindingKeyQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.ZoneKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.ZoneKeyQuery;
import com.expediagroup.streamplatform.streamregistry.it.helpers.ObjectIT;

public class ProducerBindingIT extends ObjectIT {

  @Override
  public void create() {

  }

  @Override
  public void update() {

  }

  @Override
  public void upsert() {

    Object data = client.getData(factory.upsertProducerBindingMutationBuilder().build());

    UpsertProducerBindingMutation.Upsert upsert = ((UpsertProducerBindingMutation.Data) data).getProducerBinding().getUpsert();

    assertThat(upsert.getSpecification().getDescription().get(), is(factory.description));
    assertThat(upsert.getSpecification().getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Override
  public void updateStatus() {
    client.getData(factory.upsertProducerBindingMutationBuilder().build());
    Object data = client.getData(factory.updateProducerBindingStatusBuilder().build());

    UpdateProducerBindingStatusMutation.UpdateStatus update =
        ((UpdateProducerBindingStatusMutation.Data) data).getProducerBinding().getUpdateStatus();

    assertThat(update.getSpecification().getDescription().get(), is(factory.description));
    assertThat(update.getStatus().get().getAgentStatus().get("skey").asText(), is("svalue"));
  }

  @Override
  public void queryByKey() {

    ProducerBindingKeyInput input = factory.producerBindingKeyInputBuilder().build();

    try {
      client.getData(ProducerBindingQuery.builder().key(input).build());
    }catch ( RuntimeException e){
      assertEquals(e.getMessage(),"No value present");
    }

    client.getData(factory.upsertProducerBindingMutationBuilder().build());

    ProducerBindingQuery.Data after = (ProducerBindingQuery.Data) client.getData(ProducerBindingQuery.builder().key(input).build());

    assertEquals(after.getProducerBinding().getKey().getStreamDomain(),input.streamDomain());

  }

  @Override
  public void queryByRegex() {

    ProducerBindingKeyQuery query = ProducerBindingKeyQuery.builder().producerNameRegex("producerName.*").build();

    ProducerBindingsQuery.Data before = (ProducerBindingsQuery.Data) client.getData(ProducerBindingsQuery.builder().key(query).build());

    client.getData(factory.upsertProducerBindingMutationBuilder().build());
    client.getData(factory.upsertProducerBindingMutationBuilder().build());

    ProducerBindingsQuery.Data after = (ProducerBindingsQuery.Data) client.getData(ProducerBindingsQuery.builder().key(query).build());

    assertTrue(after.getProducerBindings().size() == before.getProducerBindings().size() + 1);
  }

  @Override
  public void createRequiredDatastoreState() {

    client.invoke(factory.upsertDomainMutationBuilder().build());
    client.invoke(factory.upsertSchemaMutationBuilder().build());
    client.invoke(factory.upsertStreamMutationBuilder().build());
    client.invoke(factory.upsertProducerMutationBuilder().build());
  }
}
