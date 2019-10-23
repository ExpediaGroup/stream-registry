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

import com.expediagroup.streamplatform.streamregistry.graphql.client.InsertZoneMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpsertZoneMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.ZoneQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.ZonesQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.ZoneKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.ZoneKeyQuery;
import com.expediagroup.streamplatform.streamregistry.it.helpers.ObjectIT;

public class ZoneIT extends ObjectIT {

  @Override
  public void create() {
    Object data = client.getData(factory.insertZoneMutationBuilder().build());

    InsertZoneMutation.Insert Insert = ((InsertZoneMutation.Data) data).getZone().getInsert();

    assertThat(Insert.getKey().getName(), is(factory.zoneName));
    assertThat(Insert.getSpecification().getDescription().get(), is(factory.description));
    assertThat(Insert.getSpecification().getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Override
  public void update() {
    Object data = client.getData(factory.upsertZoneMutationBuilder().build());

    UpsertZoneMutation.Upsert upsert = ((UpsertZoneMutation.Data) data).getZone().getUpsert();

    assertThat(upsert.getKey().getName(), is(factory.zoneName));
    assertThat(upsert.getSpecification().getDescription().get(), is(factory.description));
    assertThat(upsert.getSpecification().getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Override
  public void upsert() {
    Object data = client.getData(factory.upsertZoneMutationBuilder().build());

    UpsertZoneMutation.Upsert upsert = ((UpsertZoneMutation.Data) data).getZone().getUpsert();

    assertThat(upsert.getKey().getName(), is(factory.zoneName));
    assertThat(upsert.getSpecification().getDescription().get(), is(factory.description));
    assertThat(upsert.getSpecification().getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Override
  public void updateStatus() {
    Object data = client.getData(factory.upsertZoneMutationBuilder().build());

    UpsertZoneMutation.Upsert upsert = ((UpsertZoneMutation.Data) data).getZone().getUpsert();

    assertThat(upsert.getKey().getName(), is(factory.zoneName));
    assertThat(upsert.getSpecification().getDescription().get(), is(factory.description));
    assertThat(upsert.getSpecification().getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Override
  public void queryByKey() {

    ZoneKeyInput input = factory.zoneKeyInputBuilder().build();

    try {
      client.getData(ZoneQuery.builder().key(input).build());
    } catch (RuntimeException e) {
      assertEquals(e.getMessage(), "No value present");
    }

    client.getData(factory.upsertZoneMutationBuilder().build());

    ZoneQuery.Data after = (ZoneQuery.Data) client.getData(ZoneQuery.builder().key(input).build());

    assertEquals(after.getZoneQuery().getByKey().getKey().getName(), input.name());
  }

  @Override
  public void queryByRegex() {

    setFactorySuffix("query_by_regex");

    ZoneKeyQuery query = ZoneKeyQuery.builder().nameRegex("zone_name.*").build();

    ZonesQuery.Data before = (ZonesQuery.Data) client.getData(ZonesQuery.builder().key(query).build());

    client.invoke(factory.upsertZoneMutationBuilder().build());

    ZonesQuery.Data after = (ZonesQuery.Data) client.getData(ZonesQuery.builder().key(query).build());

    assertEquals(after.getZoneQuery().getByQuery().size(), before.getZoneQuery().getByQuery().size() + 1);
  }

  @Override
  public void createRequiredDatastoreState() {
  }
}
