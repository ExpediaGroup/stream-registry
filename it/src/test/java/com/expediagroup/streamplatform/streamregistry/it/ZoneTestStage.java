/**
 * Copyright (C) 2018-2024 Expedia, Inc.
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.expediagroup.streamplatform.streamregistry.graphql.client.test.DeleteZoneMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.InsertZoneMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.UpdateZoneMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.UpdateZoneStatusMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.UpsertZoneMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.ZoneQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.ZonesQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.fragment.SpecificationPart;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.fragment.ZonePart;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.type.ZoneKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.type.ZoneKeyQuery;
import com.expediagroup.streamplatform.streamregistry.it.helpers.AbstractTestStage;

public class ZoneTestStage extends AbstractTestStage {

  @Override
  public void create() {
    Object data = client.getOptionalData(factory.insertZoneMutationBuilder().build()).get();

    InsertZoneMutation.Insert insert = ((InsertZoneMutation.Data) data).getZone().getInsert();

    ZonePart part = insert.getFragments().getZonePart();
    assertThat(part.getKey().getName(), is(factory.zoneName));

    SpecificationPart specificationPart = part.getSpecification().getFragments().getSpecificationPart();
    assertThat(specificationPart.getDescription().get(), is(factory.description));
    assertThat(specificationPart.getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Override
  public void update() {
    Object data = client.getOptionalData(factory.updateZoneMutationBuilder().build()).get();

    UpdateZoneMutation.Update update = ((UpdateZoneMutation.Data) data).getZone().getUpdate();

    ZonePart part = update.getFragments().getZonePart();
    assertThat(part.getKey().getName(), is(factory.zoneName));

    SpecificationPart specificationPart = part.getSpecification().getFragments().getSpecificationPart();
    assertThat(specificationPart.getDescription().get(), is(factory.description));
    assertThat(specificationPart.getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Override
  public void upsert() {
    Object data = client.getOptionalData(factory.upsertZoneMutationBuilder().build()).get();

    UpsertZoneMutation.Upsert upsert = ((UpsertZoneMutation.Data) data).getZone().getUpsert();

    ZonePart part = upsert.getFragments().getZonePart();
    assertThat(part.getKey().getName(), is(factory.zoneName));

    SpecificationPart specificationPart = part.getSpecification().getFragments().getSpecificationPart();
    assertThat(specificationPart.getDescription().get(), is(factory.description));
    assertThat(specificationPart.getConfiguration().get(factory.key).asText(), is(factory.value));
  }

  @Override
  public void delete() {
    setFactorySuffix("delete");

    Object data = client.getOptionalData(factory.deleteZoneMutationBuilder().build()).get();
    boolean result = ((DeleteZoneMutation.Data) data).getZone().isDelete();

    assertTrue(result);
  }

  @Override
  public void updateStatus() {
    client.getOptionalData(factory.upsertZoneMutationBuilder().build()).get();
    Object data = client.getOptionalData(factory.updateZoneStatusBuilder().build()).get();

    UpdateZoneStatusMutation.UpdateStatus update =
        ((UpdateZoneStatusMutation.Data) data).getZone().getUpdateStatus();

    ZonePart part = update.getFragments().getZonePart();

    assertThat(part.getSpecification().getFragments().getSpecificationPart().getDescription().get(), is(factory.description));
    assertThat(part.getStatus().get().getFragments().getStatusPart().getAgentStatus().get("skey").asText(), is("svalue"));
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

    assertEquals(after.getZone().getByKey().get().getFragments().getZonePart().getKey().getName(), input.name());
  }

  @Override
  public void queryByRegex() {

    setFactorySuffix("zone_query_by_regex");

    ZoneKeyQuery query = ZoneKeyQuery.builder().nameRegex("zone_name.*").build();

    ZonesQuery.Data before = (ZonesQuery.Data) client.getOptionalData(ZonesQuery.builder().key(query).build()).get();

    client.invoke(factory.upsertZoneMutationBuilder().build());

    ZonesQuery.Data after = (ZonesQuery.Data) client.getOptionalData(ZonesQuery.builder().key(query).build()).get();

    assertNotNull(after.getZone().getByQuery().get(0)
        .getFragments().getZonePart().getStatus().get()
        .getFragments().getStatusPart().getAgentStatus());
  }

  @Override
  public void createRequiredDatastoreState() {
  }

  @Override
  public void queryByInvalidKey() {
    ZoneKeyInput input = factory.zoneKeyInputBuilder().name("disnae_exist").build();
    assertFalse(client.getZone(input).isPresent());
  }
}
