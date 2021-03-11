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
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.apollographql.apollo.api.Mutation;

import com.expediagroup.streamplatform.streamregistry.graphql.client.test.ConsumerBindingQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.ConsumerBindingsQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.DeleteConsumerBindingMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.InsertConsumerBindingMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.UpdateConsumerBindingMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.UpdateConsumerBindingStatusMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.UpsertConsumerBindingMutation;
import org.junit.Ignore;
import org.junit.Test;

import com.expediagroup.streamplatform.streamregistry.graphql.client.test.fragment.ConsumerBindingPart;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.fragment.SpecificationPart;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.type.ConsumerBindingKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.type.ConsumerBindingKeyQuery;
import com.expediagroup.streamplatform.streamregistry.it.helpers.AbstractTestStage;

public class ConsumerBindingTestStage extends AbstractTestStage {

  @Override
  public void create() {

    setFactorySuffix("create");

    assertMutationFails(factory.updateConsumerBindingMutationBuilder().build());

    Object data = client.getOptionalData(factory.insertConsumerBindingMutationBuilder().build()).get();

    InsertConsumerBindingMutation.Insert insert = ((InsertConsumerBindingMutation.Data) data).getConsumerBinding().getInsert();

    ConsumerBindingPart part = insert.getFragments().getConsumerBindingPart();
    assertThat(part.getKey().getStreamName(), is(factory.streamName));

    SpecificationPart specificationPart = part.getSpecification().getFragments().getSpecificationPart();
    assertThat(specificationPart.getDescription().get(), is(factory.description));
    assertThat(specificationPart.getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Test
  public void update() {

    setFactorySuffix("update");

    Mutation updateMutation = factory.updateConsumerBindingMutationBuilder().build();

    assertMutationFails(updateMutation);

    client.invoke(factory.insertConsumerBindingMutationBuilder().build());

    UpdateConsumerBindingMutation.Update update = ((UpdateConsumerBindingMutation.Data) client.getOptionalData(updateMutation).get())
        .getConsumerBinding().getUpdate();

    ConsumerBindingPart part = update.getFragments().getConsumerBindingPart();
    assertThat(part.getKey().getStreamName(), is(factory.streamName));

    SpecificationPart specificationPart = part.getSpecification().getFragments().getSpecificationPart();
    assertThat(specificationPart.getDescription().get(), is(factory.description));
    assertThat(specificationPart.getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Override
  public void upsert() {

    setFactorySuffix("upsert");

    Object data = client.getOptionalData(factory.upsertConsumerBindingMutationBuilder().build()).get();

    UpsertConsumerBindingMutation.Upsert upsert = ((UpsertConsumerBindingMutation.Data) data).getConsumerBinding().getUpsert();

    ConsumerBindingPart part = upsert.getFragments().getConsumerBindingPart();
    SpecificationPart specificationPart = part.getSpecification().getFragments().getSpecificationPart();
    assertThat(specificationPart.getDescription().get(), is(factory.description));
    assertThat(specificationPart.getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Override
  public void delete() {
    setFactorySuffix("delete");
  }

  @Override
  public void updateStatus() {
    client.getOptionalData(factory.upsertConsumerBindingMutationBuilder().build()).get();
    Object data = client.getOptionalData(factory.updateConsumerBindingStatusBuilder().build()).get();

    UpdateConsumerBindingStatusMutation.UpdateStatus update =
        ((UpdateConsumerBindingStatusMutation.Data) data).getConsumerBinding().getUpdateStatus();

    ConsumerBindingPart part = update.getFragments().getConsumerBindingPart();
    SpecificationPart specificationPart = part.getSpecification().getFragments().getSpecificationPart();
    assertThat(specificationPart.getDescription().get(), is(factory.description));
    assertThat(part.getStatus().get().getFragments().getStatusPart().getAgentStatus().get("skey").asText(), is("svalue"));
  }

  @Override
  public void queryByKey() {

    ConsumerBindingKeyInput input = factory.consumerBindingKeyInputBuilder().build();

    try {
      client.getOptionalData(ConsumerBindingQuery.builder().key(input).build()).get();
    } catch (RuntimeException e) {
      assertEquals(e.getMessage(), "No value present");
    }

    client.getOptionalData(factory.upsertConsumerBindingMutationBuilder().build()).get();

    ConsumerBindingQuery.Data after = (ConsumerBindingQuery.Data) client.getOptionalData(ConsumerBindingQuery.builder().key(input).build()).get();

    assertEquals(after.getConsumerBinding().getByKey().get().getFragments().getConsumerBindingPart().getKey().getStreamName(), input.streamName());
  }

  @Override
  public void queryByRegex() {

    setFactorySuffix("query_by_regex");

    client.invoke(factory.upsertConsumerBindingMutationBuilder().build());

    ConsumerBindingKeyQuery query = ConsumerBindingKeyQuery.builder().consumerNameRegex(".*query_by_regex").build();
    ConsumerBindingsQuery.Data data = (ConsumerBindingsQuery.Data) client.getOptionalData(ConsumerBindingsQuery.builder().key(query).build()).get();

    assertEquals(1, data.getConsumerBinding().getByQuery().size());
  }

  @Override
  public void createRequiredDatastoreState() {
    client.createDomain(factory);
    client.createSchema(factory);
    client.createStream(factory);
    client.createZone(factory);
    client.createInfrastructure(factory);
    client.createConsumer(factory);
    client.createStreamBinding(factory);
  }

  @Override
  public void queryByInvalidKey() {
    ConsumerBindingKeyInput input = factory.consumerBindingKeyInputBuilder().consumerName("disnae_exist").build();
    assertFalse(client.getConsumerBinding(input).isPresent());
  }
}
