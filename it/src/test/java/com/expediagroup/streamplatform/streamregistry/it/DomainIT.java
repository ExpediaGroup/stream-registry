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

import org.junit.Ignore;
import org.junit.Test;

import com.expediagroup.streamplatform.streamregistry.graphql.client.UpsertDomainMutation;
import com.expediagroup.streamplatform.streamregistry.it.helpers.ObjectIT;

public class DomainIT extends ObjectIT {

  @Test
  @Ignore
  public void create() {

  }

  @Override
  @Ignore
  public void update() {

  }

  @Override
  public void upsert() {
    Object data = client.data(factory.upsertDomainMutationBuilder().build());

    UpsertDomainMutation.Upsert upsert = ((UpsertDomainMutation.Data) data).getDomain().getUpsert();

    assertThat(upsert.getKey().getName(), is(factory.domainName.getValue()));
    assertThat(upsert.getSpecification().getDescription().get(), is(factory.description.getValue()));
    assertThat(upsert.getSpecification().getConfiguration().get(factory.key.toString()).asText(), is(factory.value.toString()));
  }

  @Override
  @Ignore
  public void updateStatus() {

  }

  @Override
  @Ignore
  public void queryByKey() {

  }

  @Override
  @Ignore
  public void queryByRegex() {

  }
}
