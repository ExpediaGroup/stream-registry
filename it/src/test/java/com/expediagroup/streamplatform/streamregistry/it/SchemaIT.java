/**
 * Copyright (C) 2016-2019 Expedia Inc.
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

import com.expediagroup.streamplatform.streamregistry.graphql.client.UpsertSchemaMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpsertStreamMutation;
import com.expediagroup.streamplatform.streamregistry.it.helpers.ObjectIT;

public class SchemaIT extends ObjectIT {

  @Override
  @Ignore
  public void create() {

  }

  @Override
  @Ignore
  public void update() {

  }

  @Override
  public void upsert() {

    client.createDomain(factory);

    Object data = client.data(factory.upsertSchemaMutationBuilder().build());

    UpsertSchemaMutation.Upsert upsert = ((UpsertSchemaMutation.Data)data).getSchema().getUpsert();

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

