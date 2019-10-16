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
import com.expediagroup.streamplatform.streamregistry.graphql.client.InsertDomainMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpdateConsumerBindingMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpdateDomainMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpdateDomainStatusMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpsertDomainMutation;
import com.expediagroup.streamplatform.streamregistry.it.helpers.ObjectIT;

public class DomainIT extends ObjectIT {

  @Override
  public void create() {

    setFactorySuffix("Create");

    assertMutationFails(factory.updateDomainMutationBuilder().build());

    Object data = client.getData(factory.insertDomainMutationBuilder().build());

    InsertDomainMutation.Insert insert = ((InsertDomainMutation.Data) data).getDomain().getInsert();

    assertThat(insert.getKey().getName(), is(factory.domainName));

    assertThat(insert.getSpecification().getDescription().get(), is(factory.description));
    assertThat(insert.getSpecification().getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Test
  public void update() {

    setFactorySuffix("Update");

    Mutation updateMutation = factory.updateDomainMutationBuilder().build();

    assertMutationFails(updateMutation);

    client.invoke(factory.upsertDomainMutationBuilder().build());

    UpdateDomainMutation.Update update =
        ((UpdateDomainMutation.Data) client.getData(updateMutation)).getDomain().getUpdate();

    assertThat(update.getKey().getName(), is(factory.domainName));

    assertThat(update.getSpecification().getDescription().get(), is(factory.description));
    assertThat(update.getSpecification().getConfiguration().get(factory.key).asText(), is(factory.value));
  }
  
  @Override
  public void upsert() {
    Object data = client.getData(factory.upsertDomainMutationBuilder().build());

    UpsertDomainMutation.Upsert upsert = ((UpsertDomainMutation.Data) data).getDomain().getUpsert();

    assertThat(upsert.getKey().getName(), is(factory.domainName));
    assertThat(upsert.getSpecification().getDescription().get(), is(factory.description));
    assertThat(upsert.getSpecification().getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Override
  public void updateStatus() {
    client.getData(factory.upsertDomainMutationBuilder().build());
    Object data = client.getData(factory.updateDomainStatusMutation().build());

    UpdateDomainStatusMutation.UpdateStatus update =
        ((UpdateDomainStatusMutation.Data) data).getDomain().getUpdateStatus();

    assertThat(update.getSpecification().getDescription().get(), is(factory.description));
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
