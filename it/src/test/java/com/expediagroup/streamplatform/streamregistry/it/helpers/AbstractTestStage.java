/**
 * Copyright (C) 2018-2020 Expedia, Inc.
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
package com.expediagroup.streamplatform.streamregistry.it.helpers;

import static org.junit.Assert.assertTrue;

import junit.framework.TestCase;

import com.apollographql.apollo.api.Mutation;

import org.junit.Before;
import org.junit.Test;

import com.expediagroup.streamplatform.streamregistry.it.StreamRegistryIT;

public abstract class AbstractTestStage {
  protected ITestDataFactory factory = new ITestDataFactory(getClass().getSimpleName().toLowerCase());

  protected ITestClient client = StreamRegistryIT.client;

  @Before
  public final void before() {
    createRequiredDatastoreState();
  }

  @Test
  public abstract void create();

  @Test
  public abstract void update();

  @Test
  public abstract void upsert();

  @Test
  public abstract void updateStatus();

  @Test
  public abstract void queryByKey();

  @Test
  public abstract void queryByInvalidKey();

  @Test
  public abstract void queryByRegex();

  public abstract void createRequiredDatastoreState();

  public Mutation assertMutationFails(Mutation mutation) {
    if (mutation.getClass().getSimpleName().toLowerCase().contains("insert")) {
      assertRequiresObjectIsAbsent(mutation);
    }
    if (mutation.getClass().getSimpleName().toLowerCase().contains("update")) {
      assertRequiresObjectIsPresent(mutation);
    }
    return mutation;
  }

  public void assertRequiresObjectIsAbsent(Mutation m) {
    try {
      client.invoke(m);
      TestCase.fail("Expected a ValidationException");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().contains("Can't create because it already exists"));
    }
  }

  public void setFactorySuffix(String suffix) {
    factory = new ITestDataFactory(suffix);
    createRequiredDatastoreState();
  }

  public void assertRequiresObjectIsPresent(Mutation m) {
    try {
      client.invoke(m);
      TestCase.fail("Expected a ValidationException");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().contains("Can't update"));
    }
  }
}
