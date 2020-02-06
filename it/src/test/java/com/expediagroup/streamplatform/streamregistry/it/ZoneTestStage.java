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

import com.expediagroup.streamplatform.streamregistry.graphql.client.InsertZoneMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpsertZoneMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.ZoneQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.ZonesQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.ZoneKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.ZoneKeyQuery;
import com.expediagroup.streamplatform.streamregistry.it.helpers.AbstractTestStage;

public class ZoneTestStage extends AbstractTestStage {

  @Override
  public void create() {
    Object data = client.getOptionalData(factory.insertZoneMutationBuilder().build()).get();

    InsertZoneMutation.Insert Insert = ((InsertZoneMutation.Data) data).getZone().getInsert();

    assertThat(Insert.getKey().getName(), is(factory.zoneName));
    assertThat(Insert.getSpecification().getDescription().get(), is(factory.description));
    assertThat(Insert.getSpecification().getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Override
  public void update() {
    Object data = client.getOptionalData(factory.upsertZoneMutationBuilder().build()).get();

    UpsertZoneMutation.Upsert upsert = ((UpsertZoneMutation.Data) data).getZone().getUpsert();

    assertThat(upsert.getKey().getName(), is(factory.zoneName));
    assertThat(upsert.getSpecification().getDescription().get(), is(factory.description));
    assertThat(upsert.getSpecification().getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Override
  public void upsert() {
    Object data = client.getOptionalData(factory.upsertZoneMutationBuilder().build()).get();

    UpsertZoneMutation.Upsert upsert = ((UpsertZoneMutation.Data) data).getZone().getUpsert();

    assertThat(upsert.getKey().getName(), is(factory.zoneName));
    assertThat(upsert.getSpecification().getDescription().get(), is(factory.description));
    assertThat(upsert.getSpecification().getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Override
  public void updateStatus() {
    Object data = client.getOptionalData(factory.upsertZoneMutationBuilder().build()).get();

    UpsertZoneMutation.Upsert upsert = ((UpsertZoneMutation.Data) data).getZone().getUpsert();

    assertThat(upsert.getKey().getName(), is(factory.zoneName));
    assertThat(upsert.getSpecification().getDescription().get(), is(factory.description));
    assertThat(upsert.getSpecification().getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Override
  public void queryByKey() {

    ZoneKeyInput input = factory.zoneKeyInputBuilder().build();

    try {
      client.getOptionalData(ZoneQuery.builder().key(input).build()).get();
    } catch (RuntimeException e) {
      assertEquals(e.getMessage(), "No value present");
    }

    client.getOptionalData(factory.upsertZoneMutationBuilder().build()).get();

    ZoneQuery.Data after = (ZoneQuery.Data) client.getOptionalData(ZoneQuery.builder().key(input).build()).get();

    assertEquals(after.getZone().getByKey().get().getKey().getName(), input.name());
  }

  @Override
  public void queryByRegex() {

    setFactorySuffix("zone_query_by_regex");

    ZoneKeyQuery query = ZoneKeyQuery.builder().nameRegex("zone_name.*").build();

    ZonesQuery.Data before = (ZonesQuery.Data) client.getOptionalData(ZonesQuery.builder().key(query).build()).get();

    client.invoke(factory.upsertZoneMutationBuilder().build());

    ZonesQuery.Data after = (ZonesQuery.Data) client.getOptionalData(ZonesQuery.builder().key(query).build()).get();

    assertEquals(after.getZone().getByQuery().size(), before.getZone().getByQuery().size() + 1);
  }

  @Override
  public void createRequiredDatastoreState() {
  }

  @Override
  public void queryByInvalidKey() {
    ZoneKeyInput input = factory.zoneKeyInputBuilder().name("disnae_exist").build();
    assertTrue(client.getZone(input).isEmpty());
  }
}
